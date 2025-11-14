package com.girsang.server.model.DTO

import com.girsang.server.model.entity.DataUmkm

data class DataUMKMDTO(
    val id: Long = 0,
    var namaUmkm: String,
    var namaPemilikUmkm: String,
    var noKTP: String,
    var email: String,
    var tglLahir: Long,
    var alamat: String,
    var kategoriUsaha: String,
    var facebookNama: String? = "",
    var facebookUrl: String? = "",
    var instagramNama: String? = "",
    var instagramUrl: String? = "",
    var status: Boolean = true,
    val daftarStiker: List<DataStikerDTO> = emptyList()
) {
    companion object {
        fun fromEntity(entity: DataUmkm): DataUMKMDTO {
            return DataUMKMDTO(
                id = entity.id,
                namaUmkm = entity.namaUmkm,
                namaPemilikUmkm = entity.namaPemilikUmkm,
                noKTP = entity.noKtp,
                email = entity.email,
                tglLahir = entity.tglLahir,
                alamat = entity.alamat,
                kategoriUsaha = entity.kategoriUsaha,
                facebookNama = entity.facebookNama,
                facebookUrl = entity.facebookUrl,
                instagramNama = entity.instagramNama,
                instagramUrl = entity.instagramUrl,
                status = entity.status,
                daftarStiker = entity.daftarStiker.map { DataStikerDTO.fromEntity(it) }
            )
        }
    }

    fun toEntity(): DataUmkm {
        return DataUmkm(
            id = this.id,
            namaUmkm = this.namaUmkm,
            namaPemilikUmkm = this.namaPemilikUmkm,
            noKtp = this.noKTP,
            email = this.email,
            tglLahir = this.tglLahir,
            alamat = this.alamat,
            kategoriUsaha = this.kategoriUsaha,
            facebookNama = this.facebookNama ?: "",
            facebookUrl = this.facebookUrl ?: "",
            instagramNama = this.instagramNama ?: "",
            instagramUrl = this.instagramUrl ?: "",
            status = this.status
        )
    }
}
