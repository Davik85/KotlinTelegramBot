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

class LearnWordsTrainer(
    private val fileName: String = WORDS_FILE
) {
    private val dictionary: MutableList<Word> = loadDictionary()
    private var currentQuestionWords: List<Word> = emptyList()
    private var currentCorrectWord: Word? = null

    fun printDictionary() {
        println("Словарь:")
        dictionary.forEach { println("${it.original} — ${it.translate}, правильных ответов: ${it.correctAnswersCount}") }
    }

    fun printStats() {
        val totalCount = dictionary.size
        val learnedCount = dictionary.count { it.correctAnswersCount >= LEARNED_THRESHOLD }
        val percent = if (totalCount == 0) 0 else (learnedCount * 100 / totalCount)
        println("Выучено $learnedCount из $totalCount слов | $percent%")
    }

    fun studyLoop() {
        while (true) {
            val notLearnedList = dictionary.filter { it.correctAnswersCount < LEARNED_THRESHOLD }
            if (notLearnedList.isEmpty()) {
                println("Все слова в словаре выучены")
                return
            }
            prepareQuestion(notLearnedList)
            printQuestion()
            val userAnswerInput = readLine()?.trim()

            when {
                userAnswerInput == "0" -> return
                userAnswerInput?.toIntOrNull() !in 1..currentQuestionWords.size -> {
                    println("Введите номер варианта ответа от 1 до ${currentQuestionWords.size}, или 0 для возврата в меню.")
                }
                else -> {
                    checkAnswer(userAnswerInput!!.toInt())
                }
            }
        }
    }

    private fun prepareQuestion(notLearnedList: List<Word>) {
        val mainPart = notLearnedList.shuffled().take(ANSWER_OPTIONS_COUNT)
        val additionalCount = (ANSWER_OPTIONS_COUNT - mainPart.size).coerceAtLeast(0)
        val additional = dictionary
            .filter { it.correctAnswersCount >= LEARNED_THRESHOLD }
            .shuffled()
            .take(additionalCount)
        currentQuestionWords = (mainPart + additional).distinct().shuffled().take(ANSWER_OPTIONS_COUNT)
        currentCorrectWord = currentQuestionWords.random()
    }

    private fun printQuestion() {
        println("\n${currentCorrectWord?.original}:")
        val variants = currentQuestionWords.map { it.translate }.shuffled()
        currentQuestionWords = currentQuestionWords.sortedBy { variants.indexOf(it.translate) } // sync with printed variants
        variants.forEachIndexed { i, variant -> println(" ${i + 1} - $variant") }
        println("----------")
        println(" 0 - Меню")
        print("Ваш ответ (номер): ")
    }

    private fun checkAnswer(answer: Int) {
        val correctAnswerId = currentQuestionWords.indexOf(currentCorrectWord) + 1
        if (answer == correctAnswerId) {
            println("Правильно!")
            currentCorrectWord?.correctAnswersCount = (currentCorrectWord?.correctAnswersCount ?: 0) + 1
            saveDictionary()
        } else {
            println("Неправильно! ${currentCorrectWord?.original} – это ${currentCorrectWord?.translate}")
        }
    }

    private fun loadDictionary(): MutableList<Word> {
        val file = File(fileName)
        if (!file.exists()) return mutableListOf()
        return file.readLines().mapNotNull { line ->
            val parts = line.split("|")
            val original = parts.getOrNull(0)?.trim() ?: return@mapNotNull null
            val translate = parts.getOrNull(1)?.trim() ?: ""
            val correctAnswers = parts.getOrNull(2)?.toIntOrNull() ?: 0
            Word(original, translate, correctAnswers)
        }.toMutableList()
    }

    private fun saveDictionary() {
        val file = File(fileName)
        file.writeText("")
        dictionary.forEach { word ->
            file.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }

    companion object {
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
    }
}

