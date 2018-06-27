
package com.indago.tr2d.plugins.seg;

import com.indago.io.ProjectFolder;
import com.indago.tr2d.ui.model.Tr2dModel;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.Context;
import org.scijava.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LabkitPanel {

	private final Logger log;
	private SegmentationModel segmentationModel;
	private ProjectFolder folder;
	private final SegmentationComponent segmentation;

	public LabkitPanel(Context context, Tr2dModel model, Logger log) {
		this.log = log;
		segmentation = createSegmentationComponent(context, model);
	}

	private SegmentationComponent createSegmentationComponent(Context context,
		Tr2dModel model)
	{
		try {
			try {
				folder = model.getProjectFolder().addFolder("labkit_segmentation");
				if(folder.exists())
					segmentationModel =
							SegmentationModel.open(model.getRawData(),
									context, folder);
			} catch(IOException e) {
				log.warn("Tr2dLabkitSegmentationPlugin: Failed to load previous settings.", e);
			}
			if(segmentationModel == null)
				segmentationModel = new SegmentationModel(model.getRawData(), context);
			return new SegmentationComponent(segmentationModel);
		}
		catch (NoClassDefFoundError e) {
			return null;
		}
	}

	public JPanel getPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		if (isUsable()) panel.add(segmentation.getComponent());
		return panel;
	}

	public boolean isUsable() {
		return segmentation != null;
	}

	public List<RandomAccessibleInterval<IntType>> getOutputs() {
		try {
			segmentationModel.save(folder);
		}
		catch (IOException e) {
			log.warn("Tr2dLabkitSegmentationPlugin: Failed to current settings. ", e);
		}
		return isUsable() ? segmentation.getSegmentations()
				: Collections.emptyList();
	}
}
