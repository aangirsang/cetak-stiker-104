package com.girsang.client.dto

import kotlinx.serialization.Serializable

@Serializable
data class LevelDTO(
    val id: Long? = null,
    val level: String
)