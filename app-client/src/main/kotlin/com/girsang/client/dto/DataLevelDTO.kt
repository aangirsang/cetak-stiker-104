package com.girsang.client.dto

import kotlinx.serialization.Serializable

@Serializable
data class DataLevelDTO(
    val id: Long? = null,
    val level: String
){
    override fun toString(): String = level
}