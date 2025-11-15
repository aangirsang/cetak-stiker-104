package com.girsang.client.dto

import kotlinx.serialization.Serializable

@Serializable
data class DataStikerDTO(
    val id: Long? = 0,
    var dataUmkm: DataUmkmDTO? = null,
    var umkmId: Long,
    var kodeStiker: String = "",
    var namaStiker: String = "",
    var panjang: Int = 0,
    var lebar: Int = 0,
    val ukuran: String = "",
    var catatan: String? = "",
    var status: Boolean = true
)