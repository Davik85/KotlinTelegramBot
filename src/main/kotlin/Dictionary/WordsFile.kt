package org.example.WordsFile

import Trainer.DictionaryRepository
import Trainer.TrainerQuiz
import Trainer.TrainerStats
import Trainer.initializeDemoWordsIfNeeded

fun main() {
    initializeDemoWordsIfNeeded()
    val dictionary = DictionaryRepository.load()

    println("Словарь:")
    dictionary.forEach { println("${it.original} — ${it.translate}, правильных ответов: ${it.correctAnswersCount}") }

    while (true) {
        println(
            """
            Меню:
            1 – Учить слова
            2 – Статистика
            0 – Выход
            """.trimIndent()
        )
        print("Введите пункт меню: ")
        when (readLine()?.trim()) {
            "1" -> TrainerQuiz.start(dictionary)
            "2" -> TrainerStats.print(dictionary)
            "0" -> {
                println("Выход из программы...")
                break
            }
            else -> println("Введите число 1, 2 или 0")
        }
        println()
    }
}




