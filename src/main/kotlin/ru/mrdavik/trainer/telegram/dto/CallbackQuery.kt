package ru.mrdavik.trainer.telegram.dto

import kotlinx.serialization.Serializable

@Serializable
data class CallbackQuery(
    val data: String? = null,
    val message: Message? = null
)
