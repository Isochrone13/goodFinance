package program;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FinanceController {
    private final WalletService walletService;
    private final Scanner scanner;

    public FinanceController(WalletService walletService) {
        this.walletService = walletService;
        this.scanner = new Scanner(System.in);
    }

    /*
    Основной цикл чтения команд пользователя.
    Вызывается из Main после успешной авторизации.
    */
    public void startFinanceLoop() {
        String command;
        do {
            printMenu();
            command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "1": // Создать категорию
                    createCategory();
                    break;
                case "2": // Добавить доход
                    addIncome();
                    break;
                case "3": // Добавить расход
                    addExpense();
                    break;
                case "4": // Установить/обновить бюджет
                    setCategoryBudget();
                    break;
                case "5": // Показать сведения
                    showInformation();
                    break;
                case "6": // Выходим
                    System.out.println("Выходим из меню управления финансами...");
                    break;
                default:
                    System.out.println("Неизвестная команда. Повторите ввод.");
            }
            // Сразу сохраняем после любой операции
            walletService.saveWalletToFile();

        } while (!command.equals("6"));
    }

    private void printMenu() {
        System.out.println("\n=== Меню управления финансами ===");
        System.out.println("1. Создать категорию");
        System.out.println("2. Добавить доход");
        System.out.println("3. Добавить расход");
        System.out.println("4. Установить/обновить бюджет по категории");
        System.out.println("5. Показать сведения (общие или по категориям)");
        System.out.println("6. Вернуться назад");
        System.out.print("Введите команду: ");
    }

    /*
    Создание новой категории
    */
    private void createCategory() {
        System.out.print("Введите название новой категории: ");
        String cat = scanner.nextLine().trim();
        walletService.addCategory(cat);
    }

    /*
    Добавление дохода (с выбором категории из уже существующих).
    */
    private void addIncome() {
        showExistingCategories();
        System.out.print("Введите категорию для дохода: ");
        String category = scanner.nextLine().trim();

        System.out.print("Введите сумму дохода: ");
        double amount = readDouble();

        walletService.addTransaction(TransactionType.income, category, amount);
    }

    /*
    Добавить расход (с выбором существующей категории).
    */
    private void addExpense() {
        showExistingCategories();
        System.out.print("Введите категорию для расхода: ");
        String category = scanner.nextLine().trim();

        System.out.print("Введите сумму расхода: ");
        double amount = readDouble();

        walletService.addTransaction(TransactionType.expense, category, amount);
    }

    /*
    Установить (обновить) бюджет для категории.
    */
    private void setCategoryBudget() {
        showExistingCategories();
        System.out.print("Введите название категории: ");
        String category = scanner.nextLine();

        System.out.print("Введите лимит бюджета по категории: ");
        double limit = readDouble();

        walletService.setBudget(category, limit);
    }

    /*
    Показать сведения:
    - спросить у пользователя: "1" = все сведения, "2" = по категориям
    - если "все" — вызываем walletService.printSummaryAll()
    - если "по категориям" — просим список категорий и вызываем printSummaryByCategories(...)
    */
    private void showInformation() {
        System.out.println("Что вывести?\n1. Все сведения\n2. По выбранным категориям");
        String choice = scanner.nextLine().trim();

        if ("1".equals(choice)) {
            walletService.printSummaryAll();
        } else if ("2".equals(choice)) {
            System.out.println("Введите категории через запятую (например: Еда,Развлечения):");
            String catsStr = scanner.nextLine().trim();
            if (catsStr.isEmpty()) {
                System.out.println("Ошибка: не введено ни одной категории.");
                return;
            }
            // Разбиваем по запятой
            String[] arr = catsStr.split(",");
            List<String> catList = new ArrayList<>();
            for (String s : arr) {
                catList.add(s.trim());
            }
            // Вызываем метод printSummaryByCategories
            walletService.printSummaryByCategories(catList);
        } else {
            System.out.println("Неизвестный выбор.");
        }
    }

    /*
    Показать (в консоли) уже существующие категории.
    */
    private void showExistingCategories() {
        System.out.println("Существующие категории:");
        for (String c : walletService.getAllCategories()) {
            System.out.println(" - " + c);
        }
    }

    /*
    Метод для безопасного чтения double из консоли.
    */
    private double readDouble() {
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: введите корректное число.");
            return 0;
        }
    }
}
