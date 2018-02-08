/**
 *
 */
package com.indago.tr2d.plugins.seg;

import com.indago.IndagoLog;
import com.indago.tr2d.ui.model.Tr2dModel;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.Context;
import org.scijava.log.Logger;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;
import java.util.List;

/**
 * @author Matthias Arzt
 */
@Plugin( type = Tr2dSegmentationPlugin.class, name = "Tr2d Labkit Segmentation" )
public class LabkitPlugin implements Tr2dSegmentationPlugin {

	@Parameter
	private Context context;

	private LabkitPanel panel;

	public Logger log = IndagoLog.stdLogger().subLogger("Tr2dLabkitPlugin");

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
		this.panel = new LabkitPanel(context, model, log);
	}

	@Override
	public String getUiName() {
		return "labkit segmentation";
	}

	@Override
	public void setLogger(Logger logger) {
		log = logger;
	}

	@Override
	public boolean isUsable() {
		return panel.isUsable();
	}
}
