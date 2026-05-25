package com.example.de

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

/**
 * Центральный менеджер данных приложения (Синглтон).
 * Содержит в себе:
 * 1. Имитацию таблицы базы данных пользователей в оперативной памяти.
 * 2. Сервисные методы авторизации, блокировки и администрирования.
 * 3. Настроенный HTTP-клиент Ktor для выполнения сетевых запросов к API (Модуль 6).
 */
object DatabaseManager {
    // Инициализация асинхронного HTTP-клиента Ktor с движком CIO (Coroutine-based I/O)
    private val client = HttpClient(CIO)

    // Внутренний изменяемый список (коллекция) пользователей — имитация таблицы БД "Users"
    private val users = mutableListOf(
        User("admin", "admin123", "ADMIN"),                     // Администратор по умолчанию
        User("user1", "pass1", "USER"),                         // Обычный активный пользователь
        User("user2", "pass2", "USER", blocked = true)         // Изначально заблокированный пользователь
    )

    // Возвращает неизменяемый снимок списка для безопасного отображения в интерфейсе
    fun getAllUsers(): List<User> = users.toList()

    // Поиск конкретного пользователя в "БД" по его уникальному логину
    fun findUser(login: String): User? = users.find { it.login == login }

    /**
     * Проверка пары Логин/Пароль.
     * Возвращает объект пользователя, если данные верны. Блокировку здесь намеренно не проверяем,
     * чтобы LoginScreen мог выдать правильную ошибку ("Неверный пароль" ИЛИ "Вы заблокированы").
     */
    fun authenticate(login: String, password: String): User? {
        val user = findUser(login)
        return if (user != null && user.password == password) user else null
    }

    /**
     * Добавление нового пользователя через панель Администратора.
     * Проверяет на уникальность логина. Возвращает true, если успешно добавлен.
     */
    fun addUser(login: String, password: String, role: String): Boolean {
        if (findUser(login) != null || login.isBlank() || password.isBlank()) return false
        users.add(User(login, password, role))
        return true
    }

    // Удаление пользователя из коллекции по логину
    fun deleteUser(login: String) {
        users.removeIf { it.login == login }
    }

    // Универсальный метод обновления полей пользователя через лямбда-выражение scope-функции
    fun updateUser(login: String, update: (User) -> Unit) {
        findUser(login)?.let { update(it) }
    }

    /**
     * Метод обработки неудачного ввода учетных данных.
     * Увеличивает счетчик ошибок на 1. При достижении 3 ошибок выставляет флаг блокировки.
     */
    fun handleFailedAttempt(login: String) {
        findUser(login)?.let {
            it.failedAttempts++
            if (it.failedAttempts >= 3) {
                it.blocked = true // Автоматическая блокировка аккаунта согласно ТЗ
            }
        }
    }

    // Сброс счетчика ошибок (вызывается при успешном прохождении авторизации и капчи)
    fun resetFailedAttempts(login: String) {
        findUser(login)?.let { it.failedAttempts = 0 }
    }

    /**
     * МОДУЛЬ 6: Внутренний защищенный метод выполнения сетевых запросов.
     * @param endpoint Хвостовая часть URL адреса метода (например, "fullName", "snils")
     * Обрабатывает потенциальное падение сервера (500 Error), исключая краш приложения.
     * Выполняет глубокую очистку сырых данных от спецсимволов "%" и "&" согласно ТЗ.
     */
    private suspend fun safeApiRequest(endpoint: String): String {
        return try {
            // Выполняем GET запрос, используя динамически собранный URL из ApiConfig
            val response: HttpResponse = client.get("${ApiConfig.baseUrl}/$endpoint")
            val rawBody = response.bodyAsText().trim() // Извлекаем текст ответа сервера

            // Проверяем: если сервер вернул JSON-строку, парсим её через kotlinx.serialization
            val cleanText = if (rawBody.startsWith("{") && rawBody.endsWith("}")) {
                Json.decodeFromString<ApiResponse>(rawBody).value
            } else {
                // Если сервер вернул сырой текст без JSON-структуры
                rawBody
            }

            // ОЧИСТКА ДАННЫХ: Удаляем "%", заменяем амперсанд "&" на пробел (актуально для ФИО), удаляем лишние пробелы по краям
            cleanText.replace("%", "").replace("&", " ").trim()
        } catch (e: Exception) {
            // В случае сетевой ошибки, таймаута или ошибки 500 — возвращаем статус-заглушку
            "Ошибка сервера"
        }
    }

    // --- Публичные методы API, вызываемые с экрана пользователя (UserScreen) ---
    suspend fun fetchFullName(): String = safeApiRequest("fullName")
    suspend fun fetchSnils(): String = safeApiRequest("snils")
    suspend fun fetchInn(): String = safeApiRequest("inn")
    suspend fun fetchEmail(): String = safeApiRequest("email")
    suspend fun fetchIdentityCard(): String = safeApiRequest("identityCard")
}