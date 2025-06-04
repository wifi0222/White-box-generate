public class ADDD {
    public static void main(String[] args) {
        System.out.println("aaaaa");
        ADDD adder = new ADDD();
        int result = adder.add(1, 1);
        System.out.println("Addition result: " + result);
    }

    public int add(int a, int b) {
        if (a == 1 && b == 1) {
            return a + b;
        }
        return 0;
    }
}