package org.example.WordsFile

import Trainer.LearnWordsTrainer

fun main() {
    LearnWordsTrainer.initializeDemoWordsIfNeeded()
    val trainer = LearnWordsTrainer()

    trainer.printDictionary()

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
            "1" -> trainer.studyLoop()
            "2" -> trainer.printStats()
            "0" -> {
                println("Выход из программы...")
                break
            }
            else -> println("Введите число 1, 2 или 0")
        }
        println()
    }
}





