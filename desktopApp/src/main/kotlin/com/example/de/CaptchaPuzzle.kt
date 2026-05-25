package com.example.de

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Класс бизнес-логики Капчи (Графический пазл 2х2).
 * Отвечает за хранение текущего перемешанного порядка фрагментов,
 * обработку механики кликов-перестановок (Swap) и проверку правильности сборки.
 */
class CaptchaPuzzle {
    // Правильный эталонный порядок картинок-фрагментов (f1.png, f2.png, f3.png, f4.png)
    private val correctOrder = listOf(1, 2, 3, 4)

    /**
     * Текущий порядок фрагментов в сетке на экране.
     * Используется SnapshotStateList, чтобы Compose автоматически перерисовывал
     * интерфейс при обмене фрагментов местами (метод swap).
     */
    val currentOrder: SnapshotStateList<Int> = mutableStateListOf(*correctOrder.shuffled().toTypedArray())

    // Хранит индекс первой нажатой плитки. Если null — плитка еще не выбрана.
    var selectedIndex: Int? by mutableStateOf(null)

    // Статус: собран ли пазл успешно в данный момент
    var isPassed: Boolean by mutableStateOf(false)

    /**
     * Механика выбора и обмена местами (перемещения плиток пазла).
     * @param index Индекс ячейки сетки (от 0 до 3), по которой кликнул пользователь.
     */
    fun handleCellClick(index: Int) {
        if (isPassed) return // Если капча уже успешно пройдена, блокируем любые клики

        val prev = selectedIndex
        when {
            prev == null -> {
                // Если это первый клик — просто выделяем текущую ячейку рамкой
                selectedIndex = index
            }
            prev == index -> {
                // Если кликнули повторно по той же самой ячейке — снимаем выделение
                selectedIndex = null
            }
            else -> {
                // Если выделена одна ячейка, а кликают по другой — меняем их содержимое местами (SWAP)
                val temp = currentOrder[prev]
                currentOrder[prev] = currentOrder[index]
                currentOrder[index] = temp
                selectedIndex = null // Сбрасываем выделение после обмена
            }
        }
    }

    /**
     * Проверка текущего порядка на экране с эталоном.
     * Принудительно приводит стейт-список к стандартному List для корректного сравнения по значениям.
     */
    fun check(): Boolean {
        isPassed = currentOrder.toList() == correctOrder
        return isPassed
    }

    /**
     * Сброс состояния пазла. Вызывается при повторных попытках входа,
     * чтобы заново перемешать картинки и очистить старые выделения.
     */
    fun resetForNewAttempt() {
        currentOrder.clear()
        currentOrder.addAll(correctOrder.shuffled()) // Заново случайно перемешиваем
        selectedIndex = null
        isPassed = false
    }
}