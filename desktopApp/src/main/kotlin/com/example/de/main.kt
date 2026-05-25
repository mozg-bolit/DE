package com.example.de

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

/**
 * Главная точка входа Desktop-приложения на базе Compose for Desktop.
 * Отвечает за инициализацию окна и глобальную маршрутизацию (навигацию) между экранами
 * в зависимости от роли авторизованного пользователя.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication, // Закрытие процесса при закрытии окна
        title = "Демонстрационный Экзамен 2026", // Заголовок окна программы
    ) {
        // Глобальный стейт текущего сеанса пользователя. Изначально null (никто не вошел)
        var currentUser by remember { mutableStateOf<User?>(null) }

        // Роутинг экранов на основе роли в объекте currentUser
        when (currentUser?.role) {
            "ADMIN" -> {
                // Если роль ADMIN — рендерим панель управления пользователями
                AdminScreen(onLogout = { currentUser = null })
            }
            "USER" -> {
                // Если роль USER — передаем объект пользователя в ЛК и открываем экран API
                UserScreen(user = currentUser!!, onLogout = { currentUser = null })
            }
            else -> {
                // Если currentUser == null (никто не авторизован) — принудительно отображаем экран логина
                LoginScreen(onLoginSuccess = { user -> currentUser = user })
            }
        }
    }
}