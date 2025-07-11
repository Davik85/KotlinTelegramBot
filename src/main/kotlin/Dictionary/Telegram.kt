package org.example.Dictionary

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

    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(updateId)
        println(updates)

        val updateIdMatch = updateIdRegex.find(updates)
        if (updateIdMatch != null) {
            updateId = updateIdMatch.groupValues[1].toInt() + 1
        }

        val chatIdMatch = chatIdRegex.find(updates)
        val chatId = chatIdMatch?.groupValues?.get(1)?.toLongOrNull()
        val textMatch = textRegex.find(updates)
        val text = textMatch?.groupValues?.get(1)
        val dataMatch = dataRegex.find(updates)
        val data = dataMatch?.groupValues?.get(1)

        if (text != null && chatId != null) {
            println("Получено сообщение: $text (chatId = $chatId)")
            when {
                text.equals("hello", ignoreCase = true) -> {
                    botService.sendMessage(chatId, "Hello")
                }
                text.equals("/menu", ignoreCase = true) || text.equals("menu", ignoreCase = true) -> {
                    botService.sendMenu(chatId)
                }
            }
        }

        if (data != null && chatId != null) {
            when (data.lowercase()) {
                "statistics_clicked" -> botService.sendMessage(chatId, "Выучено 10 из 10 слов | 100%")
                "learn_words_clicked" -> botService.sendMessage(chatId, "Давай начнем изучение новых слов!")
            }
        }
    }
}
