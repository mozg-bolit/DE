package com.example.de

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * Компонент всплывающего окна (AlertDialog) Капчи.
 * Отображает картинку-оригинал сверху и интерактивную перемешанную сетку фрагментов 2х2 снизу.
 * Реализован скролл и защита от сжатия элементов интерфейса при выводе сообщений об ошибках.
 */
@Composable
fun CaptchaDialog(
    login: String,                 // Логин текущего авторизующегося пользователя (для учета блокировок)
    puzzle: CaptchaPuzzle,         // Объект состояния пазла, переданный с экрана авторизации
    onSuccess: () -> Unit,         // callback-функция при успешной сборке пазла
    onDismiss: () -> Unit          // callback-функция при закрытии окна (нажатии кнопки "Отмена")
) {
    // Локальное текстовое сообщение о результате проверки ("Успешно" или "Неверно! Попыток...")
    var checkMessage by remember { mutableStateOf<String?>(null) }

    // Получаем актуальный объект пользователя из "БД" для вычисления оставшихся попыток ввода
    val user = DatabaseManager.findUser(login)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.width(420.dp), // Фиксированная жесткая ширина окна, исключающая расползание интерфейса
        title = { Text("Проверка: соберите пазл") },
        text = {
            // Вертикальный контейнер со скроллом, гарантирующий доступ ко всем элементам на любых экранах
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()) // Добавлен скролл для предотвращения выталкивания UI
            ) {
                Text("Образец (оригинал сверху):", style = MaterialTheme.typography.caption)

                // Картинка-образец (целый исходный рисунок) f0.png
                Image(
                    painter = painterResource("f0.png"),
                    contentDescription = null,
                    modifier = Modifier
                        .size(110.dp)                     // Фиксированный размер
                        .border(1.dp, Color.Gray)         // Тонкая серая обводка рамки
                )
                Spacer(Modifier.height(12.dp))

                // ОТРИСОВКА СЕТКИ ПАЗЛА 2х2 при помощи вложенных циклов
                Column {
                    for (row in 0..1) { // Строки сетки (0 и 1)
                        Row {
                            for (col in 0..1) { // Колонки сетки (0 и 1)
                                val idx = row * 2 + col                 // Вычисление линейного индекса массива (от 0 до 3)
                                val fragmentId = puzzle.currentOrder[idx]// Получаем номер фрагмента, лежащего в этой ячейке
                                val isSelected = puzzle.selectedIndex == idx // Проверка, выделена ли текущая ячейка

                                Box(
                                    modifier = Modifier
                                        .size(85.dp) // Размер плитки пазла
                                        .padding(2.dp)
                                        // Если плитка выбрана первым кликом — рамка синяя, иначе светло-серая
                                        .border(2.dp, if (isSelected) Color.Blue else Color.LightGray)
                                        .clickable { puzzle.handleCellClick(idx) } // Обработка клика
                                ) {
                                    // Рендеринг самого файла картинки-фрагмента (f1.png - f4.png)
                                    Image(
                                        painter = painterResource("f$fragmentId.png"),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                // Отображение сообщения о результатах проверки (если оно не null)
                checkMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    // Если пазл собран — текст зеленый, если ошибка — красный
                    Text(it, color = if (puzzle.isPassed) Color(0xFF2E7D32) else Color.Red)
                }
                Spacer(Modifier.height(12.dp))

                // Кнопка подтверждения сборки
                Button(
                    onClick = {
                        if (puzzle.check()) {
                            checkMessage = "Пазл собран успешно!"
                            onSuccess() // Вызываем лямбду успешного входа
                        } else {
                            // Если пазл не совпал с образцом:
                            DatabaseManager.handleFailedAttempt(login) // Регистрируем ошибку в БД
                            val remaining = user?.let { 3 - it.failedAttempts } ?: 0 // Считаем остаток попыток
                            checkMessage = "Неверно! Попыток осталось: $remaining"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.9f) // Кнопка занимает 90% ширины контейнера
                ) { Text("Проверить пазл") }
            }
        },
        // Кнопка закрытия окна (отмены авторизации)
        confirmButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}