// ArrayOperations.java
import java.util.Arrays;

public class ArrayOperations {

    public int findMax(int[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("数组不能为空");
        }
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    public int[] sortArray(int[] array) {
        if (array == null) {
            return null;
        }
        int[] sortedArray = Arrays.copyOf(array, array.length);
        Arrays.sort(sortedArray);
        return sortedArray;
    }
}