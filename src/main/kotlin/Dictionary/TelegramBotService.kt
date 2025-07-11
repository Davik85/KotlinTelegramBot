package org.example.Dictionary

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TELEGRAM_API_BASE_URL = "https://api.telegram.org"

class TelegramBotService(private val botToken: String) {
    private val client = HttpClient.newBuilder().build()

    fun getUpdates(offset: Int): String {
        val url = "$TELEGRAM_API_BASE_URL/bot$botToken/getUpdates?offset=$offset"
        val request = HttpRequest.newBuilder().uri(URI.create(url)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Long, text: String): String {
        val url = "$TELEGRAM_API_BASE_URL/bot$botToken/sendMessage?chat_id=$chatId&text=${encodeURIComponent(text)}"
        val request = HttpRequest.newBuilder().uri(URI.create(url)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMenu(chatId: Long): String {
        val sendMessageUrl = "$TELEGRAM_API_BASE_URL/bot$botToken/sendMessage"
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучить слова",
                                "callback_data": "learn_words_clicked"
                            },
                            {
                                "text": "Статистика",
                                "callback_data": "statistics_clicked"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(URI.create(sendMessageUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun encodeURIComponent(text: String): String =
        java.net.URLEncoder.encode(text, "UTF-8")
}
