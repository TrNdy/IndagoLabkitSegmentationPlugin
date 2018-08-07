
package com.indago.tr2d.plugins.seg;

import com.indago.io.ProjectFolder;
import com.indago.tr2d.io.projectfolder.Tr2dProjectFolder;
import com.indago.tr2d.ui.model.Tr2dModel;
import ij.ImagePlus;
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

	private final Tr2dSegmentationPlugin plugin;

	public static void main(String... args) throws IOException {
		new LabkitPluginDemo();
	}

	private LabkitPluginDemo() throws IOException {
		plugin = initPlugin();
		JFrame frame = setupFrame();
		frame.add(plugin.getInteractionPanel());
		frame.add(initBottomPanel(), BorderLayout.PAGE_END);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {

			@Override public void windowClosing(WindowEvent windowEvent) {
				try {
					if(plugin instanceof AutoCloseable)
						((AutoCloseable) plugin).close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				super.windowClosed(windowEvent);
			}
		});
	}

	private static Tr2dSegmentationPlugin initPlugin() throws IOException {
		Tr2dSegmentationPlugin plugin = new LabkitPlugin();
		new Context().inject(plugin);
		Tr2dModel tr2dModel = getTr2dModel();
		plugin.setTr2dModel(tr2dModel);
		return plugin;
	}

	private Component initBottomPanel() {
		return new JButton(new RunnableAction("Show Outputs", this::showOutputs));
	}

	private void showOutputs() {
		plugin.getOutputs().forEach(ImageJFunctions::show);
	}

	private static Tr2dModel getTr2dModel() throws IOException {
		String path = "/home/arzt/Documents/Notes/Tr2d/Project";
		Tr2dProjectFolder projectFolder = new Tr2dProjectFolder(new File(path));
		projectFolder.initialize();
		ImagePlus imagePlus = new ImagePlus(path + "/raw.tif");
		return new Tr2dModel(projectFolder, imagePlus);
	}

	private static JFrame setupFrame() {
		JFrame frame = new JFrame("Tr2d Labkit Segmentation Plugin Demo");
		frame.setSize(1000, 1000);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}

}
