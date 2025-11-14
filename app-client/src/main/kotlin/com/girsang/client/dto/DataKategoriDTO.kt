package com.girsang.client.dto

import kotlinx.serialization.Serializable

@Serializable
data class DataKategoriDTO(
    val id: Long? = null,
    val kategori: String
)