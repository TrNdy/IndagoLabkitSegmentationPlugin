
package com.indago.tr2d.plugins.seg;

import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;

public class SegmentationComponentDemo {

	private final SegmentationComponent segmenter;

	public static void main(String... args) {
		new SegmentationComponentDemo();
	}

	private SegmentationComponentDemo() {
		JFrame frame = setupFrame();
		Img<? extends NumericType<?>> image = ImageJFunctions.wrap(new ImagePlus(
			"/home/arzt/Documents/Notes/Tr2d/ProjectFiles/raw.tif"));
		Context context = new Context();
		segmenter = new SegmentationComponent(context, image);
		frame.add(segmenter.getComponent());
		frame.add(getBottomPanel(), BorderLayout.PAGE_END);
		frame.setVisible(true);
	}

	private JPanel getBottomPanel() {
		JButton segmentation = new JButton(new RunnableAction("Show Result",
			this::showSegmentation));
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout());
		panel.add(segmentation);
		return panel;
	}

	private void showSegmentation() {
		for (RandomAccessibleInterval<IntType> segmentation : segmenter
			.getSegmentations())
		{
			Views.iterable(segmentation).forEach(x -> x.mul(50));
			ImageJFunctions.show(segmentation);
		}
	}

	private static JFrame setupFrame() {
		JFrame frame = new JFrame();
		frame.setSize(500, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}
}
