package com.girsang.client.dto

import kotlinx.serialization.Serializable

@Serializable
data class PenggunaDTO(
    val id: Long? = null,
    val namaLengkap: String,
    val namaAkun: String,
    val kataSandi: String
)