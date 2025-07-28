package ru.mrdavik.trainer.telegram

import kotlinx.serialization.json.Json
import ru.mrdavik.trainer.model.Question
import ru.mrdavik.trainer.telegram.dto.InlineKeyboardButton
import ru.mrdavik.trainer.telegram.dto.ReplyMarkup
import ru.mrdavik.trainer.telegram.dto.SendMessageRequest
import ru.mrdavik.trainer.telegram.dto.TelegramResponse
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TELEGRAM_API_BASE_URL = "https://api.telegram.org"
const val CALLBACK_LEARN_WORDS_CLICKED = "learn_words_clicked"
const val CALLBACK_STATISTICS_CLICKED = "statistics_clicked"
const val CALLBACK_RESET_PROGRESS = "reset_progress"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val CALLBACK_MENU_CLICKED = "menu_clicked"


class TelegramBotService(private val botToken: String) {
    private val client = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }

    fun getUpdates(offset: Long): TelegramResponse {
        val url = "$TELEGRAM_API_BASE_URL/bot$botToken/getUpdates?offset=$offset&timeout=30"
        val request = HttpRequest.newBuilder().uri(URI.create(url)).build()
        while (true) {
            try {
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                return json.decodeFromString(TelegramResponse.serializer(), response.body())
            } catch (e: Exception) {
                println("Ошибка при получении обновлений: ${e.message} | ${e.javaClass.simpleName} | Повтор через 2 сек...")
                Thread.sleep(2000)
            }
        }
    }

    fun sendMessage(chatId: Long, text: String) {
        val body = SendMessageRequest(chatId = chatId, text = text)
        sendJson(json.encodeToString(body))
    }

    fun sendMenu(chatId: Long) {
        val menuKeyboard = listOf(
            listOf(
                InlineKeyboardButton("Изучить слова", CALLBACK_LEARN_WORDS_CLICKED),
                InlineKeyboardButton("Статистика", CALLBACK_STATISTICS_CLICKED)
            ),
            listOf(
                InlineKeyboardButton("Сбросить прогресс", CALLBACK_RESET_PROGRESS)
            )
        )
        val body = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(menuKeyboard)
        )
        sendJson(json.encodeToString(body))
    }

    fun sendQuestion(chatId: Long, question: Question) {
        val keyboard = question.options.mapIndexed { idx, word ->
            listOf(InlineKeyboardButton(word.translate, "$CALLBACK_DATA_ANSWER_PREFIX$idx"))
        }.toMutableList()

        keyboard.add(listOf(InlineKeyboardButton("Выйти в меню", CALLBACK_MENU_CLICKED)))

        val body = SendMessageRequest(
            chatId = chatId,
            text = question.options[question.correctIndex].original,
            replyMarkup = ReplyMarkup(keyboard)
        )
        sendJson(json.encodeToString(body))
    }


    private fun sendJson(jsonBody: String) {
        val url = "$TELEGRAM_API_BASE_URL/bot$botToken/sendMessage"
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build()
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: Exception) {
            println("Ошибка отправки сообщения: ${e.message} | ${e.javaClass.simpleName}")
        }
    }
}
