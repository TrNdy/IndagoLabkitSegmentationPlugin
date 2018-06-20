
package com.indago.tr2d.plugins.seg;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import net.imglib2.labkit.panel.SegmenterPanel;
import org.scijava.Context;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ThresholdButton {

	private final SegmentationModel segmentationModel;

	private final JButton button;

	public ThresholdButton(SegmentationModel segmentationModel) {
		this.segmentationModel = segmentationModel;
		segmentationModel.selectedSegmenter().notifier().add(
			ignore -> updateThresholds());
		button = new JButton("Thresholds ...");
		button.addActionListener(a -> {
			String text = JOptionPane.showInputDialog(null, "Enter thresholds",
				"Thresholds ...", JOptionPane.PLAIN_MESSAGE);
			if (text != null) try {
				MySegmentationItem segmentationItem = this.segmentationModel
					.selectedSegmenter().get();
				segmentationItem.thresholds().set(new ListOfDoubleFormatter()
					.stringToValue(text));
			}
			catch (NumberFormatException ignore) {}
			updateThresholds();
		});
	}

	public JComponent getComponent() {
		return button;
	}

	private void updateThresholds() {
		List<Double> doubles = segmentationModel.selectedSegmenter().get()
			.thresholds().get();
		button.setText("Thresholds: " + new ListOfDoubleFormatter().valueToString(
			doubles));
	}

	private static class ListOfDoubleFormatter extends
		JFormattedTextField.AbstractFormatter
	{

		@Override
		public List<Double> stringToValue(String text) {
			return Stream.of(text.split(";")).map(Double::new).collect(Collectors
				.toList());
		}

		@Override
		public String valueToString(Object value) {
			if (value == null) return "";
			@SuppressWarnings("unchecked")
			List<Double> list = (List<Double>) value;
			StringJoiner joiner = new StringJoiner("; ");
			list.stream().map(Object::toString).forEach(joiner::add);
			return joiner.toString();
		}
	}

	public static void main(String... args) {
		SegmentationModel segmentationModel = new SegmentationModel(
			new DefaultInputImage(ArrayImgs.unsignedBytes(100, 100, 100)),
			new Context());
		JFrame frame = new JFrame();
		frame.add(new SegmenterPanel(segmentationModel, new ActionMap())
			.getComponent());
		frame.add(new ThresholdButton(segmentationModel).getComponent(),
			BorderLayout.PAGE_END);
		frame.setSize(500, 500);
		frame.setVisible(true);
		segmentationModel.selectedSegmenter().notifier().add(System.out::println);
		segmentationModel.selectedSegmenter().get().thresholds().notifier().add(
			x -> System.out.println(Arrays.toString(x.toArray())));
	}
}
