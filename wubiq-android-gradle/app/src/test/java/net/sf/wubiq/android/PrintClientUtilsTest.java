package net.sf.wubiq.android;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests copies handling in PrintClientUtils.
 */
public class PrintClientUtilsTest {
    private static final String JOB_NAME = "javax.print.attribute.standard.JobName:J:Test page";
    private static final String MEDIA = "javax.print.attribute.standard.MediaSizeName:M:215.9,279.4,na-letter";

    @Test
    public void copies_readsCopiesAttribute() {
        assertEquals(3, PrintClientUtils.INSTANCE.copies(
                "javax.print.attribute.standard.Copies:I:3/" + MEDIA + "/" + JOB_NAME));
        assertEquals(2, PrintClientUtils.INSTANCE.copies(MEDIA + "/javax.print.attribute.standard.Copies:I:2"));
    }

    @Test
    public void copies_readsCompressedAttribute() {
        assertEquals(4, PrintClientUtils.INSTANCE.copies("xCOPx:I:4/" + JOB_NAME));
    }

    @Test
    public void copies_ignoresCopiesSupported() {
        assertEquals(1, PrintClientUtils.INSTANCE.copies(
                "javax.print.attribute.standard.CopiesSupported:S:1,9999/" + JOB_NAME));
    }

    @Test
    public void copies_defaultsToOne() {
        assertEquals(1, PrintClientUtils.INSTANCE.copies(null));
        assertEquals(1, PrintClientUtils.INSTANCE.copies(""));
        assertEquals(1, PrintClientUtils.INSTANCE.copies(MEDIA + "/" + JOB_NAME));
        assertEquals(1, PrintClientUtils.INSTANCE.copies("javax.print.attribute.standard.Copies:I:0"));
        assertEquals(1, PrintClientUtils.INSTANCE.copies("javax.print.attribute.standard.Copies:I:x"));
    }

    @Test
    public void repeat_concatenatesOnePerCopy() {
        byte[] data = {1, 2, 3};
        assertArrayEquals(data, PrintClientUtils.INSTANCE.repeat(data, 1));
        assertArrayEquals(new byte[]{1, 2, 3, 1, 2, 3, 1, 2, 3}, PrintClientUtils.INSTANCE.repeat(data, 3));
        assertArrayEquals(new byte[0], PrintClientUtils.INSTANCE.repeat(new byte[0], 5));
    }
}
