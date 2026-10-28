package net.sf.wubiq.android.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the printer name detection in BluetoothUtils.
 */
public class BluetoothUtilsTest {

    @Test
    public void isPrinterName_knownBrands() {
        assertTrue(BluetoothUtils.isPrinterName("Star Micronics"));
        assertTrue(BluetoothUtils.isPrinterName("ZEBRA MZ320"));
        assertTrue(BluetoothUtils.isPrinterName("DataMax Apex"));
        assertTrue(BluetoothUtils.isPrinterName("Porti-S"));
        assertTrue(BluetoothUtils.isPrinterName("BT Printer"));
    }

    @Test
    public void isPrinterName_otherDevices() {
        assertFalse(BluetoothUtils.isPrinterName(null));
        assertFalse(BluetoothUtils.isPrinterName(""));
        assertFalse(BluetoothUtils.isPrinterName("JBL Flip 5"));
        assertFalse(BluetoothUtils.isPrinterName("Logitech K380"));
    }
}
