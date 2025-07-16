package dictionary

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TELEGRAM_API_BASE_URL = "https://api.telegram.org"
const val CALLBACK_LEARN_WORDS_CLICKED = "learn_words_clicked"
const val CALLBACK_STATISTICS_CLICKED = "statistics_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

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
        val sendMenuBody = """
            {
                "chat_id": $chatId,
                "text": "Основное меню",
                "reply_markup": {
                    "inline_keyboard": [
                        [
                            {
                                "text": "Изучить слова",
                                "callback_data": "$CALLBACK_LEARN_WORDS_CLICKED"
                            },
                            {
                                "text": "Статистика",
                                "callback_data": "$CALLBACK_STATISTICS_CLICKED"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()

        return sendJson(sendMenuBody)
    }

    fun sendQuestion(chatId: Long, question: Question) {
        val questionText = question.options[question.correctIndex].original
        val keyboard = question.options.mapIndexed { idx, word ->
            """
                {
                    "text": "${word.translate}",
                    "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX$idx"
                }
            """
        }.joinToString(",\n", "[", "]")

        val jsonBody = """
            {
                "chat_id": $chatId,
                "text": "$questionText",
                "reply_markup": {
                    "inline_keyboard": [
                        $keyboard
                    ]
                }
            }
        """.trimIndent()

        sendJson(jsonBody)
    }

    private fun sendJson(jsonBody: String): String {
        val url = "$TELEGRAM_API_BASE_URL/bot$botToken/sendMessage"
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun encodeURIComponent(text: String): String =
        URLEncoder.encode(text, "UTF-8")
}
