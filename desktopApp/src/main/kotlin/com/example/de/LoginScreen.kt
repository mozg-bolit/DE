package com.example.de

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Основной экран авторизации пользователей (Форма авторизации: логин/пароль).
 * Проверяет заполненность полей, статус блокировки аккаунта, валидность пароля,
 * и инициирует открытие диалогового окна Капчи.
 */
@Composable
fun LoginScreen(onLoginSuccess: (User) -> Unit) {
    // Состояния текстовых полей ввода
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Текст сообщения об ошибке (выводится внизу под кнопкой)
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Триггер видимости диалогового окна капчи
    var showCaptchaDialog by remember { mutableStateOf(false) }

    // Сохраняем экземпляр пазла в памяти текущего экрана, чтобы состояние не сбрасывалось при рекомпозициях
    val puzzle = remember { CaptchaPuzzle() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Вход в систему", style = MaterialTheme.typography.h5)
        Spacer(Modifier.height(16.dp))

        // Поле ввода логина
        TextField(value = login, onValueChange = { login = it }, label = { Text("Логин") })
        Spacer(Modifier.height(8.dp))

        // Поле ввода пароля
        TextField(value = password, onValueChange = { password = it }, label = { Text("Пароль") })
        Spacer(Modifier.height(16.dp))

        // Кнопка отправки формы авторизации
        Button(onClick = {
            // ПРОВЕРКА 1: Пустота полей
            if (login.isBlank() || password.isBlank()) {
                errorMessage = "Поля не могут быть пустыми!"
                return@Button
            }

            val user = DatabaseManager.findUser(login)

            // ПРОВЕРКА 2: Если пользователь заблокирован — сразу запрещаем вход без сверки пароля
            if (user != null && user.blocked) {
                errorMessage = "Учётная запись заблокирована. Обратитесь к администратору."
                return@Button
            }

            // ПРОВЕРКА 3: Валидация пары Логин-Пароль
            if (DatabaseManager.authenticate(login, password) == null) {
                errorMessage = "Неверный логин или пароль"
                DatabaseManager.handleFailedAttempt(login) // Фиксируем ошибку ввода пароля в БД
                return@Button
            }

            // Если все проверки пройдены — сбрасываем состояние старой капчи и открываем диалог
            puzzle.resetForNewAttempt()
            showCaptchaDialog = true
            errorMessage = null // Очищаем старые ошибки
        }) { Text("Войти") }

        // Блок отрисовки ошибки под кнопкой "Войти"
        errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colors.error)
        }
    }

    // Если флаг showCaptchaDialog равен true — рендерим окно поверх экрана
    if (showCaptchaDialog) {
        CaptchaDialog(
            login = login, puzzle = puzzle,
            onSuccess = {
                // Если капча решена верно:
                showCaptchaDialog = false // Закрываем капчу
                DatabaseManager.resetFailedAttempts(login) // Сбрасываем счетчик ошибок пользователя в 0
                DatabaseManager.findUser(login)?.let { onLoginSuccess(it) } // Передаем объект юзера в main.kt для смены экрана
            },
            onDismiss = { showCaptchaDialog = false } // Если окно закрыли кнопкой "Отмена"
        )
    }
}