
package com.indago.tr2d.plugins.seg;

import com.indago.io.ProjectFile;
import com.indago.io.ProjectFolder;
import io.scif.img.ImgOpener;
import io.scif.img.ImgSaver;
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
import net.imglib2.type.numeric.real.FloatType;
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
	private static final String PROBABILITY_IMAGE_FILENAME = "probability.tif";

	private final Labeling labeling;

	private final SegmentationModel model;

	private final Holder<List<Double>> thresholds = new DefaultHolder<>(
		Collections.singletonList(0.5));
	private Img< FloatType > probability = null;
	private RandomAccessibleInterval< IntType > segmentation = null;

	public MySegmentationItem(SegmentationModel model, Segmenter segmenter, Labeling labeling, Img<FloatType> probability) {
		super(model, segmenter);
		this.model = model;
		this.labeling = labeling;
		this.probability = probability;
		segmenter.listeners().add(ignore -> resetProbability());
		thresholds.notifier().add(ignore -> resetSegmentation());
	}

	public MySegmentationItem(SegmentationModel model, Segmenter segmenter) {
		this(model, segmenter, defaultLabeling(model), null);
	}

	private static Labeling defaultLabeling(SegmentationModel model) {
		return new Labeling(Arrays.asList("background", "foreground"), model
				.image());
	}

	private void resetProbability() {
		resetSegmentation();
		probability = null;
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
		RandomAccessibleInterval< FloatType > probability = getProbability();
		Img<IntType> segmentation = new CellImgFactory<>(new IntType()).create(model.image());
		DoubleToIntFunction thresholdFunction = thresholdFunction();
		Views.interval(Views.pair(probability, segmentation), segmentation).forEach(pair -> pair
			.getB().setInteger(thresholdFunction.applyAsInt(pair.getA().get())));
		return segmentation;
	}

	private RandomAccessibleInterval<FloatType> getProbability() {
		if(probability == null)
			probability = calculateProbability();
		return probability;
	}

	private Img< FloatType > calculateProbability() {
		RandomAccessibleInterval<?> image = model.image();
		RandomAccessibleInterval<FloatType> dummy = ConstantUtils
			.constantRandomAccessibleInterval(new FloatType(), image.numDimensions(),
				image);
		Img<FloatType> probability = new CellImgFactory<>(
			new FloatType()).create(image);
		RandomAccessibleInterval<FloatType> probabilities = Views.stack(dummy,
			probability);
		segmenter().predict(image, probabilities);
		return probability;
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
		Img<FloatType> probability = openProbability(context, subFolder);
		return new MySegmentationItem(model, segmenter, labeling, probability);
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

	private static Img<FloatType> openProbability(Context context,
			ProjectFolder subFolder) {
		ProjectFile probabilityFile = subFolder.addFile("probability.tif");
		if(!probabilityFile.exists())
			return null;
		try {
			return (Img<FloatType>) new ImgOpener(context).openImgs(probabilityFile.getAbsolutePath()).get(0);
		} catch(Exception e) {
			return null;
		}
	}


	public void save(Context context, ProjectFolder subFolder) throws IOException {
		saveLabeling(context, subFolder);
		saveClassifier(subFolder);
		saveProbability(context, subFolder);
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

	private void saveProbability(Context context, ProjectFolder subFolder) {
		ProjectFile probabilityFile = subFolder.addFile(
				PROBABILITY_IMAGE_FILENAME);
		if (probability == null)
			probabilityFile.getFile().delete();
		else
			new ImgSaver(context).saveImg(probabilityFile.getAbsolutePath(), probability);
	}

}
