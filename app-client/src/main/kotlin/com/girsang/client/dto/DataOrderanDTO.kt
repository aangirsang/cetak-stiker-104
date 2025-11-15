package com.girsang.client.dto

import kotlinx.serialization.Serializable

@Serializable
data class DataOrderanDTO (
    val id: Long,
    var penggunaId: Long,
    var dataPengguna: DataPenggunaDTO? = null,
    var umkmId: Long,
    var umkm: DataUmkmDTO? = null,
    var umkmNama: String = "",
    var faktur: String,
    var tanggal: Long,
    var totalStiker: Int,
    var rincian: List<DataOrderanRinciDTO> = emptyList()
)