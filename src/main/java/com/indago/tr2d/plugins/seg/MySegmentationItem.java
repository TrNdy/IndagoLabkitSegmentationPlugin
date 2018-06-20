
package com.indago.tr2d.plugins.seg;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.models.DefaultHolder;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
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

	public MySegmentationItem(SegmentationModel model, Segmenter segmenter) {
		super(model, segmenter);
		this.model = model;
		labeling = new Labeling(Arrays.asList("background", "foreground"), model
			.image());
	}

	public <T extends IntegerType<T> & NativeType<T>> RandomAccessibleInterval<T>
		apply(T type)
	{
		RandomAccessibleInterval<?> image = model.image();
		RandomAccessibleInterval<T> labels = new CellImgFactory<>(type).create(
			image);
		RandomAccessibleInterval<DoubleType> dummy = ConstantUtils
			.constantRandomAccessibleInterval(new DoubleType(), image.numDimensions(),
				image);
		RandomAccessibleInterval<DoubleType> probability = new CellImgFactory<>(
			new DoubleType()).create(image);
		RandomAccessibleInterval<DoubleType> probabilities = Views.stack(dummy,
			probability);
		segmenter().predict(image, probabilities);
		DoubleToIntFunction thresholdFunction = thresholdFunction();
		Views.interval(Views.pair(probability, labels), labels).forEach(pair -> pair
			.getB().setInteger(thresholdFunction.applyAsInt(pair.getA().get())));
		return labels;
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
