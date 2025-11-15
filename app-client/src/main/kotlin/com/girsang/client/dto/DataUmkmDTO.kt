package com.girsang.client.dto

import kotlinx.serialization.Serializable

@Serializable
data class DataUmkmDTO(
    val id: Long = 0,
    var namaUmkm: String,
    var namaPemilikUmkm: String,
    var noKtp: String,
    var email: String,
    var tglLahir: Long,
    var alamat: String,
    var noTelpon: String,

    var dataKategoriId: Long,
    var facebookNama: String? = "",
    var instagramNama: String? = "",
    var status: Boolean = true
)