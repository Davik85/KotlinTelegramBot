package org.example.WordsFile

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

fun loadDictionary(): MutableList<Word> {
    val file = File("words.txt")
    val dictionary = mutableListOf<Word>()

    if (!file.exists()) {
        println("Файл словаря не найден: words.txt")
        return dictionary
    }

    val lines = file.readLines()
    for (line in lines) {
        val parts = line.split("|")
        val original = parts.getOrNull(0)?.trim() ?: continue
        val translate = parts.getOrNull(1)?.trim() ?: ""
        val correctAnswers = parts.getOrNull(2)?.toIntOrNull() ?: 0

        dictionary.add(Word(original, translate, correctAnswers))
    }

    return dictionary
}

fun main() {
    val wordsFile = File("words.txt")
    if (!wordsFile.exists() || wordsFile.readText().isBlank()) {
        wordsFile.writeText("hello|привет|0\n")
        wordsFile.appendText("dog|собака|0\n")
        wordsFile.appendText("cat|кошка|0\n")
    }

    val dictionary = loadDictionary()

    println("Словарь:")
    for (word in dictionary) {
        println("${word.original} — ${word.translate}, правильных ответов: ${word.correctAnswersCount}")
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
        val input = readLine()?.trim()

        when (input) {
            "1" -> println("Вы выбрали: Учить слова")
            "2" -> println("Вы выбрали: Статистика")
            "0" -> {
                println("Выход из программы...")
                break
            }
            else -> println("Введите число 1, 2 или 0")
        }

        println()
    }
}

