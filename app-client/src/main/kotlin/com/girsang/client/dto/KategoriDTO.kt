package com.girsang.client.dto

import kotlinx.serialization.Serializable

@Serializable
data class KategoriDTO(
    val id: Long? = null,
    val kategori: String
)