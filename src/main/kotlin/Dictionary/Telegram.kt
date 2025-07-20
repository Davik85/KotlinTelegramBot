package dictionary

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val COMMAND_MENU = "/menu"
const val COMMAND_START = "/start"
const val COMMAND_MENU_WORD = "menu"

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class TelegramResponse(
    val result: List<Update>
)

@Serializable
data class Message(
    val text: String? = null,
    val chat: Chat
)

@Serializable
data class Chat(
    val id: Long
)

@Serializable
data class CallbackQuery(
    val data: String? = null,
    val message: Message? = null
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboardButton>>
)

@Serializable
data class InlineKeyboardButton(
    val text: String,
    @SerialName("callback_data")
    val callbackData: String
)

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Ошибка: Необходимо передать токен бота в качестве аргумента командной строки.")
        return
    }
    val botToken = args[0]
    var lastUpdateId = 0L

    val botService = TelegramBotService(botToken)

    val trainers = mutableMapOf<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val response: TelegramResponse = botService.getUpdates(lastUpdateId)
        for (update in response.result) {
            lastUpdateId = maxOf(lastUpdateId, update.updateId + 1)
            val chatId: Long = update.message?.chat?.id
                ?: update.callbackQuery?.message?.chat?.id
                ?: continue

            val trainer = trainers.getOrPut(chatId) {
                LearnWordsTrainer(fileName = "words_$chatId.txt")
            }
            val text = update.message?.text
            val data = update.callbackQuery?.data

            if (text?.equals(COMMAND_MENU, ignoreCase = true) == true
                || text?.equals(COMMAND_START, ignoreCase = true) == true
                || text?.equals(COMMAND_MENU_WORD, ignoreCase = true) == true
            ) {
                botService.sendMenu(chatId)
                continue
            }

            when {
                data == CALLBACK_STATISTICS_CLICKED -> {
                    val stats = trainer.getStatistics()
                    botService.sendMessage(chatId, stats)
                }

                data == CALLBACK_LEARN_WORDS_CLICKED -> {
                    val question = trainer.nextQuestion()
                    if (question == null) {
                        botService.sendMessage(chatId, "Все слова в словаре выучены")
                    } else {
                        botService.sendQuestion(chatId, question)
                    }
                }

                data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
                    val answerIdx = data.removePrefix(CALLBACK_DATA_ANSWER_PREFIX).toIntOrNull()
                    if (answerIdx != null) {
                        val isCorrect = trainer.checkAnswer(answerIdx)
                        if (isCorrect) {
                            botService.sendMessage(chatId, "Правильно!")
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
                        val question = trainer.nextQuestion()
                        if (question == null) {
                            botService.sendMessage(chatId, "Все слова в словаре выучены")
                        } else {
                            botService.sendQuestion(chatId, question)
                        }
                    }
                }
            }
        }
    }
}
