package dictionary

fun main() {
    LearnWordsTrainer.initializeDemoWordsIfNeeded()
    val trainer = LearnWordsTrainer()
    println("Словарь:")
    trainer.getDictionary().forEach {
        println("${it.original} — ${it.translate}, правильных ответов: ${it.correctAnswersCount}")
    }

    val MENU_PROMPT = """
        Меню:
        1 – Учить слова
        2 – Статистика
        0 – Выход
    """.trimIndent()

    val WRONG_INPUT_MSG = "Введите число 1, 2 или 0"

    loop@ while (true) {
        println(MENU_PROMPT)
        print("Введите пункт меню: ")
        when (readLine()?.trim()) {
            "1" -> studyWords(trainer)
            "2" -> println(trainer.getStatistics())
            "0" -> {
                println("Выход из программы...")
                break@loop
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
        trainer.setCurrentQuestion(question)

        println("\n${question.options[question.correctIndex].original}:")
        question.options.forEachIndexed { i, variant ->
            println(" ${i + 1} - ${variant.translate}")
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
        if (trainer.checkAnswer(userAnswer - 1)) {
            println("Правильно!")
            trainer.incrementCorrect()
        } else {
            val correct = question.options[question.correctIndex]
            println("Неправильно! ${correct.original} – это ${correct.translate}")
        }
    }
}
