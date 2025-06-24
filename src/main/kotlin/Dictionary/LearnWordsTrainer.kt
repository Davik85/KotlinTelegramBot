package Trainer

import java.io.File

const val LEARNED_THRESHOLD = 3
const val ANSWER_OPTIONS_COUNT = 4
const val WORDS_FILE = "words.txt"

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

object DictionaryRepository {
    fun load(): MutableList<Word> {
        val file = File(WORDS_FILE)
        if (!file.exists()) return mutableListOf()
        return file.readLines().mapNotNull { line ->
            val parts = line.split("|")
            val original = parts.getOrNull(0)?.trim() ?: return@mapNotNull null
            val translate = parts.getOrNull(1)?.trim() ?: ""
            val correctAnswers = parts.getOrNull(2)?.toIntOrNull() ?: 0
            Word(original, translate, correctAnswers)
        }.toMutableList()
    }

    fun save(dictionary: List<Word>) {
        val file = File(WORDS_FILE)
        file.writeText("")
        dictionary.forEach { word ->
            file.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }
}

object TrainerStats {
    fun print(dictionary: List<Word>) {
        val totalCount = dictionary.size
        val learnedCount = dictionary.count { it.correctAnswersCount >= LEARNED_THRESHOLD }
        val percent = if (totalCount == 0) 0 else (learnedCount * 100 / totalCount)
        println("Выучено $learnedCount из $totalCount слов | $percent%")
    }
}

object TrainerQuiz {
    fun start(dictionary: MutableList<Word>) {
        while (true) {
            val notLearnedList = dictionary.filter { it.correctAnswersCount < LEARNED_THRESHOLD }
            if (notLearnedList.isEmpty()) {
                println("Все слова в словаре выучены")
                return
            }
            val questionWords = getQuestionWords(dictionary, notLearnedList)
            val correctWord = questionWords.random()
            val variants = questionWords.map { it.translate }.shuffled()
            val correctAnswerId = variants.indexOf(correctWord.translate) + 1

            printQuestion(correctWord, variants)
            val userAnswerInput = readLine()?.trim()

            if (userAnswerInput != null) {
                when {
                    userAnswerInput == "0" -> return
                    userAnswerInput?.toIntOrNull() !in 1..variants.size -> {
                        println("Введите номер варианта ответа от 1 до ${variants.size}, или 0 для возврата в меню.")
                    }

                    userAnswerInput.toInt() == correctAnswerId -> {
                        println("Правильно!")
                        correctWord.correctAnswersCount++
                        DictionaryRepository.save(dictionary)
                    }

                    else -> println("Неправильно! ${correctWord.original} – это ${correctWord.translate}")
                }
            }
        }
    }

    private fun getQuestionWords(dictionary: List<Word>, notLearnedList: List<Word>): List<Word> {
        val mainPart = notLearnedList.shuffled().take(ANSWER_OPTIONS_COUNT)
        val additionalCount = (ANSWER_OPTIONS_COUNT - mainPart.size).coerceAtLeast(0)
        val additional = dictionary
            .filter { it.correctAnswersCount >= LEARNED_THRESHOLD }
            .shuffled()
            .take(additionalCount)
        return (mainPart + additional).distinct().shuffled().take(ANSWER_OPTIONS_COUNT)
    }

    private fun printQuestion(word: Word, variants: List<String>) {
        println("\n${word.original}:")
        variants.forEachIndexed { i, variant -> println(" ${i + 1} - $variant") }
        println("----------")
        println(" 0 - Меню")
        print("Ваш ответ (номер): ")
    }
}

fun initializeDemoWordsIfNeeded() {
    val wordsFile = File(WORDS_FILE)
    if (!wordsFile.exists() || wordsFile.readText().isBlank()) {
        wordsFile.writeText(
            """
            hello|привет|0
            dog|собака|0
            cat|кошка|0
            table|стол|0
            chair|стул|0
            house|дом|0
            bye|пока|0
            tomorrow|завтра|0
            yesterday|вчера|0
            monday|понедельник|0
            tuesday|вторник|0
            """.trimIndent()
        )
    }
}
