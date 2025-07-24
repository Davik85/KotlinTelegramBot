package ru.mrdavik.trainer.telegram.dto

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val text: String? = null,
    val chat: Chat
)
