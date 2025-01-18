package program;

import java.util.Scanner;
import java.util.UUID;


/*
Класс, отвечающий за взаимодействие с пользователем
*/
public class AuthController {
    // Объявляем поля класса
    private final UserService userService; // Cервис для работы с пользователями
    private final Scanner scanner;         // Для чтения ввода пользователя
    private User currentUser;              // Текущий авторизованный пользователь

    // Конструктор класса
    public AuthController(UserService userService) {
        this.userService = userService;
        this.scanner = new Scanner(System.in);
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /*
    Метод для регистрации нового пользователя.
    Пароль генерируется автоматически (6 символов: строчные лат. буквы + цифры).
    */
    public void registerUser() {
        System.out.print("Введите логин (буквы, цифры, '_', '-'): ");
        String login = scanner.nextLine();

        // Проверяем, что логин не пустой и состоит из разрешённых символов
        if (!login.matches("[A-Za-z0-9_-]+")) {
            System.out.println("Логин может содержать только буквы латиницы (A-Z, a-z), " +
                    "цифры (0-9) и символы '_' или '-'.");
            return;
        }

        // Проверяем, что такого логина ещё нет
        if (userService.findByLogin(login) != null) {
            System.out.println("Пользователь с таким логином уже существует!");
            return;
        }

        // Генерация UUID
        String uuid = UUID.randomUUID().toString();
        // Генерация пароля
        String password = generateRandomPassword(6);

        // Создаём и сохраняем пользователя
        User newUser = new User(uuid, login, password);
        userService.addUser(newUser);

        System.out.println("Пользователь создан!");
        System.out.println("Ваш UUID: " + uuid);
        System.out.println("Ваш пароль: " + password);

        // Автоматически авторизуемся
        currentUser = newUser;
        System.out.println("Вы автоматически авторизованы как " + newUser.getLogin());
    }

    /*
    Метод для авторизации пользователя
    */
    public void loginUser() {
        System.out.println("Авторизация:");
        System.out.print("Введите UUID или логин: ");
        String userNameOrUUID = scanner.nextLine();

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        // Сначала ищем по UUID
        User user = userService.findByUuid(userNameOrUUID);
        // Если не нашли – ищем по логину
        if (user == null) {
            user = userService.findByLogin(userNameOrUUID);
        }

        if (user == null) {
            System.out.println("Пользователь не найден!");
            return;
        }

        if (!user.getPassword().equals(password)) {
            System.out.println("Неверный пароль!");
            return;
        }

        // Авторизация успешна
        currentUser = user;
        System.out.println("Добро пожаловать, " + user.getLogin() + "!");
    }

    /*
    Метод выхода из программы
    */
    public void logoutUser() {
        if (currentUser == null) {
            System.out.println("Никто не авторизован.");
            return;
        }
        System.out.println("Пользователь " + currentUser.getLogin() + " вышел из системы.");
        currentUser = null;
    }

    /*
    Метод для генерации случайного пароля из цифр и строчных лат. букв указанной длины.
    */
    private String generateRandomPassword(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}