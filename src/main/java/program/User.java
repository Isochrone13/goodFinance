package program;

// Класс пользователя
public class User {
    // Объявляем типы полей класса: UUID, логин, пароль
    private final String uuid;
    private final String login;
    private final String password;

    /*
    Объявляем конструктор класса, который инициализирует поля значениями,
    переданными при создании объекта.
    */
    public User(String uuid, String login, String password) {
        this.uuid = uuid;
        this.login = login;
        this.password = password;
    }

    // Объявляем геттеры для доступа к полям пользователя
    public String getUuid() {
        return uuid;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
