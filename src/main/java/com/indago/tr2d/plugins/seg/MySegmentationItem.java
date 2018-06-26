
package com.indago.tr2d.plugins.seg;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.models.DefaultHolder;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleToIntFunction;

public class MySegmentationItem extends SegmentationItem {

	private final Labeling labeling;

	private final SegmentationModel model;

	private final Holder<List<Double>> thresholds = new DefaultHolder<>(
		Collections.singletonList(0.5));
	private RandomAccessibleInterval< IntType > segmentation = null;

	public MySegmentationItem(SegmentationModel model, Segmenter segmenter) {
		super(model, segmenter);
		this.model = model;
		labeling = new Labeling(Arrays.asList("background", "foreground"), model
			.image());
		segmenter.listeners().add(ignore -> resetSegmentation());
		thresholds.notifier().add(ignore -> resetSegmentation());
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
}
