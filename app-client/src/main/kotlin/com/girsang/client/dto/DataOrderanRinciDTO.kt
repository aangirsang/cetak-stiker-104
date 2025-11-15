package com.girsang.client.dto

import kotlinx.serialization.Serializable

@Serializable
data class DataOrderanRinciDTO (
    val id: Long? = 0,
    var stiker: DataStikerDTO? = null,
    var stikerId: Long,
    var stikerNama: String,
    var stikerKode: String,
    var jumlah: Int,
    var ukuran: String
)