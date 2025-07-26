package ru.mrdavik.trainer.telegram.dto

import kotlinx.serialization.Serializable

@Serializable
data class TelegramResponse(
    val ok: Boolean = true,
    val result: List<Update> = emptyList()
)

