package org.example.Dictionary

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(private val botToken: String) {
    private val client = HttpClient.newBuilder().build()

    fun getUpdates(offset: Int): String {
        val url = "https://api.telegram.org/bot$botToken/getUpdates?offset=$offset"
        val request = HttpRequest.newBuilder().uri(URI.create(url)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun sendMessage(chatId: Long, text: String): String {
        val url = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=${encodeURIComponent(text)}"
        val request = HttpRequest.newBuilder().uri(URI.create(url)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun encodeURIComponent(text: String): String =
        java.net.URLEncoder.encode(text, "UTF-8")
}
