
package com.indago.tr2d.plugins.seg;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.scijava.Context;
import org.scijava.ui.behaviour.util.RunnableAction;

import com.indago.io.DoubleTypeImgLoader;
import com.indago.io.ProjectFolder;
import com.indago.plugins.seg.IndagoSegmentationPlugin;

import ij.ImagePlus;
import net.imglib2.img.display.imagej.ImageJFunctions;

public class LabkitPluginDemo {

	private final IndagoSegmentationPlugin plugin;

	public static void main(final String... args) throws IOException {
		new LabkitPluginDemo();
	}

	private LabkitPluginDemo() throws IOException {
		plugin = initPlugin();
		final JFrame frame = setupFrame();
		frame.add(plugin.getInteractionPanel());
		frame.add(initBottomPanel(), BorderLayout.PAGE_END);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {

			@Override public void windowClosing(final WindowEvent windowEvent) {
				try {
					if(plugin instanceof AutoCloseable)
						((AutoCloseable) plugin).close();
				}
				catch (final Exception e) {
					e.printStackTrace();
				}
				super.windowClosed(windowEvent);
			}
		});
	}

	private static IndagoSegmentationPlugin initPlugin() throws IOException {
		final IndagoSegmentationPlugin plugin = new IndagoLabkitPlugin();
		new Context().inject(plugin);

		final String path = "/home/arzt/Documents/Notes/Tr2d/Project";
		final ProjectFolder projectFolder = new ProjectFolder( "TEST", new File( path + "/segmentation" ) );
		final ImagePlus imagePlus = new ImagePlus( path + "/raw.tif" );

		plugin.setProjectFolderAndData( projectFolder, DoubleTypeImgLoader.wrapEnsureType( imagePlus ) );
		return plugin;
	}

	private Component initBottomPanel() {
		return new JButton(new RunnableAction("Show Outputs", this::showOutputs));
	}

	private void showOutputs() {
		plugin.getOutputs().forEach(ImageJFunctions::show);
	}

	private static JFrame setupFrame() {
		final JFrame frame = new JFrame("Tr2d Labkit Segmentation Plugin Demo");
		frame.setSize(1000, 1000);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}

}
