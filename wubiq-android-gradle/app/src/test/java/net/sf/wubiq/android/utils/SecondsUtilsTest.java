package net.sf.wubiq.android.utils;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Tests the milliseconds / seconds conversion.
 */
public class SecondsUtilsTest {
    private static final int MAX = 10000;

    @Test
    public void toSeconds_trimsTrailingZeros() {
        assertEquals("2.5", SecondsUtils.toSeconds(2500, Locale.US));
        assertEquals("3", SecondsUtils.toSeconds(3000, Locale.US));
        assertEquals("0", SecondsUtils.toSeconds(0, Locale.US));
        assertEquals("0.3", SecondsUtils.toSeconds(300, Locale.US));
    }

    @Test
    public void toSeconds_usesLocaleSeparator() {
        assertEquals("2,5", SecondsUtils.toSeconds(2500, new Locale("es", "ES")));
    }

    @Test
    public void toMilliseconds_acceptsDotAndComma() {
        assertEquals(2500, SecondsUtils.toMilliseconds("2.5", 1, MAX));
        assertEquals(2500, SecondsUtils.toMilliseconds(" 2,5 ", 1, MAX));
        assertEquals(3000, SecondsUtils.toMilliseconds("3", 1, MAX));
        assertEquals(0, SecondsUtils.toMilliseconds("0", 1, MAX));
    }

    @Test
    public void toMilliseconds_fallsBackToDefault() {
        assertEquals(2500, SecondsUtils.toMilliseconds(null, 2500, MAX));
        assertEquals(2500, SecondsUtils.toMilliseconds("", 2500, MAX));
        assertEquals(2500, SecondsUtils.toMilliseconds(".", 2500, MAX));
        assertEquals(2500, SecondsUtils.toMilliseconds("abc", 2500, MAX));
    }

    @Test
    public void toMilliseconds_negativeIsInvalid() {
        assertEquals(2500, SecondsUtils.toMilliseconds("-4", 2500, MAX));
        assertEquals(2500, SecondsUtils.toMilliseconds("-0.5", 2500, MAX));
    }

    @Test
    public void toMilliseconds_cappedAtMaximum() {
        assertEquals(10000, SecondsUtils.toMilliseconds("10", 2500, MAX));
        assertEquals(10000, SecondsUtils.toMilliseconds("10.5", 2500, MAX));
        assertEquals(10000, SecondsUtils.toMilliseconds("9999999999", 2500, MAX));
    }
}
