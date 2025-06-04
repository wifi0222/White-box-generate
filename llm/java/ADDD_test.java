import org.junit.Test;
import static org.junit.Assert.*;

public class ADDD_test {
    
    @Test
    public void testAdd() {
        ADDD adder = new ADDD();
        
        // Test case when both a and b are 1
        assertEquals(2, adder.add(1, 1));
        
        // Test case when a is not 1
        assertEquals(0, adder.add(2, 1));
        
        // Test case when b is not 1
        assertEquals(0, adder.add(1, 2));
        
        // Test case when neither a nor b is 1
        assertEquals(0, adder.add(2, 2));
    }
}