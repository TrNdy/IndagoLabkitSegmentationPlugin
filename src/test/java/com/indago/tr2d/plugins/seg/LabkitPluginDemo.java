
package com.indago.tr2d.plugins.seg;

import com.indago.io.DoubleTypeImgLoader;
import com.indago.io.ProjectFolder;
import com.indago.plugins.seg.IndagoSegmentationPlugin;
import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.img.display.imagej.ImageJFunctions;
import org.scijava.Context;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class LabkitPluginDemo {

	public static void main(final String... args) throws IOException {
		final File path = openDialog();
		final File folder = new File(path,  "segmentation");
		final ImgPlus image = VirtualStackAdapter
				.wrap(new ImagePlus( new File( path, "raw.tif").getAbsolutePath() ));
		demo(folder, image);
	}

	private static File openDialog() {
		JFileChooser dialog = new JFileChooser();
		dialog.setDialogTitle("Select Tr2d / Metaseg directory");
		dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		dialog.showOpenDialog(null);
		return dialog.getSelectedFile();
	}

	private final IndagoSegmentationPlugin plugin;

	public static LabkitPluginDemo demo(File folder, ImgPlus image) throws IOException
	{
		return new LabkitPluginDemo(initPlugin(folder, image));
	}

	private LabkitPluginDemo(IndagoSegmentationPlugin plugin) throws IOException {
		this.plugin = plugin;
		final JFrame frame = setupFrame();
		frame.add(this.plugin.getInteractionPanel());
		frame.add(initBottomPanel(), BorderLayout.PAGE_END);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {

			@Override public void windowClosing(final WindowEvent windowEvent) {
				try {
					plugin.close();
				}
				catch (final Exception e) {
					e.printStackTrace();
				}
				super.windowClosed(windowEvent);
			}
		});
	}

	private static IndagoSegmentationPlugin initPlugin(File folder, ImgPlus image) throws IOException {
		final IndagoSegmentationPlugin plugin = new IndagoLabkitPlugin();
		new Context().inject(plugin);
		final ProjectFolder projectFolder = new ProjectFolder( "TEST",
				folder);
		plugin.setProjectFolderAndData( projectFolder, DoubleTypeImgLoader.wrapEnsureType(image) );
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
