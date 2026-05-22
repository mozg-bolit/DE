package com.example.de

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Демонстрационный Экзамен 2026",
    ) {
        var currentUser by remember { mutableStateOf<User?>(null) }

        // Распределение экранов по ролям на основе оригинального main.kt
        when (currentUser?.role) {
            "ADMIN" -> AdminScreen(onLogout = { currentUser = null })
            "USER" -> UserScreen(user = currentUser!!, onLogout = { currentUser = null })
            else -> LoginScreen(onLoginSuccess = { user -> currentUser = user })
        }
    }
}