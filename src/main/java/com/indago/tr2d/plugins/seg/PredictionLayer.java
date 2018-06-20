
package com.indago.tr2d.plugins.seg;

import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.labkit.bdv.BdvLayer;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmentationResultsModel;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.utils.RandomAccessibleContainer;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.type.volatiles.VolatileFloatType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.DoubleToIntFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

public class PredictionLayer implements BdvLayer {

	private final Holder<? extends MySegmentationItem> model;
	private final RandomAccessibleContainer<VolatileARGBType> segmentationContainer;
	private final SharedQueue queue = new SharedQueue(Runtime.getRuntime()
		.availableProcessors());
	private Notifier<Runnable> listeners = new Notifier<>();
	private RandomAccessibleInterval<? extends NumericType<?>> view;
	private AffineTransform3D transformation;
	private Set<MySegmentationItem> alreadyRegistered = Collections.newSetFromMap(
		new WeakHashMap<>());

	public PredictionLayer(Holder<? extends MySegmentationItem> model) {
		this.model = model;
		SegmentationResultsModel selected = model.get().results();
		this.segmentationContainer = new RandomAccessibleContainer<>(
			getEmptyPrediction(selected));
		this.transformation = selected.transformation();
		this.view = Views.interval(segmentationContainer, selected.interval());
		model.notifier().add(ignore -> classifierChanged());
		registerListener(model.get());
	}

	private void registerListener(MySegmentationItem segmenter) {
		if (alreadyRegistered.contains(segmenter)) return;
		alreadyRegistered.add(segmenter);
		segmenter.segmenter().listeners().add(this::onTrainingFinished);
		segmenter.thresholds().notifier().add(ignore -> onTrainingFinished(segmenter
			.segmenter()));
	}

	private void onTrainingFinished(Segmenter segmenter) {
		if (model.get().segmenter() == segmenter) classifierChanged();
	}

	private RandomAccessible<VolatileARGBType> getEmptyPrediction(
		SegmentationResultsModel selected)
	{
		return ConstantUtils.constantRandomAccessible(new VolatileARGBType(0),
			selected.interval().numDimensions());
	}

	private void classifierChanged() {
		MySegmentationItem segmentationItem = model.get();
		registerListener(segmentationItem);
		SegmentationResultsModel selected = segmentationItem.results();
		RandomAccessible<VolatileARGBType> source = selected.hasResults() ? Views
			.extendValue(coloredVolatileView(segmentationItem), new VolatileARGBType(
				0)) : getEmptyPrediction(selected);
		segmentationContainer.setSource(source);
		listeners.forEach(Runnable::run);
	}

	private RandomAccessibleInterval<VolatileARGBType> coloredVolatileView(
		MySegmentationItem segmentationItem)
	{
		SegmentationResultsModel selected = segmentationItem.results();
		DoubleToIntFunction thresholdFunction = segmentationItem
			.thresholdFunction();
		ARGBType[] colors = setupColors(segmentationItem.thresholds().get().size(),
			selected.colors().get(0), selected.colors().get(1));
		final Converter<VolatileFloatType, VolatileARGBType> conv = (input,
			output) -> {
			final boolean isValid = input.isValid();
			output.setValid(isValid);
			if (isValid) output.set(colors[thresholdFunction.applyAsInt(input.get()
				.get())].get());
		};

		RandomAccessibleInterval<VolatileFloatType> source = Views.hyperSlice(
			VolatileViews.wrapAsVolatile(selected.prediction(), queue), 3, 1);
		return Converters.convert(source, conv, new VolatileARGBType());
	}

	private ARGBType[] setupColors(int size, ARGBType background,
		ARGBType foreground)
	{
		return IntStream.rangeClosed(0, size).mapToObj(i -> blend((double) i /
			(double) size, background, foreground)).toArray(ARGBType[]::new);
	}

	private ARGBType blend(double alpha, ARGBType background,
		ARGBType foreground)
	{
		int r = blend(alpha, background, foreground, ARGBType::red);
		int g = blend(alpha, background, foreground, ARGBType::green);
		int b = blend(alpha, background, foreground, ARGBType::blue);
		return new ARGBType(ARGBType.rgba(r, g, b, 255));
	}

	private int blend(double alpha, ARGBType background, ARGBType foreground,
		IntUnaryOperator channel)
	{
		return (int) (alpha * channel.applyAsInt(foreground.get()) + (1 - alpha) *
			channel.applyAsInt(background.get()));
	}

	@Override
	public BdvShowable image() {
		return BdvShowable.wrap(view, transformation);
	}

	@Override
	public Notifier<Runnable> listeners() {
		return listeners;
	}

	@Override
	public String title() {
		return "Segmentation";
	}
}
