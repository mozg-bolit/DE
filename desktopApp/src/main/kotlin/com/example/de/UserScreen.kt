package com.example.de

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun UserScreen(user: User, onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("Данные не загружены") }
    var snils by remember { mutableStateOf("Данные не загружены") }
    var inn by remember { mutableStateOf("Данные не загружены") }
    var email by remember { mutableStateOf("Данные не загружены") }
    var idCard by remember { mutableStateOf("Данные не загружены") }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Личный кабинет / Модуль 6", style = MaterialTheme.typography.h5)
        Text("Вы вошли как: ${user.login}", style = MaterialTheme.typography.caption)
        Spacer(Modifier.height(16.dp))

        // Кнопка переключения сервера в самом верху экрана
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Использовать локальный сервер (Lab):")
            Checkbox(checked = ApiConfig.useLabAddress, onCheckedChange = { ApiConfig.useLabAddress = it })
        }
        Spacer(Modifier.height(12.dp))

        // 1. Проверка ФИО клиента
        DataItemCard(
            label = "ФИО (Не должно содержать цифр)", value = fullName,
            onFetch = { scope.launch { fullName = DatabaseManager.fetchFullName() } },
            validate = { if (it.any { c -> c.isDigit() }) "Не успешно: найдены цифры" else "Успешно" }
        )

        // 2. Проверка СНИЛС
        DataItemCard(
            label = "СНИЛС (Формат: XXX-XXX-XXX XX)", value = snils,
            onFetch = { scope.launch { snils = DatabaseManager.fetchSnils() } },
            validate = { if (Regex("^\\d{3}-\\d{3}-\\d{3}\\s\\d{2}$").matches(it)) "Успешно" else "Не успешно: неверный формат" }
        )

        // 3. Проверка ИНН
        DataItemCard(
            label = "ИНН (Должно быть ровно 10 или 12 цифр)", value = inn,
            onFetch = { scope.launch { inn = DatabaseManager.fetchInn() } },
            validate = { if ((it.length == 10 || it.length == 12) && it.all { c -> c.isDigit() }) "Успешно" else "Не успешно" }
        )

        // 4. Проверка Email
        DataItemCard(
            label = "Электронная почта", value = email,
            onFetch = { scope.launch { email = DatabaseManager.fetchEmail() } },
            validate = { if (it.contains("@") && it.contains(".")) "Успешно" else "Не успешно" }
        )

        // 5. Проверка Карты-пропуска
        DataItemCard(
            label = "Номер карты-пропуска", value = idCard,
            onFetch = { scope.launch { idCard = DatabaseManager.fetchIdentityCard() } },
            validate = { if (it.replace(" ", "").all { c -> c.isDigit() }) "Успешно" else "Не успешно" }
        )

        Spacer(Modifier.height(20.dp))
        Button(onClick = onLogout) { Text("Выйти") }
    }
}

// Переиспользуемая карточка отображения и проверки строки данных
@Composable
fun DataItemCard(label: String, value: String, onFetch: () -> Unit, validate: (String) -> String) {
    val resultStatus = if (value == "Данные не загружены" || value == "Ошибка сервера") "-" else validate(value)

    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 2.dp) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.subtitle2, color = MaterialTheme.colors.primary)
            Text("Значение: $value", style = MaterialTheme.typography.body2)
            Text("Статус проверки: $resultStatus", color = if (resultStatus == "Успешно") Color(0xFF2E7D32) else if (resultStatus.contains("Не успешно")) Color.Red else Color.Black)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onFetch) { Text("Запросить данные", style = MaterialTheme.typography.caption) }
        }
    }
}