package com.example.de

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform