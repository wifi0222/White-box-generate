import org.junit.Test;
import static org.junit.Assert.*;

public class Add_test {
    @Test
    public void testAddBothOne() {
        Add adder = new Add();
        assertEquals(2, adder.add(1, 1));
    }

    @Test
    public void testAddFirstOne() {
        Add adder = new Add();
        assertEquals(0, adder.add(1, 0));
    }

    @Test
    public void testAddSecondOne() {
        Add adder = new Add();
        assertEquals(0, adder.add(0, 1));
    }

    @Test
    public void testAddNeitherOne() {
        Add adder = new Add();
        assertEquals(0, adder.add(2, 2));
    }
}