package org.example.Dictionary


import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

fun main() {
    val wordsFile = File("words.txt")

    val dictionary = mutableListOf<Word>()

    val lines = wordsFile.readLines()

    for (line in lines) {
        val parts = line.split("|")

        val original = parts.getOrNull(0)?.trim() ?: continue
        val translate = parts.getOrNull(1)?.trim() ?: ""
        val correctAnswers = parts.getOrNull(2)?.toIntOrNull() ?: 0

        val word = Word(original, translate, correctAnswers)
        dictionary.add(word)
    }

    println("Словарь:")
    for (word in dictionary) {
        println("${word.original} — ${word.translate}, правильных ответов: ${word.correctAnswersCount}")
    }
}
