package com.indago.tr2d.plugins.seg;

import com.indago.io.ProjectFile;
import com.indago.io.ProjectFolder;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;
import org.scijava.Context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SegmentationModelTest {

	private ImgPlus< ByteType > image = ImgPlus.wrap( ArrayImgs.bytes(2,2,2) );
	private Context context = new Context();
	private ImgPlus< ByteType > bwImage = ImgPlus.wrap( ArrayImgs.bytes(new byte[]{1,0,0,0,0,0,0,0}, 2,2,2) );

	@Test
	public void testSerializeSingleLabeling() throws IOException {
		// setup
		SegmentationModel model = new SegmentationModel(image, context);
		ProjectFolder projectFolder = tmpProjectFolder();
		addPixel(model.labeling(), "foreground", 0, 0, 0);
		// process
		model.save(projectFolder);
		SegmentationModel model2 = SegmentationModel
				.open(image, context, projectFolder);
		// test
		assertEquals(Collections.singleton("foreground"), getPixelNames(model2.labeling(), 0, 0, 0));
	}

	private Set<String> getPixelNames(Labeling labeling, long... position) {
		return getPixel(labeling, position).stream().map(Label::name).collect(
				Collectors.toSet());
	}

	@Test
	public void testFolderCreated() throws IOException {
		// setup
		SegmentationModel model = new SegmentationModel(image, context);
		ProjectFolder projectFolder = tmpProjectFolder();
		model.save(projectFolder);
		ProjectFolder subFolder = projectFolder.addFolder("1");
		assertTrue(subFolder.exists());
		subFolder.loadFiles();
		ProjectFile file = subFolder.getFile("segmenter.labeling");
		assertTrue(file.exists());
	}

	@Test
	public void testSerializeSingleLabeling2() throws IOException {
		// setup
		SegmentationModel model = new SegmentationModel(image, context);
		assertSame(model.labeling(), model.segmenters().get(0).labeling());
		ProjectFolder projectFolder = tmpProjectFolder();
		model.addSegmenter();
		addPixel(model.segmenters().get(0).labeling(), "foreground", 0, 0, 0);
		// process
		model.save(projectFolder);
		SegmentationModel model2 = SegmentationModel
				.open(image, context, projectFolder);
		// test
		assertSame(model2.labeling(), model2.segmenters().get(0).labeling());
		assertEquals(Collections.singleton("foreground"), getPixelNames(model2.segmenters().get(0).labeling(), 0, 0, 0));
	}

	@Test
	public void testSerializeMultipleLabelings() throws IOException {
		// setup
		SegmentationModel model = new SegmentationModel(image, context);
		ProjectFolder projectFolder = tmpProjectFolder();
		model.addSegmenter();
		List< MySegmentationItem > segmenters = model.segmenters();
		addPixel(segmenters.get(0).labeling(), "foreground", 0, 0, 0);
		addPixel(segmenters.get(1).labeling(), "background", 0, 0, 0);
		// process
		model.save(projectFolder);
		SegmentationModel model2 = SegmentationModel
				.open(image, context, projectFolder);
		// test
		assertEquals(Collections.singleton("foreground"), getPixelNames(model2.segmenters().get(0).labeling(), 0, 0, 0));
		assertEquals(Collections.singleton("background"), getPixelNames(model2.segmenters().get(1).labeling(), 0, 0, 0));
	}

	private void addPixel(Labeling labeling, String foreground, long... position)
	{
		getPixel(labeling, position).add(labeling.getLabel(foreground));
	}

	@Test
	public void testSerializeClassifiers() throws IOException {
		// setup
		SegmentationModel model = new SegmentationModel(bwImage, context);
		ProjectFolder projectFolder = tmpProjectFolder();
		train(model.segmenters().get(0));
		// process
		model.save(projectFolder);
		SegmentationModel model2 = SegmentationModel
				.open(bwImage, context, projectFolder);
		// test
		MySegmentationItem item = model2.segmenters().get(0);
		assertTrue(item.segmenter().isTrained());
		testCorrectlyTrained(item);
	}

	@Test
	public void testSerializeUntrainedClassifier() throws IOException {
		ProjectFolder projectFolder = tmpProjectFolder();
		// setup
		SegmentationModel model = new SegmentationModel(bwImage, context);
		model.addSegmenter();
		// process
		model.save(projectFolder);
		SegmentationModel model2 =
				SegmentationModel.open(bwImage, context,
						projectFolder);
		// test
		assertFalse(model2.segmenters().get(0).segmenter().isTrained());
	}

	@Test
	public void testSerialize() throws IOException {
		ProjectFolder projectFolder = tmpProjectFolder();
		// setup
		SegmentationModel model = new SegmentationModel(bwImage, context);
		model.addSegmenter();
		assertEquals(2, model.segmenters().size());
		model.save(projectFolder);
		model = new SegmentationModel(bwImage, context);
		model.save(projectFolder);
		model = SegmentationModel
				.open(bwImage, context, projectFolder);
		assertEquals(1, model.segmenters().size());
	}

	private void train(MySegmentationItem item) {
		final Labeling labeling = item.labeling();
		addPixel(labeling, "foreground", 0, 0, 0);
		addPixel(labeling, "background", 1, 1, 1);
		item.train();
	}

	private void testCorrectlyTrained(MySegmentationItem item) {
		RandomAccessibleInterval< IntType > segmentation = item.getSegmentation();
		assertEquals(1, getPixel(segmentation, 0,0,0).getInteger());
		assertEquals(0, getPixel(segmentation, 1,1,1).getInteger());
	}


	private <T> T getPixel(RandomAccessible<T> image, long... position) {
		RandomAccess< T > ra = image.randomAccess();
		ra.setPosition(position);
		return ra.get();
	}

	private ProjectFolder tmpProjectFolder() throws IOException {
		Path folder = Files.createTempDirectory(null);
		return new ProjectFolder("Test", folder.toFile());
	}

}
