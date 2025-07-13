package org.example.Dictionary

import Trainer.LearnWordsTrainer

const val UPDATE_ID_PATTERN = """"update_id":\s*(\d+)"""
const val CHAT_ID_PATTERN = """"chat":\s*\{[^}]*"id":\s*(-?\d+)"""
const val TEXT_PATTERN = """"text":"(.+?)""""
const val DATA_PATTERN = """"data":"(.+?)""""

const val CALLBACK_STATISTICS_CLICKED = "statistics_clicked"
const val CALLBACK_LEARN_WORDS_CLICKED = "learn_words_clicked"

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
            when (data.lowercase()) {
                CALLBACK_STATISTICS_CLICKED -> {
                    val stats = trainer.getStatistics()
                    botService.sendMessage(chatId, stats)
                }

                CALLBACK_LEARN_WORDS_CLICKED -> {
                    botService.sendMessage(chatId, "Давай начнем изучение новых слов!")
                }
            }
        }
    }
}

