package net.sf.wubiq.android;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
    public void usesStarMicronics_looksAtTheConfiguredDrivers() {
        Map<String, Object> preferences = new HashMap<String, Object>();
        assertFalse(PrintClientUtils.INSTANCE.usesStarMicronics(preferences));

        preferences.put("wubiq-android-bt_00:11:22:33:44:55", "Zebra MZ320 3 in");
        preferences.put("wubiq-android-bt_00:11:22:33:44:66", "Generic 3 in");
        preferences.put("wubiq-android-bt_00:11:22:33:44:77", "--");
        preferences.put("print_pause_between_copies", 2000);
        assertFalse(PrintClientUtils.INSTANCE.usesStarMicronics(preferences));

        preferences.put("wubiq-android-bt_00:15:0E:E4:26:C8", "Star Micronics 3 in");
        assertTrue(PrintClientUtils.INSTANCE.usesStarMicronics(preferences));
    }

    @Test
    public void usesStarMicronics_ignoresTheTestDevice() {
        Map<String, Object> preferences = new HashMap<String, Object>();
        preferences.put("wubiq-android-bt_test_address", "test device");
        assertFalse(PrintClientUtils.INSTANCE.usesStarMicronics(preferences));
    }
}
