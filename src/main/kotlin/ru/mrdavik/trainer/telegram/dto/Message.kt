package ru.mrdavik.trainer.telegram.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    @SerialName("text") val text: String? = null,
    @SerialName("chat") val chat: Chat,
    @SerialName("document") val document: Document? = null
)

