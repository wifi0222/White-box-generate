import org.junit.Test;
import static org.junit.Assert.*;

public class Add_test {

    @Test
    public void testAddBothOne() {
        Add adder = new Add();
        assertEquals(2, adder.add(1, 1));
    }

    @Test
    public void testAddFirstNotOne() {
        Add adder = new Add();
        assertEquals(0, adder.add(2, 1));
    }

    @Test
    public void testAddSecondNotOne() {
        Add adder = new Add();
        assertEquals(0, adder.add(1, 2));
    }

    @Test
    public void testAddNeitherOne() {
        Add adder = new Add();
        assertEquals(0, adder.add(2, 2));
    }

    @Test
    public void testAddNegative() {
        Add adder = new Add();
        assertEquals(0, adder.add(-1, 1));
    }

    @Test
    public void testAddZero() {
        Add adder = new Add();
        assertEquals(0, adder.add(0, 1));
    }
}