package com.example.de

import kotlinx.serialization.Serializable

/**
 * Класс-обертка для десериализации JSON-ответов от API симулятора.
 * Соответствует структуре ответа из ТЗ: { "value": "строковое_значение" }
 */
@Serializable
data class ApiResponse(val value: String)

/**
 * Синглтон (object) для централизованного управления базовыми URL-адресами API.
 * Позволяет динамически переключаться между локальной лабораторной сетью и Интернетом.
 */
object ApiConfig {
    // Константа для адреса внутри лабораторного кабинета (Lab URL)
    const val LAB_URL: String = "http://127.0.0.1:4444/TransferSimulator"

    // Константа для адреса через глобальный Интернет (Internet URL)
    const val INTERNET_URL: String = "http://prb.sylas.ru/TransferSimulator"

    // Динамический флаг: true — использовать локальный адрес, false — интернет-адрес
    // Изменяется через Checkbox на экране пользователя (UserScreen)
    var useLabAddress: Boolean = true

    /**
     * Геттер, который автоматически возвращает нужный базовый URL
     * в зависимости от текущего состояния флага useLabAddress.
     */
    val baseUrl: String get() = if (useLabAddress) LAB_URL else INTERNET_URL
}