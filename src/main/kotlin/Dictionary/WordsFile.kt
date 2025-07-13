package org.example.WordsFile

import Trainer.LearnWordsTrainer

fun main() {
    LearnWordsTrainer.initializeDemoWordsIfNeeded()
    val trainer = LearnWordsTrainer()

    println("Словарь:")
    trainer.getDictionary().forEach {
        println("${it.original} — ${it.translate}, правильных ответов: ${it.correctAnswersCount}")
    }

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
            "1" -> {
                while (true) {
                    val question = trainer.nextQuestion()
                    if (question == null) {
                        println("Все слова в словаре выучены")
                        break
                    }
                    println("\n${question.options[question.correctIndex].original}:")
                    question.options.map { it.translate }.forEachIndexed { i, variant ->
                        println(" ${i + 1} - $variant")
                    }
                    println("----------")
                    println(" 0 - Меню")
                    print("Ваш ответ (номер): ")
                    val userAnswerInput = readLine()?.trim()
                    if (userAnswerInput == "0") break
                    val userAnswer = userAnswerInput?.toIntOrNull()
                    if (userAnswer == null || userAnswer !in 1..question.options.size) {
                        println("Введите номер варианта ответа от 1 до ${question.options.size}, или 0 для возврата в меню.")
                        continue
                    }
                    if (trainer.checkAnswer(question, userAnswer)) {
                        println("Правильно!")
                        trainer.incrementCorrect(question)
                    } else {
                        println("Неправильно! ${question.options[question.correctIndex].original} – это ${question.options[question.correctIndex].translate}")
                    }
                }
            }

            "2" -> {
                val stats = trainer.getStatistics()
                println(stats)
            }

            "0" -> {
                println("Выход из программы...")
                break
            }

            else -> println("Введите число 1, 2 или 0")
        }
        println()
    }
}
