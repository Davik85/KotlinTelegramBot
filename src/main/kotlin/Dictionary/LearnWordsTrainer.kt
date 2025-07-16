package dictionary

import java.io.File

const val DEFAULT_LEARNED_THRESHOLD = 3
const val DEFAULT_ANSWER_OPTIONS_COUNT = 4
const val DEFAULT_WORDS_FILE = "words.txt"

data class Word(val original: String, val translate: String, var correctAnswersCount: Int = 0)
data class Question(val options: List<Word>, val correctIndex: Int)

class LearnWordsTrainer(
    private val learnedThreshold: Int = DEFAULT_LEARNED_THRESHOLD,
    private val answerOptionsCount: Int = DEFAULT_ANSWER_OPTIONS_COUNT,
    private val fileName: String = DEFAULT_WORDS_FILE
) {
    private val dictionary: List<Word>
    private var currentQuestion: Question? = null

    init {
        initializeDemoWordsIfNeeded(fileName)
        dictionary = loadDictionary()
    }

    fun getStatistics(): String {
        val total = dictionary.size
        val learned = dictionary.count { it.correctAnswersCount >= learnedThreshold }
        val percent = if (total == 0) 0 else (learned * 100 / total)
        return "Выучено $learned из $total слов | $percent%"
    }

    fun nextQuestion(): Question? {
        val notLearned = dictionary.filter { it.correctAnswersCount < learnedThreshold }
        if (notLearned.isEmpty()) {
            currentQuestion = null
            return null
        }
        val mainPart = notLearned.shuffled().take(answerOptionsCount)
        val additional = dictionary
            .filter { it.correctAnswersCount >= learnedThreshold }
            .shuffled()
            .take((answerOptionsCount - mainPart.size).coerceAtLeast(0))
        val options = (mainPart + additional).distinct().shuffled().take(answerOptionsCount)
        val correctWord = mainPart.random()
        val correctIndex = options.indexOf(correctWord)
        currentQuestion = Question(options, correctIndex)
        return currentQuestion
    }

    fun checkAnswer(userAnswerIndex: Int): Boolean {
        val question = currentQuestion ?: return false
        val isCorrect = userAnswerIndex == question.correctIndex
        if (isCorrect) {
            question.options[question.correctIndex].correctAnswersCount++
            saveDictionary()
        }
        return isCorrect
    }

    fun getCurrentQuestion(): Question? = currentQuestion
    fun getDictionary(): List<Word> = dictionary

    private fun loadDictionary(): List<Word> {
        val file = File(fileName)
        if (!file.exists()) return emptyList()
        return file.readLines().mapNotNull { line ->
            val parts = line.split("|")
            val original = parts.getOrNull(0)?.trim() ?: return@mapNotNull null
            val translate = parts.getOrNull(1)?.trim() ?: ""
            val correctAnswers = parts.getOrNull(2)?.toIntOrNull() ?: 0
            Word(original, translate, correctAnswers)
        }
    }

    private fun saveDictionary() {
        val file = File(fileName)
        file.bufferedWriter().use { writer ->
            dictionary.forEach { word ->
                writer.write("${word.original}|${word.translate}|${word.correctAnswersCount}")
                writer.newLine()
            }
        }
    }

    companion object {
        fun initializeDemoWordsIfNeeded(fileName: String) {
            val file = File(fileName)
            if (!file.exists() || file.readText().isBlank()) {
                file.writeText(
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
