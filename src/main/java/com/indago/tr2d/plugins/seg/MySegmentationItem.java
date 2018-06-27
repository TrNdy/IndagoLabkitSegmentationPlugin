
package com.indago.tr2d.plugins.seg;

import com.indago.io.ProjectFile;
import com.indago.io.ProjectFolder;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.labkit.models.DefaultHolder;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;
import org.scijava.Context;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleToIntFunction;

public class MySegmentationItem extends SegmentationItem {

	private static final String LABELING_FILENAME = "segmenter.labeling";
	private static final String CLASSIFIER_FILENAME = "segmenter.classifier";

	private final Labeling labeling;

	private final SegmentationModel model;

	private final Holder<List<Double>> thresholds = new DefaultHolder<>(
		Collections.singletonList(0.5));
	private RandomAccessibleInterval< IntType > segmentation = null;

	public MySegmentationItem(SegmentationModel model, Segmenter segmenter, Labeling labeling) {
		super(model, segmenter);
		this.model = model;
		this.labeling = labeling;
		segmenter.listeners().add(ignore -> resetSegmentation());
		thresholds.notifier().add(ignore -> resetSegmentation());
	}

	public MySegmentationItem(SegmentationModel model, Segmenter segmenter) {
		this(model, segmenter, defaultLabeling(model));
	}

	private static Labeling defaultLabeling(SegmentationModel model) {
		return new Labeling(Arrays.asList("background", "foreground"), model
				.image());
	}

	private void resetSegmentation() {
		segmentation = null;
	}

	public RandomAccessibleInterval<IntType> getSegmentation()
	{
		if(segmentation == null)
			segmentation = calculateSegmentation();
		return segmentation;
	}

	private Img< IntType > calculateSegmentation() {
		RandomAccessibleInterval<?> image = model.image();
		Img<IntType> segmentation = new CellImgFactory<>(new IntType()).create( image);
		RandomAccessibleInterval<DoubleType > dummy = ConstantUtils
			.constantRandomAccessibleInterval(new DoubleType(), image.numDimensions(),
				image);
		RandomAccessibleInterval<DoubleType> probability = new CellImgFactory<>(
			new DoubleType()).create(image);
		RandomAccessibleInterval<DoubleType> probabilities = Views.stack(dummy,
			probability);
		segmenter().predict(image, probabilities);
		DoubleToIntFunction thresholdFunction = thresholdFunction();
		Views.interval(Views.pair(probability, segmentation), segmentation).forEach(pair -> pair
			.getB().setInteger(thresholdFunction.applyAsInt(pair.getA().get())));
		return segmentation;
	}

	public DoubleToIntFunction thresholdFunction() {
		double[] thresholds = thresholds().get().stream().mapToDouble(x -> x)
			.toArray();
		Arrays.sort(thresholds);
		return (p) -> {
			int result = 0;
			for (double threshold : thresholds)
				if (p < threshold) break;
				else result++;
			return result;
		};
	}

	public Labeling labeling() {
		return labeling;
	}

	public void train() {
		segmenter().train(Collections.singletonList(model.image()), Collections
			.singletonList(labeling));
	}

	public Holder<List<Double>> thresholds() {
		return thresholds;
	}

	public static MySegmentationItem open(SegmentationModel model,
			Segmenter initialSegmenter,
			Context context, ProjectFolder subFolder) throws IOException
	{
		Labeling labeling = openLabeling(model, context, subFolder);
		Segmenter segmenter = openClassifier(initialSegmenter, subFolder);
		return new MySegmentationItem(model, segmenter, labeling);
	}

	private static Segmenter openClassifier(Segmenter initialSegmenter,
			ProjectFolder subFolder) throws IOException
	{
		ProjectFile classifierFile = subFolder.addFile(CLASSIFIER_FILENAME);
		Segmenter segmenter = initialSegmenter;
		if (classifierFile.exists()) try {
			segmenter.openModel(classifierFile.getAbsolutePath());
		}
		catch (Exception e) {
			throw new IOException(e);
		}
		return segmenter;
	}

	private static Labeling openLabeling(SegmentationModel model,
			Context context, ProjectFolder subFolder) throws IOException
	{
		ProjectFile labelingFile = subFolder.addFile(LABELING_FILENAME);
		return labelingFile.exists() ?
				new LabelingSerializer(context).open(labelingFile.getAbsolutePath()) :
				defaultLabeling(model);
	}

	public void save(Context context, ProjectFolder subFolder) throws IOException {
		saveLabeling(context, subFolder);
		saveClassifier(subFolder);
	}

	private void saveLabeling(Context context, ProjectFolder subFolder)
			throws IOException
	{
		ProjectFile labelingFile = subFolder.addFile(LABELING_FILENAME);
		new LabelingSerializer(context).save(labeling, labelingFile.getAbsolutePath());
	}

	private void saveClassifier(ProjectFolder subFolder) throws IOException {
		ProjectFile classifierFile = subFolder.addFile(CLASSIFIER_FILENAME);
		if(classifierFile.exists())
			classifierFile.getFile().delete();
		String classifierFilename = classifierFile.getAbsolutePath();
		Segmenter segmenter = segmenter();
		if(segmenter.isTrained())
			try {
				segmenter.saveModel(classifierFilename, false);
			}
			catch (Exception e) {
				throw new IOException(e);
			}
	}

}
