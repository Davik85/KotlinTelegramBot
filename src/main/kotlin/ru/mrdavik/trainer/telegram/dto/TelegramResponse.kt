package ru.mrdavik.trainer.telegram.dto

import kotlinx.serialization.Serializable

@Serializable
data class TelegramResponse(
    val result: List<Update>
)
