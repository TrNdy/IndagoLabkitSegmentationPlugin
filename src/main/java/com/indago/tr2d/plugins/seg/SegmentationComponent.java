
package com.indago.tr2d.plugins.seg;

import net.imglib2.labkit.BasicLabelingComponent;
import net.imglib2.labkit.DefaultExtensible;
import net.imglib2.labkit.actions.ClassifierSettingsAction;
import net.imglib2.labkit.actions.LabelEditAction;
import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.labkit.panel.ImageInfoPanel;
import net.imglib2.labkit.panel.LabelPanel;
import net.imglib2.labkit.panel.SegmenterPanel;
import net.imglib2.labkit.segmentation.TrainClassifier;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class SegmentationComponent implements AutoCloseable {

	private final JSplitPane panel;

	private final BasicLabelingComponent labelingComponent;

	private final SegmentationModel segmentationModel;

	private final DefaultExtensible extensible;

	public SegmentationComponent(SegmentationModel segmentationModel)
	{
		this.segmentationModel = segmentationModel;
		JFrame dialogBoxOwner = null;
		this.extensible = new DefaultExtensible(segmentationModel.getContext(),
				dialogBoxOwner);
		labelingComponent = new BasicLabelingComponent(dialogBoxOwner,
			segmentationModel.imageLabelingModel());
		labelingComponent.addBdvLayer(new PredictionLayer(segmentationModel
			.selectedSegmenter(), segmentationModel.segmentationVisibility()));
		initActions();
		JPanel leftPanel = initLeftPanel();
		this.panel = initPanel(leftPanel, labelingComponent.getComponent());
	}

	private void initActions() {
		new TrainClassifier(extensible, segmentationModel);
		new LabelEditAction(extensible, true, new ColoredLabelsModel(segmentationModel.imageLabelingModel()));
		new ClassifierSettingsAction(extensible, segmentationModel.selectedSegmenter());
		labelingComponent.addShortcuts(extensible.getShortCuts());
	}

	private JPanel initLeftPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[grow]", "[][grow][grow][]"));
		panel.add(ImageInfoPanel.newFramedImageInfoPanel(segmentationModel.imageLabelingModel()), "grow, wrap");
		panel.add(LabelPanel.newFramedLabelPanel(segmentationModel.imageLabelingModel(), extensible, true), "grow, wrap");
		panel.add(SegmenterPanel.newFramedSegmeterPanel(segmentationModel, extensible), "grow, wrap");
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

	@Override
	public void close() {
		labelingComponent.close();
	}
}
