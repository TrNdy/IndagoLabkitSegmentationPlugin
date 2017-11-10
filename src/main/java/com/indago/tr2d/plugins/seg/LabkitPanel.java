package com.indago.tr2d.plugins.seg;

import com.indago.tr2d.ui.model.Tr2dModel;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.IntType;
import org.slf4j.Logger;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public class LabkitPanel {

	private final JPanel panel = new JPanel();
	private final Logger log;
	private final Tr2dModel model;

	public LabkitPanel(Tr2dModel model, Logger log) {
		this.log = log;
		this.model = model;
	}

	public JPanel getPanel() {
		return panel;
	}

	public List<RandomAccessibleInterval<IntType>> getOutputs() {
		return Collections.emptyList();
	}
}
