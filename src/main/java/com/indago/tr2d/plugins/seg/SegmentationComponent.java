
package com.indago.tr2d.plugins.seg;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.DefaultExtensible;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.BasicLabelingComponent;
import net.imglib2.labkit.actions.SelectClassifier;
import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.labkit.panel.GuiUtils;
import net.imglib2.labkit.panel.LabelPanel;
import net.imglib2.labkit.panel.SegmenterPanel;
import net.imglib2.labkit.segmentation.TrainClassifier;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.IntType;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;

import javax.swing.*;
import java.util.List;

public class SegmentationComponent implements AutoCloseable {

	private final JSplitPane panel;

	private final JFrame dialogBoxOwner = null;

	private final BasicLabelingComponent labelingComponent;

	private final Context context;

	private final SegmentationModel segmentationModel;

	public SegmentationComponent(SegmentationModel segmentationModel)
	{
		this.segmentationModel = segmentationModel;
		this.context = segmentationModel.getContext();
		labelingComponent = new BasicLabelingComponent(dialogBoxOwner,
			segmentationModel.imageLabelingModel());
		labelingComponent.addBdvLayer(new PredictionLayer(segmentationModel
			.selectedSegmenter()));
		initActions();
		JPanel leftPanel = initLeftPanel();
		this.panel = initPanel(leftPanel, labelingComponent.getComponent());
	}

	private void initActions() {
		Extensible extensible = new DefaultExtensible(context, dialogBoxOwner,
			labelingComponent);
		new TrainClassifier(extensible, segmentationModel);
		new SelectClassifier(extensible, segmentationModel.selectedSegmenter());
	}

	private JPanel initLeftPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[grow]", "[][grow][grow][]"));
		ActionMap actions = getActions();
		panel.add(GuiUtils.createCheckboxGroupedPanel(actions.get("Image"), GuiUtils
			.createDimensionsInfo(segmentationModel.image())), "grow, wrap");
		panel.add(GuiUtils.createCheckboxGroupedPanel(actions.get("Labeling"),
			new LabelPanel(dialogBoxOwner, new ColoredLabelsModel(segmentationModel
				.imageLabelingModel()), true).getComponent()), "grow, wrap");
		panel.add(GuiUtils.createCheckboxGroupedPanel(actions.get("Segmentation"),
			new SegmenterPanel(segmentationModel).getComponent()),
			"grow, wrap");
		panel.add(new ThresholdButton(segmentationModel).getComponent(), "grow");
		return panel;
	}

	private JSplitPane initPanel(JComponent left, JComponent right) {
		JSplitPane panel = new JSplitPane();
		panel.setSize(100, 100);
		panel.setOneTouchExpandable(true);
		panel.setLeftComponent(left);
		panel.setRightComponent(right);
		return panel;
	}

	public JComponent getComponent() {
		return panel;
	}

	private ActionMap getActions() {
		return labelingComponent.getActions();
	}

	public List<RandomAccessibleInterval<IntType >> getSegmentations()
	{
		return segmentationModel.getSegmentations();
	}

	@Override
	public void close() {
		labelingComponent.close();
	}
}
