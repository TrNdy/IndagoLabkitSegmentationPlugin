
package com.indago.tr2d.plugins.seg;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.color.ColorMap;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.DefaultHolder;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.SegmenterListModel;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.segmentation.weka.TimeSeriesSegmenter;
import net.imglib2.labkit.segmentation.weka.TrainableSegmentationSegmenter;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Serves as a model for PredictionLayer and TrainClassifierAction
 */
public class SegmentationModel implements
	net.imglib2.labkit.models.SegmentationModel, SegmenterListModel
{

	private final ImageLabelingModel imageLabelingModel;
	private final Holder<MySegmentationItem> selectedSegmenter;
	private final InputImage inputImage;
	private List<MySegmentationItem> segmenters = new ArrayList<>();
	private final RandomAccessibleInterval<? extends NumericType<?>> compatibleImage;
	private final CellGrid grid;

	private Context context;

	public SegmentationModel(
			RandomAccessibleInterval< ? extends NumericType< ? > > image,
			Context context)
	{
		this.context = context;
		this.inputImage = initInputImage(image, true);
		this.compatibleImage = inputImage.imageForSegmentation();
		this.grid = LabkitUtils.suggestGrid(compatibleImage, inputImage.isTimeSeries());
		MySegmentationItem segmentationItem = addSegmenter();
		this.selectedSegmenter = new DefaultHolder<>(segmentationItem);
		this.selectedSegmenter.notifier().add(this::selectedSegmenterChanged);
		this.imageLabelingModel = new ImageLabelingModel(inputImage.showable(),
			segmentationItem.labeling(), true);
	}

	private static DefaultInputImage initInputImage(
			RandomAccessibleInterval<? extends NumericType<?>> image,
			boolean isTimeSeries)
	{
		DefaultInputImage defaultInputImage = new DefaultInputImage(image);
		defaultInputImage.setTimeSeries(isTimeSeries);
		return defaultInputImage;
	}

	private void selectedSegmenterChanged(MySegmentationItem segmentationItem) {
		imageLabelingModel.labeling().set(segmentationItem.labeling());
	}

	@Override
	public Labeling labeling() {
		return imageLabelingModel.labeling().get();
	}

	@Override
	public RandomAccessibleInterval<?> image() {
		return compatibleImage;
	}

	@Override
	public CellGrid grid() {
		return grid;
	}

	@Override
	public List<MySegmentationItem> segmenters() {
		return segmenters;
	}

	@Override
	public Holder<MySegmentationItem> selectedSegmenter() {
		return selectedSegmenter;
	}

	@Override
	public ColorMap colorMap() {
		return imageLabelingModel.colorMapProvider().colorMap();
	}

	@Override
	public AffineTransform3D labelTransformation() {
		return imageLabelingModel.labelTransformation();
	}

	@Override
	public MySegmentationItem addSegmenter() {
		MySegmentationItem segmentationItem = new MySegmentationItem(this,
			initClassifier());
		this.segmenters.add(segmentationItem);
		return segmentationItem;
	}

	private Segmenter initClassifier() {
		TrainableSegmentationSegmenter classifier1 =
			new TrainableSegmentationSegmenter(context, inputImage);
		return inputImage.isTimeSeries() ? new TimeSeriesSegmenter(classifier1)
			: classifier1;
	}

	@Override
	public void trainSegmenter() {
		selectedSegmenter().get().train();
	}

	public List<RandomAccessibleInterval<IntType>> getSegmentations()
	{
		Stream<MySegmentationItem> trainedSegmenters = getTrainedSegmenters();
		return trainedSegmenters.map(segmenter -> segmenter.getSegmentation()).collect(
			Collectors.toList());
	}

	private Stream<MySegmentationItem> getTrainedSegmenters() {
		return segmenters().stream().filter(x -> x.segmenter().isTrained());
	}

	public ImageLabelingModel imageLabelingModel() {
		return imageLabelingModel;
	}

	public Context getContext() {
		return context;
	}
}
