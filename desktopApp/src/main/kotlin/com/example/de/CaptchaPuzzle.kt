package com.example.de

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

class CaptchaPuzzle {
    private val correctOrder = listOf(1, 2, 3, 4) // Правильный порядок картинок

    // Текущий перемешанный порядок элементов на экране
    val currentOrder: SnapshotStateList<Int> = mutableStateListOf(*correctOrder.shuffled().toTypedArray())

    var selectedIndex: Int? by mutableStateOf(null)
    var isPassed: Boolean by mutableStateOf(false)

    // Обработка клика по фрагменту пазла
    fun handleCellClick(index: Int) {
        if (isPassed) return
        val prev = selectedIndex
        when {
            prev == null -> selectedIndex = index // Выбираем первую ячейку
            prev == index -> selectedIndex = null // Кликнули по той же — снимаем выделение
            else -> {
                // Кликнули по второй ячейке — меняем их местами
                val temp = currentOrder[prev]
                currentOrder[prev] = currentOrder[index]
                currentOrder[index] = temp
                selectedIndex = null
            }
        }
    }

    // Проверка правильности сборки
    fun check(): Boolean {
        isPassed = currentOrder.toList() == correctOrder
        return isPassed
    }

    // Перемешивание заново при открытии окна
    fun resetForNewAttempt() {
        currentOrder.clear()
        currentOrder.addAll(correctOrder.shuffled())
        selectedIndex = null
        isPassed = false
    }
}