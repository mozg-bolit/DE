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

/**
 * Экран Администратора системы.
 * Позволяет:
 * 1. Просматривать список всех зарегистрированных пользователей.
 * 2. Видеть текущий статус блокировки аккаунтов.
 * 3. Создавать новых пользователей (роль "USER" по умолчанию).
 * 4. Изменять существующие пароли пользователей.
 * 5. Блокировать/разблокировать учетные записи вручную.
 * 6. Удалять пользователей из системы.
 */
@Composable
fun AdminScreen(onLogout: () -> Unit) {
    // Хранит актуальный список пользователей для мгновенного обновления UI при CRUD-операциях
    var users by remember { mutableStateOf(DatabaseManager.getAllUsers()) }

    // Состояния полей ввода формы добавления нового пользователя
    var newLogin by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Панель администратора", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        // ФОРМА ДОБАВЛЕНИЯ НОВОГО ПОЛЬЗОВАТЕЛЯ (Карточка)
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), backgroundColor = Color.LightGray.copy(alpha = 0.1f)) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(value = newLogin, onValueChange = { newLogin = it }, label = { Text("Логин") }, modifier = Modifier.weight(1f))
                TextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("Пароль") }, modifier = Modifier.weight(1f))
                Button(onClick = {
                    // Пробуем добавить пользователя в список БД
                    if (DatabaseManager.addUser(newLogin, newPassword, "USER")) {
                        users = DatabaseManager.getAllUsers() // Обновляем UI список
                        newLogin = ""; newPassword = "" // Очищаем текстовые поля формы
                    }
                }) { Text("Создать") }
            }
        }

        // ОПТИМИЗИРОВАННЫЙ СПИСОК ПОЛЬЗОВАТЕЛЕЙ (Аналог RecyclerView в XML)
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(users) { user -> // Итерация по каждому пользователю в списке
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = 2.dp) {
                    Row(modifier = Modifier.padding(12.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {

                        // ЛЕВАЯ ЧАСТЬ: Информация о пользователе и форма редактирования пароля
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${user.login} (${user.role})")
                            // Динамическое отображение статуса блокировки цветом
                            Text(
                                text = "Статус: ${if (user.blocked) "Заблокирован" else "Активен"}",
                                color = if (user.blocked) Color.Red else Color(0xFF2E7D32)
                            )

                            // Поле быстрого изменения пароля внутри элемента списка
                            var editPassword by remember { mutableStateOf("") }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                                TextField(value = editPassword, onValueChange = { editPassword = it }, label = { Text("Новый пароль") }, modifier = Modifier.width(130.dp))
                                Button(onClick = {
                                    if (editPassword.isNotBlank()) {
                                        // Вызов обновления пароля в DatabaseManager
                                        DatabaseManager.updateUser(user.login) { u -> u.password = editPassword }
                                        users = DatabaseManager.getAllUsers() // Обновляем интерфейс
                                    }
                                }) { Text("ОК", style = MaterialTheme.typography.caption) }
                            }
                        }

                        // ПРАВАЯ ЧАСТЬ: Кнопки Управления Учетной Записью
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Кнопка Блокировки / Разблокировки
                            Button(
                                // Если заблокирован — кнопка Зеленая ("Разблок."), если активен — Красная ("Заблочить")
                                colors = ButtonDefaults.buttonColors(backgroundColor = if (user.blocked) Color.Green else Color.Red),
                                onClick = {
                                    DatabaseManager.updateUser(user.login) {
                                        it.blocked = !it.blocked // Инвертируем флаг блокировки
                                        if (!it.blocked) it.failedAttempts = 0 // Обязательный сброс ошибок при ручной разблокировке админом
                                    }
                                    users = DatabaseManager.getAllUsers() // Синхронизируем UI список
                                }
                            ) { Text(if (user.blocked) "Разблок." else "Заблочить") }

                            // Кнопка Удаления пользователя
                            Button(onClick = {
                                DatabaseManager.deleteUser(user.login)
                                users = DatabaseManager.getAllUsers() // Синхронизируем UI список
                            }) { Text("Удалить") }
                        }
                    }
                }
            }
        }

        // Кнопка разлогина (возврат на LoginScreen)
        Button(onClick = onLogout, modifier = Modifier.align(Alignment.End)) { Text("Выйти") }
    }
}