package ru.mrdavik.trainer

import ru.mrdavik.trainer.telegram.dto.TelegramResponse
import ru.mrdavik.trainer.telegram.CALLBACK_DATA_ANSWER_PREFIX
import ru.mrdavik.trainer.telegram.CALLBACK_LEARN_WORDS_CLICKED
import ru.mrdavik.trainer.telegram.CALLBACK_RESET_PROGRESS
import ru.mrdavik.trainer.telegram.CALLBACK_STATISTICS_CLICKED
import ru.mrdavik.trainer.trainer.LearnWordsTrainer
import ru.mrdavik.trainer.telegram.TelegramBotService

const val COMMAND_MENU = "/menu"
const val COMMAND_START = "/start"
const val COMMAND_MENU_WORD = "menu"

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
        try {
            Thread.sleep(2000)
            val response: TelegramResponse = botService.getUpdates(lastUpdateId)
            for (update in response.result) {
                try {
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
                        || text?.equals(COMMAND_MENU_WORD, ignoreCase = true) == true) {
                        botService.sendMenu(chatId)
                        continue
                    }

                    when {
                        data == CALLBACK_STATISTICS_CLICKED -> {
                            val stats = trainer.statisticsString()
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
                        data == CALLBACK_RESET_PROGRESS -> {
                            trainer.resetProgress()
                            botService.sendMessage(chatId, "Ваш прогресс сброшен! Начните изучение заново.")
                            botService.sendMenu(chatId)
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
                } catch (e: Exception) {
                    println("ОШИБКА при обработке update: ${e.message}")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            println("ГЛОБАЛЬНАЯ ОШИБКА основного цикла: ${e.message}")
            e.printStackTrace()
            Thread.sleep(5000)
        }
    }
}
