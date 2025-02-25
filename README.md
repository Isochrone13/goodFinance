## Общая структура проекта

Проект состоит из нескольких классов, каждый из которых выполняет свою роль:

- **Main** — точка входа в приложение.
- **AuthController** — отвечает за регистрацию, авторизацию, выход из системы.
- **UserService** — управляет хранением и поиском пользователей.
- **FinanceController** — управляет взаимодействием с кошельком после авторизации: добавление транзакций, категорий, бюджетов и т.д.
- **WalletService** — сервис для работы с объектом `Wallet`: загрузка/сохранение в файл, подсчёты доходов/расходов, бюджеты, выдача статистики.
- **Wallet** — класс, описывающий кошелёк, то есть содержит список транзакций, набор категорий и данные о бюджетах.
- **Transaction** — класс-транзакция с информацией о сумме, дате/времени, категории, типе (доход/расход).
- **TransactionType** — enum, в котором записаны типы транзакций: `income` (доход) и `expense` (расход).
- **User** — класс пользователя, хранит `uuid`, `login`, `password`.

Приложение позволяет пользователю:

1. Зарегистрировать новый аккаунт (автоматическая генерация пароля/UUID, вывод его на экран).
2. Авторизоваться по логину или `uuid`.
3. После авторизации управлять финансами в меню:
    - Создавать новые категории.
    - Добавлять доходы/расходы.
    - Устанавливать бюджеты по категориям и контролировать превышения.
    - Просматривать сводную информацию по своим финансам.

---

## Сущности приложения

### User (пользователь)

- **uuid** — уникальный идентификатор (генерируется при регистрации).
- **login** — имя пользователя (либо псевдоним), которое он вводит при авторизации.
- **password** — пароль для входа. Пользователь хранится в списке `users` внутри `UserService`, а затем сериализуется в JSON-файл (`users.json`).

### UserService (работа с пользователями)

- При инициализации получает путь к JSON-файлу, где хранятся данные пользователей.
- Загружает пользователей в список `users`.
- Имеет методы:
    - `findByLogin(String login)`: поиск по логину (без учета регистра).
    - `findByUuid(String uuid)`: поиск по UUID.
    - `addUser(User user)`: добавление и сохранение нового пользователя.

### AuthController (регистрация/авторизация)

- Использует `UserService` для поиска и добавления пользователей.
- Содержит:
    - `registerUser()`:
        1. Считывает логин.
        2. Проверяет формат логина (допустимые символы).
        3. Генерирует уникальный `UUID`.
        4. Генерирует пароль из 6 символов (строчные лат.буквы + цифры).
        5. Создаёт пользователя и добавляет его в `UserService`.
        6. Автоматически авторизует нового пользователя (запоминается в `currentUser`).
    - `loginUser()`:
        1. Считывает ввод (логин/UUID и пароль).
        2. Сверяет пароль. Если совпадает, пользователь авторизуется.
    - `logoutUser()`:
        - Сбрасывает `currentUser` в `null`.

### Wallet (кошелёк пользователя)

- **transactions** — список всех транзакций (`доход`/`расход`).
- **budgets** — карта «категория -> бюджет». Если бюджет для категории не установлен, считается 0.
- **categories** — множество доступных категорий, чтобы исключить дублирование.

### Transaction (транзакция)

- **type** (`income` или `expense`) — указывает на тип: доход или расход.
- **category** — к какой категории относится.
- **amount** — сумма.
- **dateTime** — время операции (ставится текущее в момент создания).

### TransactionType (тип транзакции)

Здесь перечислены типы транзакций.

### WalletService (управление кошельком)

- **wallet** — текущий экземпляр `Wallet`, загружается при авторизации из файла `wallet_{uuid}.json`.
- Методы:
    - `loadWalletFromFile()` / `saveWalletToFile()` — чтение и запись кошелька в JSON.
    - `addCategory(String category)` — добавляет новую категорию в `Set` категорий.
    - `addTransaction(TransactionType type, String category, double amount)` — добавляет операцию (доход/расход) с проверками. Если расход превышает лимит (или общие расходы превосходят доход), выводит предупреждение.
    - `setBudget(String category, double limit)` — устанавливает/обновляет бюджет по категории.
    - Методы для подсчёта дохода/расходов:
        - `calculateTotalIncome()` / `calculateTotalExpense()` — общие доход/расход.
        - `calculateExpenseInCategory(String category)` и `calculateIncomeInCategory(String category)` — по конкретной категории.
    - Методы для вывода статистики:
        - `printSummaryAll()` — сводная информация по всем категориям (доход, расход, бюджеты, остатки).
        - `printSummaryByCategories(List<String> categories)` — аналогичная сводка, но только по выбранным пользователем категориям.

### FinanceController (основное меню работы с финансами)

- Отвечает за интерактивное меню после авторизации.
- Основной метод: `startFinanceLoop()`, который:
    1. Показывает пункты меню (создать категорию, добавить доход/расход, установить бюджет, показать статистику, выход).
    2. По каждой операции вызывает соответствующий метод (`createCategory()`, `addIncome()`, `addExpense()`, `setCategoryBudget()`, `showInformation()`).
    3. После каждой операции вызывает `walletService.saveWalletToFile()`, чтобы изменения не терялись.
 
---

## Механизм работы приложения

1. **Запуск приложения**:
    - В классе `Main` метод `main()` начинает работу.
    - Инициализируется `UserService` (для работы с `users.json`).
    - Создаётся `AuthController` на базе `UserService`.
2. **Главное меню** (в `Main`):
    - Три опции: *Регистрация*, *Авторизация* и *Выход*.
    - Если выбирается *Регистрация* (`1`):
        - Вызывается `authController.registerUser()`.
        - Генерируется `uuid`, пароль. Создаётся `User`. Сохраняется в `users.json`.
        - Если пользователь успешно создан, он автоматически авторизуется. Далее приложение переходит в меню кошелька (`openFinanceMenu(...)`).
    - Если выбирается *Авторизация* (`2`):
        - Вызывается `authController.loginUser()`.
        - Если авторизация успешна, тоже переходим в меню кошелька.
3. **Переход в меню кошелька** (`openFinanceMenu` в `Main`):
    - По текущему пользователю берем его `uuid`.
    - Формируем имя JSON-файла кошелька: `wallet_{uuid}.json`.
    - Создаём `WalletService` с этим файлом, который загрузит или создаст новый `Wallet`.
    - Создаём `FinanceController` и запускаем метод `controller.startFinanceLoop()`.
4. **Меню управления финансами** (метод `startFinanceLoop()` в `FinanceController`):
    - Показывает пункты:
        1. Создать категорию.
        2. Добавить доход.
        3. Добавить расход.
        4. Установить/обновить бюджет.
        5. Показать сведения (все или по выбранным категориям).
        6. Вернуться назад (завершить цикл).
    - После каждой операции (кроме «выхода» из цикла) вызывает `walletService.saveWalletToFile()` для сохранения изменений.
5. **Основные операции**:
    - **Создать категорию**:С помощью `walletService.addCategory(...)` добавляется новая строка в `Set<String>` `categories`.
    - **Добавить доход**:
        1. Показываются существующие категории.
        2. Пользователь выбирает категорию (должна существовать, иначе ошибка).
        3. Вводится сумма дохода.
        4. Вызывается `walletService.addTransaction(TransactionType.income, category, amount)`.
    - **Добавить расход**:Похоже на добавление дохода, но:
        1. После добавления проверяются бюджеты (не превышен ли лимит по категории).
        2. Проверяется, не превышены ли общие расходы по сравнению с общим доходом.
    - **Установить/обновить бюджет**:Выбирается существующая категория и вводится сумма. Вызывается `walletService.setBudget(...)`.
    - **Показать сведения**:
        - Либо полная статистика по всем категориям (`printSummaryAll()`),
        - Либо по выбранным категориям (`printSummaryByCategories(...)`).
6. **Завершение работы**:
    - При выходе из меню кошелька вызывается `authController.logoutUser()`.
    - Возврат в главное меню — при желании можно снова авторизоваться под другим пользователем или выйти из приложения.
