package com.girsang.server.model.DTO

import com.girsang.server.model.entity.DataUmkm

data class DataUMKMDTO(
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
    var status: Boolean = true,
) {
    companion object {
        fun fromEntity(entity: DataUmkm): DataUMKMDTO {
            return DataUMKMDTO(
                id = entity.id,
                namaUmkm = entity.namaUmkm,
                namaPemilikUmkm = entity.namaPemilikUmkm,
                noKtp = entity.noKtp,
                email = entity.email,
                tglLahir = entity.tglLahir,
                alamat = entity.alamat,
                noTelpon = entity.noTelpon,
                dataKategoriId = entity.dataKategori.id,
                facebookNama = entity.facebookNama,
                instagramNama = entity.instagramNama,
                status = entity.status,
            )
        }
    }
}
