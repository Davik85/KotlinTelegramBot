package org.example.Dictionary

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0
    val bot = TelegramBotService(botToken)

    while (true) {
        Thread.sleep(2000)
        val updates = bot.getUpdates(updateId)
        println(updates)

        val updateIdRegex = "\"update_id\":(\\d+)".toRegex()
        val chatIdRegex = "\"chat\":\\{\"id\":(\\d+)".toRegex()
        val textRegex = "\"text\":\"([^\"]+)\"".toRegex()

        val updateIdMatch = updateIdRegex.find(updates)
        val chatIdMatch = chatIdRegex.find(updates)
        val textMatch = textRegex.find(updates)

        if (updateIdMatch != null && chatIdMatch != null && textMatch != null) {
            updateId = updateIdMatch.groupValues[1].toInt() + 1
            val chatId = chatIdMatch.groupValues[1].toLong()
            val text = textMatch.groupValues[1]

            println("chatId: $chatId, text: $text")

            if (text.equals("hello", ignoreCase = true)) {
                bot.sendMessage(chatId, "Hello")
            }
        }
    }
}
