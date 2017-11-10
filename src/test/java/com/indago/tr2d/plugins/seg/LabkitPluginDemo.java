package com.indago.tr2d.plugins.seg;

import com.indago.io.ProjectFolder;
import com.indago.tr2d.io.projectfolder.Tr2dProjectFolder;
import com.indago.tr2d.ui.model.Tr2dModel;
import ij.ImagePlus;
import org.scijava.Context;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class LabkitPluginDemo {

	public static void main(String... args) throws IOException {
		Tr2dSegmentationPlugin plugin = new LabkitPlugin();
		Context context = new Context();
		context.inject(plugin);
		plugin.setTr2dModel(setupTr2dModel());
		JFrame frame = setupFrame();
		frame.add(plugin.getInteractionPanel());
		frame.setVisible(true);
	}

	private static Tr2dModel setupTr2dModel() throws IOException {
		String path = "/home/arzt/Documents/Notes/Tr2d/Project";
		ProjectFolder projectFolder = new Tr2dProjectFolder(new File(path));
		ImagePlus imagePlus = new ImagePlus(path + "/raw.tif");
		return new Tr2dModel(projectFolder, imagePlus);
	}

	private static JFrame setupFrame() {
		JFrame frame = new JFrame("Tr2d Labkit Segmentation Plugin Demo")	;
		frame.setSize(1000, 1000);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}

}
