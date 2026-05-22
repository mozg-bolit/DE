package com.example.de

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.Json

// Модель данных пользователя
data class User(
    val login: String,
    var password: String,
    val role: String,         // "ADMIN" или "USER"
    var blocked: Boolean = false,
    var failedAttempts: Int = 0
)

// Менеджер базы данных и запросов API
object DatabaseManager {
    private val client = HttpClient(CIO)

    // Список пользователей в оперативной памяти (Имитация БД)
    private val users = mutableListOf(
        User("admin", "admin123", "ADMIN"),
        User("user1", "pass1", "USER"),
        User("user2", "pass2", "USER", blocked = true)
    )

    fun getAllUsers(): List<User> = users.toList()
    fun findUser(login: String): User? = users.find { it.login == login }

    // Проверка логина и пароля
    fun authenticate(login: String, password: String): User? {
        val user = findUser(login)
        return if (user != null && user.password == password) user else null
    }

    // Добавление нового пользователя администратором
    fun addUser(login: String, password: String, role: String): Boolean {
        if (findUser(login) != null || login.isBlank() || password.isBlank()) return false
        users.add(User(login, password, role))
        return true
    }

    // Удаление пользователя администратором
    fun deleteUser(login: String) {
        users.removeIf { it.login == login }
    }

    // Изменение данных (пароль, блокировка) через лямбду
    fun updateUser(login: String, update: (User) -> Unit) {
        findUser(login)?.let { update(it) }
    }

    // Логика обработки неудачной попытки входа
    fun handleFailedAttempt(login: String) {
        findUser(login)?.let {
            it.failedAttempts++
            if (it.failedAttempts >= 3) {
                it.blocked = true
            }
        }
    }

    // Сброс счетчика ошибок при успешном входе
    fun resetFailedAttempts(login: String) {
        findUser(login)?.let { it.failedAttempts = 0 }
    }

    /**
     * МОДУЛЬ 6: Безопасный запрос к API.
     * Защищает от падения (ошибки 500), если сервер шлет обычный текст вместо JSON,
     * и сразу очищает строковые ответы от мусора (% и &) согласно ТЗ.
     */
    private suspend fun safeApiRequest(endpoint: String): String {
        return try {
            val response: HttpResponse = client.get("${ApiConfig.baseUrl}/$endpoint")
            val rawBody = response.bodyAsText().trim()

            // Если пришел JSON-объект — парсим, иначе работаем как с чистым текстом
            val cleanText = if (rawBody.startsWith("{") && rawBody.endsWith("}")) {
                Json.decodeFromString<ApiResponse>(rawBody).value
            } else {
                rawBody
            }
            // Очистка от спецсимволов % и &
            cleanText.replace("%", "").replace("&", " ").trim()
        } catch (e: Exception) {
            "Ошибка сервера"
        }
    }

    // Методы интеграции с API из ТЗ
    suspend fun fetchFullName() = safeApiRequest("fullName")
    suspend fun fetchSnils() = safeApiRequest("snils")
    suspend fun fetchInn() = safeApiRequest("inn")
    suspend fun fetchEmail() = safeApiRequest("email")
    suspend fun fetchIdentityCard() = safeApiRequest("identityCard")
}