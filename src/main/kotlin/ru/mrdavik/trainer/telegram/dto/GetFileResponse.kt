package ru.mrdavik.trainer.telegram.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetFileResponse(
    @SerialName("ok") val ok: Boolean,
    @SerialName("result") val result: TelegramFile? = null
)

