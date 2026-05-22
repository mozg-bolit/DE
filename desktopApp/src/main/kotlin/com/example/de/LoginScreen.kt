package com.example.de

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(onLoginSuccess: (User) -> Unit) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCaptchaDialog by remember { mutableStateOf(false) }

    val puzzle = remember { CaptchaPuzzle() }

    Column(Modifier.fillMaxSize().padding(16.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Вход в систему", style = MaterialTheme.typography.h5)
        Spacer(Modifier.height(16.dp))

        TextField(value = login, onValueChange = { login = it }, label = { Text("Логин") })
        Spacer(Modifier.height(8.dp))
        TextField(value = password, onValueChange = { password = it }, label = { Text("Пароль") })
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            if (login.isBlank() || password.isBlank()) {
                errorMessage = "Поля не могут быть пустыми!"
                return@Button
            }

            val user = DatabaseManager.findUser(login)
            if (user == null || DatabaseManager.authenticate(login, password) == null) {
                errorMessage = "Неверный логин или пароль"
                DatabaseManager.handleFailedAttempt(login)
                return@Button
            }

            if (user.blocked) {
                errorMessage = "Учётная запись заблокирована. Обратитесь к администратору."
                return@Button
            }

            // Данные верны — открываем капчу
            puzzle.resetForNewAttempt()
            showCaptchaDialog = true
            errorMessage = null
        }) {
            Text("Войти")
        }

        errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colors.error)
        }
    }

    if (showCaptchaDialog) {
        CaptchaDialog(
            login = login,
            puzzle = puzzle,
            onSuccess = {
                showCaptchaDialog = false
                DatabaseManager.resetFailedAttempts(login)
                DatabaseManager.findUser(login)?.let { onLoginSuccess(it) }
            },
            onDismiss = { showCaptchaDialog = false }
        )
    }
}