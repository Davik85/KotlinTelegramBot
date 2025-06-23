package org.example.WordsFile

import java.io.File

const val LEARNED_THRESHOLD = 3
const val ANSWER_OPTIONS_COUNT = 4

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

fun saveDictionary(dictionary: List<Word>, fileName: String = "words.txt") {
    val file = File(fileName)
    file.writeText(
        dictionary.joinToString("\n") { "${it.original}|${it.translate}|${it.correctAnswersCount}" }
    )
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
            "1" -> {
                while (true) {
                    val notLearnedList = dictionary.filter { it.correctAnswersCount < LEARNED_THRESHOLD }
                    if (notLearnedList.isEmpty()) {
                        println("Все слова в словаре выучены")
                        break
                    }

                    val mainPart = notLearnedList.shuffled().take(ANSWER_OPTIONS_COUNT)
                    val additionalCount = (ANSWER_OPTIONS_COUNT - mainPart.size).coerceAtLeast(0)
                    val additional = dictionary
                        .filter { it.correctAnswersCount >= LEARNED_THRESHOLD }
                        .shuffled()
                        .take(additionalCount)

                    val questionWords = (mainPart + additional)
                        .distinct()
                        .shuffled()
                        .take(ANSWER_OPTIONS_COUNT)

                    val correctWord = questionWords.random()
                    val variants = questionWords.map { it.translate }.shuffled()

                    val correctAnswerId = variants.indexOf(correctWord.translate) + 1 // +1 потому что вывод от 1

                    println("\n${correctWord.original}:")
                    for ((index, variant) in variants.withIndex()) {
                        println(" ${index + 1} - $variant")
                    }
                    println("----------")
                    println(" 0 - Меню")

                    print("Ваш ответ (номер): ")
                    val userAnswerInput = readLine()?.trim()
                    if (userAnswerInput == "0") break

                    val userAnswer = userAnswerInput?.toIntOrNull()
                    if (userAnswer == null || userAnswer !in 1..variants.size) {
                        println("Введите номер варианта ответа от 1 до ${variants.size}, или 0 для возврата в меню.")
                        continue
                    }

                    if (userAnswer == correctAnswerId) {
                        println("Правильно!")
                        correctWord.correctAnswersCount++
                        saveDictionary(dictionary)
                    } else {
                        println("Неправильно! ${correctWord.original} – это ${correctWord.translate}")
                    }
                }
            }

            "2" -> {
                val totalCount = dictionary.size
                val learnedCount = dictionary.filter { it.correctAnswersCount >= LEARNED_THRESHOLD }.size
                val percent = if (totalCount == 0) 0 else (learnedCount * 100 / totalCount)
                println("Выучено $learnedCount из $totalCount слов | $percent%")
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



