import org.junit.Test;
import static org.junit.Assert.*;

public class BitField_test {

    @Test
    public void testGetValue() {
        BitField bitField = new BitField(0b001100);
        assertEquals(3, bitField.getValue(0b101100));
    }

    @Test
    public void testGetShortValue() {
        BitField bitField = new BitField(0b001100);
        assertEquals(3, bitField.getShortValue((short) 0b101100));
    }

    @Test
    public void testGetRawValue() {
        BitField bitField = new BitField(0b001100);
        assertEquals(0b001100, bitField.getRawValue(0b101100));
    }

    @Test
    public void testGetShortRawValue() {
        BitField bitField = new BitField(0b001100);
        assertEquals(0b001100, bitField.getShortRawValue((short) 0b101100));
    }

    @Test
    public void testIsSet() {
        BitField bitField = new BitField(0b001100);
        assertTrue(bitField.isSet(0b101100));
        assertFalse(bitField.isSet(0b100000));
    }

    @Test
    public void testIsAllSet() {
        BitField bitField = new BitField(0b001100);
        assertTrue(bitField.isAllSet(0b101100));
        assertFalse(bitField.isAllSet(0b100100));
    }

    @Test
    public void testSetValue() {
        BitField bitField = new BitField(0b001100);
        assertEquals(0b1001100, bitField.setValue(0b1000000, 0b1100));
    }

    @Test
    public void testSetShortValue() {
        BitField bitField = new BitField(0b001100);
        assertEquals(0b1001100, bitField.setShortValue((short) 0b1000000, (short) 0b1100));
    }

    @Test
    public void testClear() {
        BitField bitField = new BitField(0b001100);
        assertEquals(0b1000000, bitField.clear(0b1001100));
    }

    @Test
    public void testClearShort() {
        BitField bitField = new BitField(0b001100);
        assertEquals(0b1000000, bitField.clearShort((short) 0b1001100));
    }

    @Test
    public void testClearByte() {
        BitField bitField = new BitField(0b001100);
        assertEquals(0b1000000, bitField.clearByte((byte) 0b1001100));
    }

    @Test
    public void testSet() {
        BitField bitField = new BitField(0b001100);
        assertEquals(0b1001100, bitField.set(0b1000000));
    }

    @Test
    public void testSetShort() {
        BitField bitField = new BitField(0b001100);
        assertEquals(0b1001100, bitField.setShort((short) 0b1000000));
    }

    @Test
    public void testSetByte() {
        BitField bitField = new BitField(0b001100);
        assertEquals(0b1001100, bitField.setByte((byte) 0b1000000));
    }

    @Test
    public void testSetBoolean() {
        BitField bitField = new BitField(0b001100);
        assertEquals(0b1001100, bitField.setBoolean(0b1000000, true));
        assertEquals(0b1000000, bitField.setBoolean(0b1001100, false));
    }

    @Test
    public void testSetShortBoolean() {
        BitField bitField = new BitField(0b001100);
        assertEquals(0b1001100, bitField.setShortBoolean((short) 0b1000000, true));
        assertEquals(0b1000000, bitField.setShortBoolean((short) 0b1001100, false));
    }

    @Test
    public void testSetByteBoolean() {
        BitField bitField = new BitField(0b001100);
        assertEquals(0b1001100, bitField.setByteBoolean((byte) 0b1000000, true));
        assertEquals(0b1000000, bitField.setByteBoolean((byte) 0b1001100, false));
    }
}