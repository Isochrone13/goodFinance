package program;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class WalletService {

    private Wallet wallet;                 // текущий кошелёк
    private final String walletFilePath;   // путь к JSON-файлу

    public WalletService(String walletFilePath) {
        this.walletFilePath = walletFilePath;
        this.wallet = loadWalletFromFile();
    }

    /*
    Загрузка данных кошелька из JSON-файла.
    Если файла нет или произошла ошибка чтения, возвращаем пустой кошелёк.
    */
    private Wallet loadWalletFromFile() {
        File file = new File(walletFilePath);
        if (!file.exists()) {
            return new Wallet();
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            System.out.println("Не удалось прочитать " + walletFilePath + ": " + e.getMessage());
            return new Wallet();
        }

        try {
            JSONObject root = new JSONObject(sb.toString());
            Wallet w = new Wallet();

            // Считываем транзакции
            if (root.has("transactions")) {
                JSONArray transactions = root.getJSONArray("transactions");
                for (int i = 0; i < transactions.length(); i++) {
                    JSONObject t = transactions.getJSONObject(i);
                    TransactionType type = TransactionType.valueOf(t.getString("type"));
                    String category = t.getString("category");
                    double amount = t.getDouble("amount");
                    LocalDateTime dateTime = LocalDateTime.parse(t.getString("dateTime"));

                    Transaction tx = new Transaction();
                    tx.setType(type);
                    tx.setCategory(category);
                    tx.setAmount(amount);
                    tx.setDateTime(dateTime);

                    w.getTransactions().add(tx);
                }
            }

            // Считываем бюджет
            if (root.has("budgets")) {
                JSONObject budgets = root.getJSONObject("budgets");
                Iterator<String> it = budgets.keys();
                while (it.hasNext()) {
                    String category = it.next();
                    double limit = budgets.getDouble(category);
                    w.getBudgets().put(category, limit);
                }
            }

            // Считываем категории
            if (root.has("categories")) {
                JSONArray cats = root.getJSONArray("categories");
                for (int i = 0; i < cats.length(); i++) {
                    w.getCategories().add(cats.getString(i));
                }
            }

            return w;

        } catch (JSONException e) {
            System.out.println("Ошибка парсинга кошелька: " + e.getMessage());
            return new Wallet();
        }
    }

    /*
    Сохранение кошелька в JSON-файл (org.json).
    */
    public void saveWalletToFile() {
        JSONObject root = new JSONObject();

        // Сохраняем транзакции
        JSONArray transactionsArr = new JSONArray();
        for (Transaction tx : wallet.getTransactions()) {
            JSONObject t = new JSONObject();
            t.put("type", tx.getType().name());
            t.put("category", tx.getCategory());
            t.put("amount", tx.getAmount());
            t.put("dateTime", tx.getDateTime().toString());
            transactionsArr.put(t);
        }
        root.put("transactions", transactionsArr);

        // Сохраняем бюджеты
        JSONObject budgetsObj = new JSONObject();
        for (Map.Entry<String, Double> entry : wallet.getBudgets().entrySet()) {
            budgetsObj.put(entry.getKey(), entry.getValue());
        }
        root.put("budgets", budgetsObj);

        // Сохраняем категории
        JSONArray categoriesArr = new JSONArray();
        for (String cat : wallet.getCategories()) {
            categoriesArr.put(cat);
        }
        root.put("categories", categoriesArr);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(walletFilePath, false))) {
            bw.write(root.toString(2));
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении кошелька: " + e.getMessage());
        }
    }

    /*
    Добавление новой категории ("Еда", "Развлечения" и т.д.).
    */
    public void addCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            System.out.println("Ошибка: категория не может быть пустой!");
            return;
        }
        boolean added = wallet.getCategories().add(category.trim());
        if (!added) {
            System.out.println("Категория \"" + category + "\" уже существует!");
        } else {
            System.out.println("Категория \"" + category + "\" успешно добавлена.");
        }
    }

    /*
    Добавление дохода/расхода в кошелёк.
    Также проверяем, что категория уже существует.
    */
    public void addTransaction(TransactionType type, String category, double amount) {
        if (amount <= 0) {
            System.out.println("Сумма должна быть > 0.");
            return;
        }
        // Проверяем, что категория есть в списке
        if (!wallet.getCategories().contains(category)) {
            System.out.println("Ошибка: категории \"" + category + "\" не существует! " +
                    "Сначала создайте категорию или выберите из доступных.");
            return;
        }

        Transaction transaction = new Transaction(type, category, amount);
        wallet.getTransactions().add(transaction);

        // Если расход — проверяем превышение бюджета
        if (type == TransactionType.expense) {
            Double budgetLimit = wallet.getBudgets().get(category);
            if (budgetLimit != null) {
                double totalExpenseInCategory = calculateExpenseInCategory(category);
                if (totalExpenseInCategory > budgetLimit) {
                    System.out.println("Внимание! Превышен лимит бюджета по категории: " + category);
                }
            }
            // Проверка — общие расходы > общие доходы
            double totalExpense = calculateTotalExpense();
            double totalIncome = calculateTotalIncome();
            if (totalExpense > totalIncome) {
                System.out.println("Внимание! Общие расходы превысили доходы!");
            }
        }
    }

    /*
    Установить (обновить) бюджет для существующей категории.
    */
    public void setBudget(String category, double limit) {
        if (limit < 0) {
            System.out.println("Лимит не может быть отрицательным.");
            return;
        }
        if (!wallet.getCategories().contains(category)) {
            System.out.println("Ошибка: нет такой категории \"" + category + "\"!");
            return;
        }
        wallet.getBudgets().put(category, limit);
    }

    // Подсчёт общих доходов/расходов
    public double calculateTotalIncome() {
        return wallet.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.income)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Подсчёт общих расходов
    public double calculateTotalExpense() {
        return wallet.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.expense)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Подсчёт общих расходов по категории
    public double calculateExpenseInCategory(String category) {
        return wallet.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.expense)
                .filter(t -> t.getCategory().equalsIgnoreCase(category))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // Подсчёт общих доходов по категории
    public double calculateIncomeInCategory(String category) {
        return wallet.getTransactions().stream()
                .filter(t -> t.getType() == TransactionType.income)
                .filter(t -> t.getCategory().equalsIgnoreCase(category))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /*
    Печатает «полную» информацию (по всем категориям), включая
    - Общий доход
    - Доходы по категориям
    - Общий расход
    - Расходы по категориям
    - Бюджет по категориям (и остаток)
    */
    public void printSummaryAll() {
        double totalIncome = calculateTotalIncome();
        double totalExpense = calculateTotalExpense();

        // Общий доход
        System.out.println("Общий доход: " + totalIncome);

        // Доходы по категориям
        Map<String, Double> incomeByCat = new HashMap<>();
        for (Transaction t : wallet.getTransactions()) {
            if (t.getType() == TransactionType.income) {
                incomeByCat.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        System.out.println("Доходы по категориям:");
        for (Map.Entry<String, Double> e : incomeByCat.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }

        // Общий расход
        System.out.println("Общие расходы: " + totalExpense);

        // Расходы по категориям
        Map<String, Double> expenseByCat = new HashMap<>();
        for (Transaction t : wallet.getTransactions()) {
            if (t.getType() == TransactionType.expense) {
                expenseByCat.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        System.out.println("Расходы по категориям:");
        for (Map.Entry<String, Double> e : expenseByCat.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }

        // Бюджет по категориям и остаток
        System.out.println("\nБюджет по категориям:");
        for (String cat : wallet.getCategories()) {
            double limit = wallet.getBudgets().getOrDefault(cat, 0.0);
            double spent = expenseByCat.getOrDefault(cat, 0.0);
            double remaining = limit - spent;
            System.out.printf("%s: %.1f, Остаток: %.1f%n", cat, limit, remaining);
        }
    }

    /*
    Вывести информацию по выбранным категориям (минимум 1),
    включая доход, расходы, бюджеты только по этим категориям.
    */
    public void printSummaryByCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            System.out.println("Нет выбранных категорий для вывода сведений!");
            return;
        }

        double totalIncome = 0;
        double totalExpense = 0;
        Map<String, Double> incomeByCat = new HashMap<>();
        Map<String, Double> expenseByCat = new HashMap<>();

        // Суммируем только транзакции, чьи категории входят в categories
        for (Transaction t : wallet.getTransactions()) {
            if (categories.contains(t.getCategory())) {
                if (t.getType() == TransactionType.income) {
                    totalIncome += t.getAmount();
                    incomeByCat.merge(t.getCategory(), t.getAmount(), Double::sum);
                } else {
                    totalExpense += t.getAmount();
                    expenseByCat.merge(t.getCategory(), t.getAmount(), Double::sum);
                }
            }
        }

        // Общий доход (по выбранным категориям)
        System.out.println("Общий доход (по выбранным категориям): " + totalIncome);
        // Доходы по категориям
        System.out.println("Доходы по категориям:");
        for (String cat : categories) {
            double val = incomeByCat.getOrDefault(cat, 0.0);
            // Печатаем даже если 0, если хотите скрывать — добавьте условие val>0
            System.out.println(cat + ": " + val);
        }

        // Общий расход (по выбранным категориям)
        System.out.println("Общие расходы (по выбранным категориям): " + totalExpense);
        // Расходы по категориям
        System.out.println("Расходы по категориям:");
        for (String cat : categories) {
            double val = expenseByCat.getOrDefault(cat, 0.0);
            System.out.println(cat + ": " + val);
        }

        // Бюджет по выбранным категориям
        System.out.println("\nБюджет по выбранным категориям:");
        for (String cat : categories) {
            double limit = wallet.getBudgets().getOrDefault(cat, 0.0);
            double spent = expenseByCat.getOrDefault(cat, 0.0);
            double remaining = limit - spent;
            System.out.printf("%s: %.1f, Остаток: %.1f%n", cat, limit, remaining);
        }
    }

    // Прочие вспомогательные методы
    public Set<String> getAllCategories() {
        return wallet.getCategories();
    }
}
