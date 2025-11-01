package respawnmarket;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        LoginService service = new LoginService();

        System.out.print("Enter email/username: ");
        String user = scanner.nextLine();

        System.out.print("Enter password: ");
        String pass = scanner.nextLine();

        boolean ok = service.login(user, pass);
        if (ok) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid credentials!");
        }
    }
}
