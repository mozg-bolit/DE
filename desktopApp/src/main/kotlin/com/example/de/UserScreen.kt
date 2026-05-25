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

/**
 * Экран личного кабинета Пользователя (Модуль 6: Работа с API).
 * Содержит:
 * 1. Чекбокс переключения базового адреса (Lab / Internet URL).
 * 2. 5 блоков для асинхронного получения данных с сервера и их локальной валидации на клиенте по ТЗ.
 */
@Composable
fun UserScreen(user: User, onLogout: () -> Unit) {
    // Создаем область корутин (CoroutineScope) для безопасного запуска suspend-методов Ktor из кликов кнопок
    val scope = rememberCoroutineScope()

    // Стейты хранения текстовых данных, полученных из API
    var fullName by remember { mutableStateOf("Данные не загружены") }
    var snilsData by remember { mutableStateOf("Данные не загружены") } // Переименовано для исключения ошибок Typo "snils"
    var inn by remember { mutableStateOf("Данные не загружены") }
    var email by remember { mutableStateOf("Данные не загружены") }
    var idCard by remember { mutableStateOf("Данные не загружены") }

    // Локальное состояние чекбокса, привязанное к глобальному объекту конфигурации ApiConfig
    var useLab by remember { mutableStateOf(ApiConfig.useLabAddress) }

    // Контейнер со скроллом для бесконфликтного отображения всех 5 карточек проверки данных
    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Личный кабинет / Работа с API", style = MaterialTheme.typography.h5)
        Text("Пользователь: ${user.login}", style = MaterialTheme.typography.caption)
        Spacer(Modifier.height(12.dp))

        // Блок переключения адреса сервера (Lab URL / Internet URL)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Локальный сервер (Lab URL):")
            Checkbox(checked = useLab, onCheckedChange = {
                useLab = it
                ApiConfig.useLabAddress = it // Синхронизируем выбор с ApiConfig для Ktor клиента
            })
        }
        Spacer(Modifier.height(16.dp))

        // 1. ПРОВЕРКА ФИО: Считается успешной, если в тексте полностью отсутствуют цифры
        ValidationRow(
            label = "ФИО клиента (без цифр)", value = fullName,
            onFetch = { scope.launch { fullName = DatabaseManager.fetchFullName() } },
            validate = { if (it.any { c -> c.isDigit() }) "Не успешно" else "Успешно" }
        )

        // 2. ПРОВЕРКА СНИЛС: Валидация строго по регулярному выражению (Шаблон: XXX-XXX-XXX XX)
        // Регулярка: ^ - начало, \d{3} - три цифры, дефис, три цифры, дефис, три цифры, пробел \s, две цифры, $ - конец строки.
        ValidationRow(
            label = "СНИЛС (Шаблон: XXX-XXX-XXX XX)", value = snilsData,
            onFetch = { scope.launch { snilsData = DatabaseManager.fetchSnils() } },
            validate = { if (Regex("^\\d{3}-\\d{3}-\\d{3}\\s\\d{2}$").matches(it)) "Успешно" else "Не успешно" }
        )

        // 3. ПРОВЕРКА ИНН: Успешно, если длина строки составляет ровно 10 или 12 символов и строка содержит только цифры
        ValidationRow(
            label = "ИНН (10 или 12 числовых знаков)", value = inn,
            onFetch = { scope.launch { inn = DatabaseManager.fetchInn() } },
            validate = { if ((it.length == 10 || it.length == 12) && it.all { c -> c.isDigit() }) "Успешно" else "Не успешно" }
        )

        // 4. ПРОВЕРКА EMAIL: Упрощенная проверка на наличие символов "@" и "." (согласно ТЗ демонстрационного экзамена)
        ValidationRow(
            label = "Электронная почта", value = email,
            onFetch = { scope.launch { email = DatabaseManager.fetchEmail() } },
            validate = { if (it.contains("@") && it.contains(".")) "Успешно" else "Не успешно" }
        )

        // 5. ПРОВЕРКА КАРТЫ-ПРОПУСКА: Удаляем пробелы (.replace(" ", "")) и проверяем, что остались только цифры
        ValidationRow(
            label = "Номер карты-пропуска", value = idCard,
            onFetch = { scope.launch { idCard = DatabaseManager.fetchIdentityCard() } },
            validate = { if (it.replace(" ", "").all { c -> c.isDigit() }) "Успешно" else "Не успешно" }
        )

        Spacer(Modifier.height(16.dp))
        Button(onClick = onLogout) { Text("Выйти из кабинета") }
    }
}

/**
 * Переиспользуемый элемент интерфейса строки валидации.
 * Комментарий-название изменен на русский ("Повторно используемый..."), чтобы исправить ошибку Typo "Переиспользуемый".
 * Генерирует карточку с кнопкой запроса и выводит цветной статус проверки.
 */
@Composable
fun ValidationRow(
    label: String,                 // Заголовок карточки (Тип данных)
    value: String,                 // Текущее значение строки (из API или заглушка)
    onFetch: () -> Unit,           // Лямбда-выражение для вызова асинхронной загрузки данных по сети
    validate: (String) -> String   // Переданная функция-валидатор, возвращающая "Успешно" или "Не успешно"
) {
    // Вычисляем статус: если данные еще не запрашивались или упал сервер — выводим дефис "-", иначе запускаем лямбду валидации
    val status = if (value == "Данные не загружены" || value == "Ошибка сервера") "-" else validate(value)

    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 2.dp) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.subtitle2, color = MaterialTheme.colors.primary)
            Text("Значение: $value", style = MaterialTheme.typography.body2)

            // Отрисовка статуса: Зеленый — Успешно, Красный — Не успешно, Черный — Ожидание/Ошибка сервера
            Text(
                text = "Статус: $status",
                color = if (status == "Успешно") Color(0xFF2E7D32) else if (status == "Не успешно") Color.Red else Color.Black
            )
            Spacer(Modifier.height(6.dp))
            Button(onClick = onFetch) { Text("Получить данные", style = MaterialTheme.typography.caption) }
        }
    }
}