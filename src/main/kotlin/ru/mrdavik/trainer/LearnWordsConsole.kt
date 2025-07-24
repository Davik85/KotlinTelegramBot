package ru.mrdavik.trainer

import ru.mrdavik.trainer.trainer.LearnWordsTrainer
import ru.mrdavik.trainer.trainer.DEFAULT_WORDS_FILE

fun main() {
    LearnWordsTrainer.initializeDemoWordsIfNeeded(DEFAULT_WORDS_FILE)
    val trainer = LearnWordsTrainer(fileName = DEFAULT_WORDS_FILE)

    println("Словарь:")
    trainer.getDictionary().forEach {
        println("${it.original} — ${it.translate}, правильных ответов: ${it.correctAnswersCount}")
    }

    val MENU_PROMPT = """
        Меню:
        1 – Учить слова
        2 – Статистика
        3 – Сбросить прогресс
        0 – Выход
    """.trimIndent()

    val WRONG_INPUT_MSG = "Введите число 1, 2, 3 или 0"

    while (true) {
        println(MENU_PROMPT)
        print("Введите пункт меню: ")
        when (readlnOrNull()?.trim()) {
            "1" -> studyWords(trainer)
            "2" -> println(trainer.getStatistics())
            "3" -> {
                trainer.resetProgress()
                println("Прогресс сброшен!")
            }
            "0" -> {
                println("Выход из программы...")
                break
            }
            else -> println(WRONG_INPUT_MSG)
        }
        println()
    }
}

private fun studyWords(trainer: LearnWordsTrainer) {
    while (true) {
        val question = trainer.nextQuestion()
        if (question == null) {
            println("Все слова в словаре выучены")
            break
        }
        println("\n${question.options[question.correctIndex].original}:")
        question.options.forEachIndexed { i, variant ->
            println(" ${i + 1} - ${variant.translate}")
        }
        println("----------")
        println(" 0 - Меню")
        print("Ваш ответ (номер): ")
        val userAnswerInput = readlnOrNull()?.trim()
        if (userAnswerInput == "0") break
        val userAnswer = userAnswerInput?.toIntOrNull()
        if (userAnswer == null || userAnswer !in 1..question.options.size) {
            println("Введите номер варианта ответа от 1 до ${question.options.size}, или 0 для возврата в меню.")
            continue
        }
        if (trainer.checkAnswer(userAnswer - 1)) {
            println("Правильно!")
        } else {
            val correct = question.options[question.correctIndex]
            println("Неправильно! ${correct.original} – это ${correct.translate}")
        }
    }
}
