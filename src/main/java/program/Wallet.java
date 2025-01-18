package program;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Wallet {
    // Все транзакции пользователя
    private List<Transaction> transactions;
    // Карта "категория -> бюджет (лимит)"
    private Map<String, Double> budgets;
    // Набор доступных категорий
    private Set<String> categories;

    // Для JSON-сериализации нужен конструктор без параметров
    public Wallet() {
        this.transactions = new ArrayList<>();
        this.budgets = new HashMap<>();
        this.categories = new HashSet<>();
    }

    // Геттеры/сеттеры
    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Map<String, Double> getBudgets() {
        return budgets;
    }

    public void setBudgets(Map<String, Double> budgets) {
        this.budgets = budgets;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }
}