package ru.mrdavik.trainer.telegram.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null
)
