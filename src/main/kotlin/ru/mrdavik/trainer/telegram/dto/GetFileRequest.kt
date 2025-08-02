package ru.mrdavik.trainer.telegram.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetFileRequest(
    @SerialName("file_id") val fileId: String
)
