package program;

import java.time.LocalDateTime;

public class Transaction {
    private TransactionType type;    // тип: доход или расход
    private String category;         // категория (например, "Еда", "Развлечения" и т.д.)
    private double amount;           // сумма
    private LocalDateTime dateTime;  // дата и время операции

    // Для корректной де/сериализации JSON объявляем пустой конструктор
    public Transaction() {
    }

    public Transaction(TransactionType type, String category, double amount) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.dateTime = LocalDateTime.now();
    }

    public TransactionType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}