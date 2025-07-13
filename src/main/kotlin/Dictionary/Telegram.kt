package org.example.Dictionary

import Trainer.LearnWordsTrainer
import Trainer.Question

const val UPDATE_ID_PATTERN = """"update_id":\s*(\d+)"""
const val CHAT_ID_PATTERN = """"chat":\s*\{[^}]*"id":\s*(-?\d+)"""
const val TEXT_PATTERN = """"text":"(.+?)""""
const val DATA_PATTERN = """"data":"(.+?)""""

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0

    val updateIdRegex = UPDATE_ID_PATTERN.toRegex()
    val chatIdRegex = CHAT_ID_PATTERN.toRegex()
    val textRegex = TEXT_PATTERN.toRegex()
    val dataRegex = DATA_PATTERN.toRegex()

    val botService = TelegramBotService(botToken)
    LearnWordsTrainer.initializeDemoWordsIfNeeded()
    val trainer = LearnWordsTrainer()
    var lastQuestion: Question? = null

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
            when {
                text.equals("hello", ignoreCase = true) -> botService.sendMessage(chatId, "Hello")
                text.equals("/menu", ignoreCase = true) || text.equals("menu", ignoreCase = true) ->
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
                    checkNextQuestionAndSend(trainer, botService, chatId).also {
                        lastQuestion = it
                    }
                }

                data.startsWith(CALLBACK_DATA_ANSWER_PREFIX) && lastQuestion != null -> {
                    val answerIdx = data.removePrefix(CALLBACK_DATA_ANSWER_PREFIX).toIntOrNull()
                    if (answerIdx != null) {
                        if (answerIdx == lastQuestion!!.correctIndex) {
                            botService.sendMessage(chatId, "Правильно!")
                            trainer.incrementCorrect(lastQuestion!!)
                        } else {
                            val correct = lastQuestion!!.options[lastQuestion!!.correctIndex]
                            botService.sendMessage(
                                chatId,
                                "Неправильно! ${correct.original} – это ${correct.translate}"
                            )
                        }
                        lastQuestion = checkNextQuestionAndSend(trainer, botService, chatId)
                    }
                }
            }
        }
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    botService: TelegramBotService,
    chatId: Long
): Question? {
    val question = trainer.nextQuestion()
    if (question == null) {
        botService.sendMessage(chatId, "Все слова в словаре выучены")
        return null
    } else {
        botService.sendQuestion(chatId, question)
        return question
    }
}
