import java.util.Scanner;

public class Adder {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int num1 = getValidNumber(scanner, "请输入第一个大于 0 小于 10 的整数: ");
        int num2 = getValidNumber(scanner, "请输入第二个大于 0 小于 10 的整数: ");

        int sum = num1 + num2;
        System.out.println("两数之和为: " + sum);

        scanner.close();
    }

    private static int getValidNumber(Scanner scanner, String prompt) {
        int number;
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                number = scanner.nextInt();
                if (number > 0 && number < 10) {
                    break;
                } else {
                    System.out.println("输入的数字必须大于 0 且小于 10，请重新输入。");
                }
            } else {
                System.out.println("输入无效，请输入一个整数。");
                scanner.next();
            }
        }
        return number;
    }
}    