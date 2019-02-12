package com.indago.tr2d.plugins.seg;

import com.google.common.io.Files;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.Img;
import net.imglib2.test.RandomImgs;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import java.io.IOException;
import java.util.stream.Stream;

public class DifferentDimensionsTest {

	public static void main(String... args) throws IOException {
		demo(Axes.X, Axes.Y);
//		demo(Axes.X, Axes.Y, Axes.Z);
//		demo(Axes.X, Axes.Y, Axes.TIME);
//		demo(Axes.X, Axes.Y, Axes.Z, Axes.TIME);
	}

	public static void demo(AxisType... axes) throws IOException {
		final long[] dims = Stream.of(axes).mapToLong(ignore -> 100).toArray();
		Img< UnsignedShortType > image =
				RandomImgs.seed(42).nextImage(new UnsignedShortType(), dims);
		final ImgPlus< UnsignedShortType > imgPlus =
				new ImgPlus<>(image, "title", axes);
		LabkitPluginDemo.demo(Files.createTempDir(), imgPlus);
	}

}
