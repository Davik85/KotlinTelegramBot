package org.example.Dictionary


import java.io.File

data class Word(
    val english: String,
    val russian: String,
    var correctAnswersCount: Int = 0
)

fun main() {
    val wordsFile = File("words.txt")

    val dictionary = mutableListOf<Word>()

    val lines = wordsFile.readLines()

    for (line in lines) {
        val parts = line.split("|")

        val english = parts.getOrNull(0)?.trim() ?: continue
        val russian = parts.getOrNull(1)?.trim() ?: ""
        val correctAnswers = parts.getOrNull(2)?.toIntOrNull() ?: 0

        val word = Word(english, russian, correctAnswers)
        dictionary.add(word)
    }

    println("Словарь:")
    for (word in dictionary) {
        println("${word.english} — ${word.russian}, правильных ответов: ${word.correctAnswersCount}")
    }
}
