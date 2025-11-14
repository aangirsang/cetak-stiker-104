package com.girsang.client.dto

import kotlinx.serialization.Serializable

@Serializable
data class DataPenggunaDTO(
    val id: Long? = null,
    val namaLengkap: String,
    val namaPengguna: String,
    val kataSandi: String,
    val dataLevel: DataLevelDTO,
    val status: Boolean
)