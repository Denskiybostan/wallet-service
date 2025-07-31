# 💸 Wallet Service
REST-сервис для управления кошельками с высокой нагрузкой (1000 RPS).  
Реализован на Spring Boot с использованием PostgreSQL, Docker и Liquibase.
---
## 📦 Стек технологий

- Java 17
- Spring Boot (Web, Data JPA, Validation)
- PostgreSQL
- Liquibase
- Docker / Docker Compose
- JUnit / Mockito
- H2 (для интеграционных тестов)
- Maven
## 🚀 Запуск проекта
### ✅ Требования
- Docker и Docker Compose *(если использовать контейнеры)*
- JDK 17 и Maven *(если запускать вручную)*
---
### 🐳 Вариант 1 — через Docker (рекомендуется)
1. Клонируйте репозиторий:

    ```bash
    git clone https://github.com/Denskiybostan/wallet-service.git
    cd wallet-service
    ```
2. Запустите проект:

    ```bash
    docker-compose up --build
    ```
3. Приложение будет доступно по адресу:

    ```
    http://localhost:8080
    ```

4. PostgreSQL доступна на `localhost:5432`  
   *Параметры указаны в `docker-compose.yml` (например, логин: `postgres`, пароль: `postgres`)*

---

### ⚙️ Вариант 2 — вручную без Docker

1. Установите PostgreSQL и создайте БД `wallet_db`

2. Настройте переменные окружения или `application.yml`:

    ```properties
    DB_HOST=localhost
    DB_PORT=5432
    DB_NAME=wallet_db
    DB_USERNAME=postgres
    DB_PASSWORD=postgres
    ```
3. Запустите миграции (Liquibase выполнится автоматически при старте)
4. Соберите проект:

    ```bash
    ./mvnw clean install
    ```
5. Запустите:

    ```bash
    ./mvnw spring-boot:run
    ```
---

## 🧪 Тестирование

Для запуска юнит- и интеграционных тестов:

```bash
./mvnw test
