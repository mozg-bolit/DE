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

@Composable
fun CaptchaDialog(
    login: String,
    puzzle: CaptchaPuzzle,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var checkMessage by remember { mutableStateOf<String?>(null) }
    val user = DatabaseManager.findUser(login)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.width(420.dp), // Фиксированная ширина окна
        title = { Text("Проверка: соберите пазл") },
        text = {
            // САМОЕ ГЛАВНОЕ ИСПРАВЛЕНИЕ: Используем verticalScroll.
            // Теперь кнопки снизу не выталкивают элементы вверх, и f0.png гарантированно видно целиком.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                Text("Образец (оригинал сверху):", style = MaterialTheme.typography.caption)

                // Картинка f0.png защищена от сжатия фиксированным размером
                Image(
                    painter = painterResource("f0.png"),
                    contentDescription = "Образец f0",
                    modifier = Modifier.size(120.dp).border(1.dp, Color.LightGray).padding(4.dp)
                )

                Spacer(Modifier.height(12.dp))

                // Проверка: заблокирован ли пользователь
                if (user == null || user.blocked) {
                    Text("Учётная запись заблокирована!", color = Color.Red, style = MaterialTheme.typography.body2)
                    return@Column
                }

                Text("Соберите изображение:", style = MaterialTheme.typography.caption)
                Spacer(Modifier.height(4.dp))

                // Отрендеренная сетка пазла 2х2
                Column {
                    for (row in 0..1) {
                        Row {
                            for (col in 0..1) {
                                val index = row * 2 + col
                                val fragmentId = puzzle.currentOrder[index]
                                val isSelected = puzzle.selectedIndex == index

                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .padding(2.dp)
                                        .border(2.dp, if (isSelected) MaterialTheme.colors.primary else Color.Gray)
                                        .clickable { puzzle.handleCellClick(index) }
                                ) {
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

                checkMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = if (puzzle.isPassed) Color(0xFF2E7D32) else Color.Red)
                }

                Spacer(Modifier.height(12.dp))

                // Кнопка проверки перенесена внутрь скролла
                Button(
                    onClick = {
                        if (puzzle.check()) {
                            checkMessage = "Пазл собран успешно!"
                            onSuccess()
                        } else {
                            DatabaseManager.handleFailedAttempt(login)
                            checkMessage = "Неверно! Попыток осталось: ${3 - user.failedAttempts}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text("Проверить пазл")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}