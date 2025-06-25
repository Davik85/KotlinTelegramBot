package Trainer

import java.io.File

const val DEFAULT_LEARNED_THRESHOLD = 3
const val DEFAULT_ANSWER_OPTIONS_COUNT = 4
const val DEFAULT_WORDS_FILE = "words.txt"

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

data class Question(
    val options: List<Word>,
    val correctIndex: Int
)

class LearnWordsTrainer(
    val learnedThreshold: Int = DEFAULT_LEARNED_THRESHOLD,
    val answerOptionsCount: Int = DEFAULT_ANSWER_OPTIONS_COUNT,
    private val fileName: String = DEFAULT_WORDS_FILE
) {
    private val dictionary: MutableList<Word> = loadDictionary().toMutableList()

    fun getStats(): Triple<Int, Int, Int> {
        val total = dictionary.size
        val learned = dictionary.count { it.correctAnswersCount >= learnedThreshold }
        val percent = if (total == 0) 0 else (learned * 100 / total)
        return Triple(total, learned, percent)
    }

    fun nextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < learnedThreshold }
        if (notLearnedList.isEmpty()) return null
        val mainPart = notLearnedList.shuffled().take(answerOptionsCount)
        val additionalCount = (answerOptionsCount - mainPart.size).coerceAtLeast(0)
        val additional = dictionary
            .filter { it.correctAnswersCount >= learnedThreshold }
            .shuffled()
            .take(additionalCount)
        val options = (mainPart + additional).distinct().shuffled().take(answerOptionsCount)
        val correctWord = options.random()
        val correctIndex = options.indexOf(correctWord)
        return Question(options, correctIndex)
    }

    fun checkAnswer(question: Question, userAnswer: Int): Boolean {
        return userAnswer == (question.correctIndex + 1)
    }

    fun incrementCorrect(question: Question) {
        question.options[question.correctIndex].correctAnswersCount++
        saveDictionary()
    }

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
        file.writeText("")
        dictionary.forEach { word ->
            file.appendText("${word.original}|${word.translate}|${word.correctAnswersCount}\n")
        }
    }

    companion object {
        fun initializeDemoWordsIfNeeded() {
            val wordsFile = File("words.txt")
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

