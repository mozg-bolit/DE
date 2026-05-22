package com.example.de

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AdminScreen(onLogout: () -> Unit) {
    var users by remember { mutableStateOf(DatabaseManager.getAllUsers()) }
    var newLogin by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Панель администратора", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Форма добавления пользователя
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            backgroundColor = Color.LightGray.copy(alpha = 0.1f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Добавить нового пользователя", style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(value = newLogin, onValueChange = { newLogin = it }, label = { Text("Логин") })
                    TextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("Пароль") })

                    Button(onClick = {
                        val added = DatabaseManager.addUser(newLogin, newPassword, "USER")
                        if (added) {
                            statusMessage = "Успешно создано!"
                            users = DatabaseManager.getAllUsers()
                            newLogin = ""
                            newPassword = ""
                        } else {
                            statusMessage = "Ошибка создания!"
                        }
                    }) {
                        Text(text = "Создать")
                    }
                }

                statusMessage?.let { message ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = message, color = MaterialTheme.colors.primary)
                }
            }
        }

        // 2. Список пользователей
        Text(text = "Список пользователей:", style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(users) { user ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 2.dp) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Информация о пользователе и поле смены пароля
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "${user.login} (${user.role})")

                            val isBlocked = user.blocked
                            Text(
                                text = "Статус: ${if (isBlocked) "Заблокирован" else "Активен"}",
                                color = if (isBlocked) Color.Red else Color(0xFF2E7D32)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Упрощенное изменение пароля без лишних вложений
                            var editPassword by remember { mutableStateOf("") }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                TextField(
                                    value = editPassword,
                                    onValueChange = { editPassword = it },
                                    label = { Text("Новый пароль") },
                                    modifier = Modifier.width(140.dp)
                                )
                                Button(onClick = {
                                    if (editPassword.isNotBlank()) {
                                        DatabaseManager.updateUser(user.login) { it.password = editPassword }
                                        users = DatabaseManager.getAllUsers()
                                        editPassword = ""
                                    }
                                }) {
                                    Text(text = "Сменить", style = MaterialTheme.typography.caption)
                                }
                            }
                        }

                        // Кнопки Блокировки и Удаления
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val userBlocked = user.blocked
                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (userBlocked) Color.Green else Color.Red
                                ),
                                onClick = {
                                    DatabaseManager.updateUser(user.login) {
                                        it.blocked = !it.blocked
                                        if (!it.blocked) it.failedAttempts = 0
                                    }
                                    users = DatabaseManager.getAllUsers()
                                }
                            ) {
                                Text(text = if (userBlocked) "Разблок." else "Заблочить")
                            }

                            Button(onClick = {
                                DatabaseManager.deleteUser(user.login)
                                users = DatabaseManager.getAllUsers()
                            }) {
                                Text(text = "Удалить")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onLogout, modifier = Modifier.align(Alignment.End)) {
            Text(text = "Выйти из панели")
        }
    }
}