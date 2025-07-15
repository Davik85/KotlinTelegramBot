package dictionary

const val UPDATE_ID_PATTERN = """"update_id":\s*(\d+)"""
const val CHAT_ID_PATTERN = """"chat":\s*\{[^}]*"id":\s*(-?\d+)"""
const val TEXT_PATTERN = """"text":"(.+?)""""
const val DATA_PATTERN = """"data":"(.+?)""""

const val COMMAND_MENU = "/menu"
const val COMMAND_START = "/start"
const val COMMAND_MENU_WORD = "menu"

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Ошибка: Необходимо передать токен бота в качестве аргумента командной строки.")
        return
    }
    val botToken = args[0]
    var updateId = 0

    val updateIdRegex = UPDATE_ID_PATTERN.toRegex()
    val chatIdRegex = CHAT_ID_PATTERN.toRegex()
    val textRegex = TEXT_PATTERN.toRegex()
    val dataRegex = DATA_PATTERN.toRegex()

    val botService = TelegramBotService(botToken)
    LearnWordsTrainer.initializeDemoWordsIfNeeded()
    val trainer = LearnWordsTrainer()

    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(updateId)
        println(updates)

        val receivedUpdateId = updateIdRegex.find(updates)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: continue
        updateId = receivedUpdateId + 1

        val chatId = chatIdRegex.find(updates)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: continue
        val text = textRegex.find(updates)?.groupValues?.getOrNull(1)
        val data = dataRegex.find(updates)?.groupValues?.getOrNull(1)

        if (text != null) {
            println("Получено сообщение: $text (chatId = $chatId)")
            if (
                text.equals(COMMAND_MENU, ignoreCase = true)
                || text.equals(COMMAND_START, ignoreCase = true)
                || text.equals(COMMAND_MENU_WORD, ignoreCase = true)
            ) {
                botService.sendMenu(chatId)
            }
        }

        if (data != null) {
            when {
                data == CALLBACK_STATISTICS_CLICKED -> {
                    val stats = trainer.getStatistics()
                    botService.sendMessage(chatId, stats)
                }
                data == CALLBACK_LEARN_WORDS_CLICKED -> {
                    trainer.checkNextQuestionAndSend(botService, chatId)
                }
                data.startsWith(CALLBACK_DATA_ANSWER_PREFIX) -> {
                    val answerIdx = data.removePrefix(CALLBACK_DATA_ANSWER_PREFIX).toIntOrNull()
                    if (answerIdx != null) {
                        val isCorrect = trainer.checkAnswer(answerIdx)
                        if (isCorrect) {
                            botService.sendMessage(chatId, "Правильно!")
                            trainer.incrementCorrect()
                        } else {
                            val currentQuestion = trainer.getCurrentQuestion()
                            if (currentQuestion != null) {
                                val correct = currentQuestion.options[currentQuestion.correctIndex]
                                botService.sendMessage(
                                    chatId,
                                    "Неправильно! ${correct.original} – это ${correct.translate}"
                                )
                            }
                        }
                        trainer.checkNextQuestionAndSend(botService, chatId)
                    }
                }
            }
        }
    }
}
