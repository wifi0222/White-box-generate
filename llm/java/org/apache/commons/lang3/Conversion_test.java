package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

public class Conversion_test {

    @Test
    public void testHexDigitToInt() {
        assertEquals(1, Conversion.hexDigitToInt('1'));
        assertEquals(0, Conversion.hexDigitToInt('0'));
        assertEquals(15, Conversion.hexDigitToInt('f'));
        assertEquals(10, Conversion.hexDigitToInt('a'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHexDigitToIntInvalid() {
        Conversion.hexDigitToInt('G');
    }

    @Test
    public void testHexDigitMsb0ToInt() {
        assertEquals(8, Conversion.hexDigitMsb0ToInt('1'));
        assertEquals(0, Conversion.hexDigitMsb0ToInt('0'));
        assertEquals(15, Conversion.hexDigitMsb0ToInt('f'));
        assertEquals(10, Conversion.hexDigitMsb0ToInt('a'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHexDigitMsb0ToIntInvalid() {
        Conversion.hexDigitMsb0ToInt('G');
    }

    @Test
    public void testHexDigitToBinary() {
        assertArrayEquals(new boolean[]{true, false, false, false}, Conversion.hexDigitToBinary('1'));
        assertArrayEquals(new boolean[]{false, false, false, false}, Conversion.hexDigitToBinary('0'));
        assertArrayEquals(new boolean[]{true, true, true, true}, Conversion.hexDigitToBinary('f'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHexDigitToBinaryInvalid() {
        Conversion.hexDigitToBinary('G');
    }

    @Test
    public void testBinaryToHexDigit() {
        assertEquals('1', Conversion.binaryToHexDigit(new boolean[]{true, false, false, false}));
        assertEquals('0', Conversion.binaryToHexDigit(new boolean[]{false, false, false, false}));
        assertEquals('f', Conversion.binaryToHexDigit(new boolean[]{true, true, true, true}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBinaryToHexDigitInvalid() {
        Conversion.binaryToHexDigit(new boolean[]{});
    }

    // Add more tests for other methods...
}