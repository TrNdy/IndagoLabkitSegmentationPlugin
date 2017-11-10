/**
 *
 */
package com.indago.tr2d.plugins.seg;

import com.indago.tr2d.ui.model.Tr2dModel;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.List;

/**
 * @author Matthias Arzt
 */
@Plugin( type = Tr2dSegmentationPlugin.class, name = "Tr2d Labkit Segmentation" )
public class LabkitPlugin implements Tr2dSegmentationPlugin {

	private LabkitPanel panel;

	public static Logger log = LoggerFactory.getLogger( LabkitPlugin.class );

	@Override
	public JPanel getInteractionPanel() {
		return panel.getPanel();
	}

	@Override
	public List< RandomAccessibleInterval< IntType > > getOutputs() {
		return panel.getOutputs();
	}

	@Override
	public void setTr2dModel( final Tr2dModel model ) {
		this.panel = new LabkitPanel(model, log);
	}

	@Override
	public String getUiName() {
		return "labkit segmentation";
	}

	@Override
	public Logger getLogger() {
		return log;
	}
}
