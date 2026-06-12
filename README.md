# 💰 Account Service

![Spring Boot](https://shields.io)
![Java](https://shields.io)
![PostgreSQL](https://shields.io)
![SonarQube](https://shields.io)
![Security](https://shields.io)


Комплексна бекенд-система корпоративного рівня для управління фінансами, рахунками компанії, співробітниками та безпекою організації. Проєкт реалізовано в рамках сертифікації на платформі **JetBrains Academy / Hyperskill**. Фокус проєкту — побудова захищеного REST API з розподілом прав доступу (RBAC), логуванням операцій та захистом від кібератак.

---

## 🛠️ Технологічний стек та Інфраструктура

*   **Core:** Java 17 / 21, Spring Boot 3.x (Web, Security, Data JPA)
*   **Database:** PostgreSQL, розгорнута в **Docker-контейнері** на віддаленій Linux VM.
*   **Code Quality:** **SonarQube / SonarScanner**, розгорнутий на Linux VM для статичного аналізу коду (Static Code Analysis), пошуку багів, вразливостей (Vulnerabilities) та технічного боргу.
*   **Security:** HTTPS (SSL/TLS), HTTP Basic Authentication, BCrypt hashing.
*   **Testing:** JUnit 5, Mockito, MockMvc (Slice Integration Testing).
*   **Tools:** Lombok & Jackson Object Mapper.

---

## 🛡️ Просунуті механізми безпеки (Advanced Security)

В додатку впроваджено суворі стандарти безпеки:

*   **🔒 Шифрування трафіку (HTTPS/SSL):** Весь обмін даними зашифровано за допомогою протоколу TLS (пакет Keystore PKCS12), що повністю захищає систему від атак типу *Man-in-the-Middle (MitM)*.
*   **📝 Журнал аудиту (Audit Log):** Будь-яка критична дія (реєстрація, зміна пароля, редагування ролей, видалення чи блокування) автоматично фіксується в базі даних із зазначенням виконавця, типу події, URI-шляху та мітки часу.
*   **🚫 Захист від брутфорсу (Account Lockout):** Інтегровано лічильник помилок входу. Після **5 поспіль невдалих спроб** автентифікації акаунт автоматично отримує статус `is_locked = true`. Розблокувати користувача може тільки Адміністратор.

---

## 👥 Рольова модель (RBAC)

*   **`Anonymous`** — Доступ лише до реєстрації.
*   **`USER`** — Перегляд особистих фінансових відомостей.
*   **`ACCOUNTANT`** — Масове завантаження та редагування зарплат усіх працівників.
*   **`ADMINISTRATOR`** — Управління користувачами, зміна ролей, блокування акаунтів та перегляд журналу безпеки. *(Примітка: Адміністратор повністю ізольований від фінансових даних).*

---

## 🌐 Специфікація API Endpoints

### 🔐 Автентифікація (`/api/auth`)
*   `POST /api/auth/signup` — Реєстрація користувача `[Доступно всім]`
*   `POST /api/auth/changepass` — Зміна пароля `[Авторизовані користувачі]` *(Діє перевірка на TOP-100 слабких паролів)*

### 📊 Робота з фінансами (`/api/acct`)
*   `POST /api/acct/payments` — Масове завантаження платежів списком `List<PaymentRequest>` `[Роль: ACCOUNTANT]`
*   `PUT /api/acct/payments` — Оновлення інформації про зарплату працівника `[Роль: ACCOUNTANT]`

### 👤 Кабінет працівника (`/api/empl`)
*   `GET /api/empl/payments` — Перегляд зарплати за період `[Ролі: USER, ACCOUNTANT]`

### 🛠️ Панель Адміністратора та Безпека (`/api/admin` | `/api/security`)
*   `GET /api/admin/user` — Отримання списку всіх користувачів `[Роль: ADMINISTRATOR]`
*   `DELETE /api/admin/user/{email}` — Видалення користувача із системи `[Роль: ADMINISTRATOR]`
*   `PUT /api/admin/user/role` — Зміна ролей користувачів `[Роль: ADMINISTRATOR]`
*   `PUT /api/admin/user/access` — Блокування/Розблокування акаунтів `[Роль: ADMINISTRATOR]`
*   `GET /api/security/events` — Перегляд журналу подій безпеки (Audit Log) `[Роль: ADMINISTRATOR]`

---

## 🧪 Стратегія тестування (Testing)

У проєкті реалізовано швидкі **зрізові інтеграційні тести (Slice Integration Tests)** для контролерів та веб-інфраструктури через `MockMvc` із підміною бізнес-шару через `@MockBean`. 

Тести повністю ізольовані від стану головної бази даних та адаптовані до роботи у будь-яких часових зонах (UTC-фіксація на рівні JVM).

### Запуск тестів:
```bash
mvn test
```

### Конфігурація тестового профайлу (`src/test/resources/application-test.properties`):
```properties
spring.datasource.driver-class-name=org.postgresql.Driver
# Підключення до бази даних у Docker-контейнері на Linux VM
spring.datasource.url=jdbc:postgresql://<IP_YOUR_LINUX_VM>:5432/account_service_test
spring.datasource.username=postgres
spring.datasource.password=your_db_password

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```
