package program;

import program.FinanceController;
import program.WalletService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Подключаем список пользователей из JSON файла
        UserService userService = new UserService("users.json");
        AuthController authController = new AuthController(userService);

        Scanner scanner = new Scanner(System.in);
        String command;

        // Выводим главное меню
        do {
            System.out.println("\n=== Главное меню ===");
            System.out.println("1. Регистрация");
            System.out.println("2. Авторизация");
            System.out.println("3. Выход");
            System.out.print("Введите команду: ");
            command = scanner.nextLine().trim();

            switch (command) {
                case "1":
                    authController.registerUser();
                    // Если успешно зарегистрированы и currentUser не null — сразу переходим в меню кошелька
                    if (authController.getCurrentUser() != null) {
                        openFinanceMenu(authController);
                    }
                    break;
                case "2":
                    authController.loginUser();
                    if (authController.getCurrentUser() != null) {
                        openFinanceMenu(authController);
                    }
                    break;
                case "3":
                    System.out.println("Выход из приложения.");
                    break;
                default:
                    System.out.println("Неизвестная команда!");
            }
        } while (!command.equals("3"));

        scanner.close();
    }

    private static void openFinanceMenu(AuthController authController) {
        String uuid = authController.getCurrentUser().getUuid();
        String walletFile = "wallets/wallet_" + uuid + ".json";

        WalletService walletService = new WalletService(walletFile);
        FinanceController controller = new FinanceController(walletService);

        // запускаем меню
        controller.startFinanceLoop();

        // после выхода из меню — делаем logout
        authController.logoutUser();
    }
}