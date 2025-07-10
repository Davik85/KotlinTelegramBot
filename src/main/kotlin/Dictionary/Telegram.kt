package org.example.Dictionary

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val UPDATE_ID_PATTERN = """"update_id":\s*(\d+)"""
const val CHAT_ID_PATTERN = """"chat":\s*\{[^}]*"id":\s*(-?\d+)"""
const val TEXT_PATTERN = """"text":"(.+?)""""

fun main(args: Array<String>) {
    val botToken = args[0]
    var updateId = 0

    val updateIdRegex = UPDATE_ID_PATTERN.toRegex()
    val chatIdRegex = CHAT_ID_PATTERN.toRegex()
    val textRegex = TEXT_PATTERN.toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        val updateIdMatch = updateIdRegex.find(updates)
        val chatIdMatch = chatIdRegex.find(updates)
        val textMatch = textRegex.find(updates)

        if (updateIdMatch == null || chatIdMatch == null || textMatch == null) continue

        updateId = updateIdMatch.groupValues[1].toInt() + 1
        val chatId = chatIdMatch.groupValues[1].toLong()
        val text = textMatch.groupValues[1]
        println(text)
    }
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    return response.body()
}
