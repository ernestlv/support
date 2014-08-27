package com.scrippsnetworks.wcm.image;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RenditionInfoTest {

	private final RenditionInfo renditionInfo;

	public RenditionInfoTest(final RenditionInfo renditionInfo) {
		this.renditionInfo = renditionInfo;
	}

	@Test
	public void testRenditionAspects() {
		Set<ImageAspect> as = renditionInfo.getAspects();
		assertTrue(as.size() > 0);
		Iterator<ImageAspect> iait = as.iterator();
		while (iait.hasNext()) {
			ImageAspect ia = iait.next();
			assertTrue(renditionInfo.hasAspect(ia));
			ImageDimensions id = renditionInfo.getImageDimensions(ia);
			int height = id.getHeight();
			int width = id.getWidth();

            String RA = renditionInfo.name() + "." + ia.name();
			// test the nearest aspect logic
			ImageAspect nearestAspect = renditionInfo.getNearestAspect(width, height);
			assertEquals(RA + " nearest aspect for current dimensions correct", ia.name(), nearestAspect.name());
			// again with dimensions not exact ratio
			nearestAspect = renditionInfo.getNearestAspect(width - 1, height - 1);
			assertEquals(RA + " nearest aspect matches with 1px subtracted", ia.name(), nearestAspect.name());

			// Obviously this should work
			Rectangle rect = id.getCropRect(width, height);
            assertEquals(RA + " crop rect height", height, rect.getHeight(), 0.0);
			assertEquals(RA + " crop rect width", width, rect.getWidth(), 0.0);
			// assertTrue(RA + " crop rect x",rect.getLocation().getX() == 0.0);
			assertEquals(RA + " crop rect x",0.0, rect.getLocation().getX(), 0.0);
			// assertTrue(RA + " crop rect y",rect.getLocation().getY() == 0.0);
			assertEquals(RA + " crop rect y",0.0, rect.getLocation().getY(), 0.0);

			// Adding to either dimension should result a crop of either side
			rect = id.getCropRect(width + 10, height);
            // Allowing 1px "slippage"...resize with straighten that out.
			assertEquals(RA + " w+10 crop rect height", height, rect.getHeight(), 1.0);
			assertEquals(RA + " w+10 crop rect width", width, rect.getWidth(), 1.0);
			// point should be shifted over 5, splitting the difference
			assertEquals(RA + " w+10 crop rect x", 5.0, rect.getLocation().getX(), 0.0);
			assertEquals(RA + " w+10 crop rect y", 0.0, rect.getLocation().getY(), 0.0);

			// Adding to either dimension should result a crop of either side
			rect = id.getCropRect(width - 10, height);
			double h = rect.getHeight();
			double w = rect.getWidth();
			// floating point, ya know...I admit, I pulled this tolerance out of my rear
			assertEquals(RA + " real aspect of crop rect w-10", (w/h), id.aspectRatio(), 0.2);// Math.abs((w/h) - id.aspectRatio()) < 0.02 );
			// If the width of the rect is the same as the value passed to getCropRect
			// and the rect ratio is within the tolerance, we know the height is sane.
			// It's math, yo!
			assertEquals(RA + " width of crop rect same as original", width - 10, w, 0.0); // w == (width - 10));
			// point should be at left in x dimension
			assertEquals(RA + " w-10 rect x", 0.0, rect.getLocation().getX(), 0.0);
			// crop is to height, so the point is shifted halfway in y dimension to center the crop
            assertEquals(RA + " w-10 rect y", Math.round((height - h)/2.0d), rect.getLocation().getY(), .001);

		}
	}

	@Parameters
	public static Collection<Object[]> data() {
		Collection<Object []> data = new ArrayList<Object []>();
		for (RenditionInfo ri : RenditionInfo.values()) {
			data.add(new Object [] {ri});
		}
		return data;
	}
}
