package com.girsang.client.dto

import kotlinx.serialization.Serializable

@Serializable
class DataLoginDTO (
    val namaPengguna: String,
    val kataSandi: String
)