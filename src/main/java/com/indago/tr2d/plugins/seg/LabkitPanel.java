
package com.indago.tr2d.plugins.seg;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import org.scijava.Context;
import org.scijava.log.Logger;

import com.indago.io.ProjectFolder;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.IntType;

public class LabkitPanel implements AutoCloseable {

	private final Logger log;
	private SegmentationModel segmentationModel;
	private ProjectFolder folder;
	private final SegmentationComponent segmentation;

	public LabkitPanel( final Context context, final IndagoLabkitPlugin ilp, final Logger log ) {
		this.log = log;
		segmentation = createSegmentationComponent( context, ilp );
	}

	private SegmentationComponent createSegmentationComponent(final Context context,
			final IndagoLabkitPlugin ilp )
	{
		try {
			try {
				folder = ilp.getProjectFolder().addFolder( "labkit" );
				if(folder.exists())
					segmentationModel =
							SegmentationModel.open(
									ilp.getRawData(),
									context, folder);
			} catch(final IOException e) {
				log.warn("Tr2dLabkitSegmentationPlugin: Failed to load previous settings.", e);
			}
			if(segmentationModel == null)
				segmentationModel = new SegmentationModel( ilp.getRawData(), context );
			return new SegmentationComponent(segmentationModel);
		}
		catch (final NoClassDefFoundError e) {
			return null;
		}
	}

	public JPanel getPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		if (isUsable()) panel.add(segmentation.getComponent());
		return panel;
	}

	public boolean isUsable() {
		return segmentation != null;
	}

	public List<RandomAccessibleInterval<IntType>> getOutputs() {
		if(!isUsable()) return Collections.emptyList();
		final List< RandomAccessibleInterval< IntType > > segmentations =
				segmentationModel.getSegmentations();
		saveSettings();
		return segmentations;
	}

	@Override
	public void close() {
		saveSettings();
	}

	private void saveSettings() {
		if(isUsable())
			try {
				if(folder != null)
					segmentationModel.save(folder);
			}
			catch (final IOException e) {
				log.warn("Tr2dLabkitSegmentationPlugin: Failed to save current settings. ", e);
			}
	}

}
