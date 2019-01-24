/**
 *
 */

package com.indago.tr2d.plugins.seg;

import java.util.List;

import javax.swing.JPanel;

import org.scijava.Context;
import org.scijava.log.Logger;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import com.indago.IndagoLog;
import com.indago.io.ProjectFolder;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * @author Matthias Arzt
 */
@Plugin( type = IndagoSegmentationPlugin.class, name = "Indago Labkit Segmentation" )
public class IndagoLabkitPlugin implements IndagoSegmentationPlugin, AutoCloseable {

	@Parameter
	private Context context;

	private LabkitPanel panel;

	public Logger log = IndagoLog.stdLogger().subLogger( "IndagoLabkitPlugin" );

	private ProjectFolder projectFolder;
	private RandomAccessibleInterval< DoubleType > rawData;

	@Override
	public JPanel getInteractionPanel() {
		return panel.getPanel();
	}

	@Override
	public List<RandomAccessibleInterval<IntType>> getOutputs() {
		return panel.getOutputs();
	}

	@Override
	public void setProjectFolderAndData( final ProjectFolder projectFolder, final RandomAccessibleInterval< DoubleType > rawData ) {
		this.projectFolder = projectFolder;
		this.rawData = rawData;
		this.panel = new LabkitPanel( context, this, log );
	}

	@Override
	public String getUiName() {
		return "labkit segmentation";
	}

	@Override
	public void setLogger(final Logger logger) {
		log = logger;
	}

	@Override
	public boolean isUsable() {
		return panel.isUsable();
	}

	@Override
	public void close() {
		panel.close();
	}

	public ProjectFolder getProjectFolder() {
		return this.projectFolder;
	}

	public RandomAccessibleInterval< DoubleType > getRawData() {
		return this.rawData;
	}

}
