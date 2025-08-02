package ru.mrdavik.trainer.telegram

import kotlinx.serialization.json.Json
import ru.mrdavik.trainer.model.Question
import ru.mrdavik.trainer.telegram.dto.*
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val TELEGRAM_API_BASE_URL = "https://api.telegram.org"

const val CALLBACK_LOAD_DICT_TOP100 = "load_dict_top100"
const val CALLBACK_LOAD_DICT_TRAVEL = "load_dict_travel"
const val CALLBACK_LOAD_DICT_IT = "load_dict_it"
const val CALLBACK_LOAD_DICT_FOOD = "load_dict_food"
const val CALLBACK_LOAD_DICT_BASIC = "load_dict_basic"
const val CALLBACK_UPLOAD_YOURS = "upload_your_dictionary"
const val CALLBACK_MY_DICTIONARY = "my_dictionary"
const val CALLBACK_BACK_TO_MAIN = "back_to_main"
const val CALLBACK_LEARN_WORDS = "learn_words"
const val CALLBACK_STATS = "statistics"
const val CALLBACK_RESET_STATS = "reset_stats"

val BUILT_IN_DICTIONARIES = listOf(
    "Топ-100" to CALLBACK_LOAD_DICT_TOP100,
    "Путешествия" to CALLBACK_LOAD_DICT_TRAVEL,
    "IT-лексика" to CALLBACK_LOAD_DICT_IT,
    "Еда и рестораны" to CALLBACK_LOAD_DICT_FOOD,
    "Базовый словарь" to CALLBACK_LOAD_DICT_BASIC
)

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

    fun sendMainMenu(chatId: Long, userHasCustomDict: Boolean) {
        val dictButtons = BUILT_IN_DICTIONARIES.map { (name, callback) ->
            listOf(InlineKeyboardButton(name, callback))
        }.toMutableList()

        if (userHasCustomDict) {
            dictButtons.add(listOf(InlineKeyboardButton("Свой словарь", CALLBACK_MY_DICTIONARY)))
        }
        dictButtons.add(listOf(InlineKeyboardButton("Загрузить свой словарь", CALLBACK_UPLOAD_YOURS)))

        val body = SendMessageRequest(
            chatId = chatId,
            text = "Выберите словарь для изучения:",
            replyMarkup = ReplyMarkup(dictButtons)
        )
        sendJson(json.encodeToString(body))
    }

    fun sendDictionaryMenu(chatId: Long, dictTitle: String) {
        val menu = listOf(
            listOf(
                InlineKeyboardButton("Изучить слова", CALLBACK_LEARN_WORDS),
                InlineKeyboardButton("Статистика", CALLBACK_STATS)
            ),
            listOf(
                InlineKeyboardButton("Сбросить статистику", CALLBACK_RESET_STATS)
            ),
            listOf(
                InlineKeyboardButton("Выйти в главное меню", CALLBACK_BACK_TO_MAIN)
            )
        )
        val body = SendMessageRequest(
            chatId = chatId,
            text = "Меню словаря \"$dictTitle\":",
            replyMarkup = ReplyMarkup(menu)
        )
        sendJson(json.encodeToString(body))
    }

    fun sendMessage(chatId: Long, text: String) {
        val body = SendMessageRequest(chatId = chatId, text = text)
        sendJson(json.encodeToString(body))
    }

    fun sendQuestion(chatId: Long, question: Question) {
        val keyboard = question.options.mapIndexed { idx, word ->
            listOf(InlineKeyboardButton(word.translate, "answer_$idx"))
        }.toMutableList()
        keyboard.add(listOf(InlineKeyboardButton("Выйти в меню словаря", CALLBACK_BACK_TO_MAIN)))

        val body = SendMessageRequest(
            chatId = chatId,
            text = question.options[question.correctIndex].original,
            replyMarkup = ReplyMarkup(keyboard)
        )
        sendJson(json.encodeToString(body))
    }

    fun getFile(fileId: String): GetFileResponse {
        val url = "$TELEGRAM_API_BASE_URL/bot$botToken/getFile"
        val body = json.encodeToString(GetFileRequest.serializer(), GetFileRequest(fileId))
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return json.decodeFromString(GetFileResponse.serializer(), response.body())
    }

    fun downloadFile(filePath: String, fileName: String) {
        val url = "$TELEGRAM_API_BASE_URL/file/bot$botToken/$filePath"
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
        response.body().use { input ->
            File(fileName).outputStream().use { output ->
                input.copyTo(output)
            }
        }
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
