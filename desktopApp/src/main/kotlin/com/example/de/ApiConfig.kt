package com.example.de

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(val value: String)

object ApiConfig {
    const val LAB_URL: String = "http://127.0.0.1:4444/TransferSimulator"
    const val INTERNET_URL: String = "http://prb.sylas.ru/TransferSimulator"

    var useLabAddress: Boolean = true

    // Автоматический подбор ссылки в зависимости от значения чекбокса на экране пользователя
    val baseUrl: String get() = if (useLabAddress) LAB_URL else INTERNET_URL
}