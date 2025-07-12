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

        val receivedUpdateId = updateIdRegex.find(updates)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: continue
        updateId = receivedUpdateId + 1

        val chatId = chatIdRegex.find(updates)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: continue
        val text = textRegex.find(updates)?.groupValues?.getOrNull(1)
        val data = dataRegex.find(updates)?.groupValues?.getOrNull(1)

        if (text != null) {
            println("Получено сообщение: $text (chatId = $chatId)")
            when {
                text.equals("hello", ignoreCase = true) -> botService.sendMessage(chatId, "Hello")
                text.equals("/menu", ignoreCase = true) || text.equals(
                    "menu",
                    ignoreCase = true
                ) -> botService.sendMenu(chatId)
            }
        }

        if (data != null) {
            when (data.lowercase()) {
                "statistics_clicked" -> botService.sendMessage(chatId, "Выучено 10 из 10 слов | 100%")
                "learn_words_clicked" -> botService.sendMessage(chatId, "Давай начнем изучение новых слов!")
            }
        }
    }
}

