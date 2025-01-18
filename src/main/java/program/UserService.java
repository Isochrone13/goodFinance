package program;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/*
Сервис для работы с пользователями (хранит, ищет, загружает, сохраняет в формате JSON).
*/
public class UserService {
    // Путь к JSON-файлу, где хранятся пользователи
    private final String filePath;
    // Список пользователей в памяти
    private final List<User> users;

    public UserService(String filePath) {
        this.filePath = filePath;
        this.users = new ArrayList<>();
        loadUsersFromFile();
    }

    /*
    Ищет пользователя по логину (игнорируя регистр).
    */
    public User findByLogin(String login) {
        for (User user : users) {
            if (user.getLogin().equalsIgnoreCase(login)) {
                return user;
            }
        }
        return null;
    }

    /*
    Ищет пользователя по UUID.
    */
    public User findByUuid(String uuid) {
        for (User user : users) {
            if (user.getUuid().equals(uuid)) {
                return user;
            }
        }
        return null;
    }

    /*
    Добавляет нового пользователя и сразу сохраняет изменения в файл.
    */
    public void addUser(User user) {
        users.add(user);
        saveUsersToFile();
    }

    /*
    Загружает пользователей из JSON-файла.
    */
    private void loadUsersFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            // Файл не найден — ничего не загружаем
            return;
        }
        // Считываем весь JSON-текст
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
            return;
        }

        // Парсим JSON
        try {
            JSONObject root = new JSONObject(sb.toString());
            if (root.has("users")) {
                JSONArray arr = root.getJSONArray("users");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject u = arr.getJSONObject(i);
                    String uuid = u.getString("uuid");
                    String login = u.getString("login");
                    String password = u.getString("password");
                    users.add(new User(uuid, login, password));
                }
            }
        } catch (JSONException e) {
            System.out.println("Ошибка парсинга JSON: " + e.getMessage());
        }
    }

    /*
    Сохраняет список пользователей в JSON-файл.
    */
    private void saveUsersToFile() {
        // Создаём объект root и помещаем туда массив users
        JSONObject root = new JSONObject();
        JSONArray arr = new JSONArray();

        for (User user : users) {
            JSONObject u = new JSONObject();
            u.put("uuid", user.getUuid());
            u.put("login", user.getLogin());
            u.put("password", user.getPassword());
            arr.put(u);
        }

        root.put("users", arr);

        // Записываем в файл
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
            bw.write(root.toString(2)); // toString(2) для форматирования с отступами
        } catch (IOException e) {
            System.out.println("Ошибка записи файла: " + e.getMessage());
        }
    }
}