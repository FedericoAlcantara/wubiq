package net.sf.wubiq.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.lang.reflect.Field;

import net.sf.wubiq.enums.PrinterType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Fonts chosen for dot-matrix printers on Windows.
 */
public class GraphicsUtilsTest {

	@Before
	public void setUp() throws Exception {
		setOsName("Windows 10");
	}

	@After
	public void tearDown() throws Exception {
		setOsName(null);
	}

	@Test
	public void windowsDotMatrixFontIsNotScaled() {
		Font font = GraphicsUtils.INSTANCE.properFont(new Font("Arial", Font.PLAIN, 12), PrinterType.DOT_MATRIX);
		assertTrue(font.getTransform().isIdentity());
	}

	@Test
	public void windowsDotMatrixFontHasNoTracking() {
		Font font = GraphicsUtils.INSTANCE.properFont(new Font("Arial", Font.PLAIN, 12), PrinterType.DOT_MATRIX);
		assertNull(font.getAttributes().get(TextAttribute.TRACKING));
		assertEquals(12, font.getSize());
		assertEquals("SansSerif", font.getFamily());
	}

	@Test
	public void courierNewBecomesMonospacedUnscaled() {
		Font font = GraphicsUtils.INSTANCE.properFont(new Font("Courier New", Font.PLAIN, 12), PrinterType.DOT_MATRIX_HQ);
		assertEquals("Monospaced", font.getName());
		assertTrue(font.getTransform().isIdentity());
	}

	@Test
	public void unmappedFontIsKeptUnscaled() {
		Font font = GraphicsUtils.INSTANCE.properFont(new Font("Wingdings", Font.PLAIN, 12), PrinterType.DOT_MATRIX);
		assertEquals("Wingdings", font.getName());
		assertEquals(12, font.getSize());
		assertTrue(font.getTransform().isIdentity());
	}

	private void setOsName(String osName) throws Exception {
		Field field = GraphicsUtils.class.getDeclaredField("osName");
		field.setAccessible(true);
		field.set(GraphicsUtils.INSTANCE, osName);
	}
}
