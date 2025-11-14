package com.girsang.server.model.DTO

import com.girsang.server.model.entity.DataStiker
import com.girsang.server.model.entity.DataUmkm

data class DataStikerDTO(
    val id: Long = 0,
    var umkmId: Long,
    var umkmNama: String,
    var kodeStiker: String = "",
    var namaStiker: String = "",
    var panjang: Int = 0,
    var lebar: Int = 0,
    val ukuran: String = "",
    var catatan: String? = "",
    var status: Boolean = true
) {
    companion object {
        fun fromEntity(entity: DataStiker): DataStikerDTO {
            return DataStikerDTO(
                id = entity.id,
                umkmId = entity.dataUmkm.id,
                umkmNama = entity.dataUmkm.namaUmkm,
                kodeStiker = entity.kodeStiker,
                namaStiker = entity.namaStiker,
                panjang = entity.panjang,
                lebar = entity.lebar,
                ukuran = "${entity.panjang} X ${entity.lebar}",
                catatan = entity.catatan,
                status = entity.status
            )
        }
    }

    fun toEntity(umkm: DataUmkm, kodeStiker: String): DataStiker {
        return DataStiker(
            id = this.id,
            dataUmkm = umkm,
            kodeStiker = kodeStiker,
            namaStiker = this.namaStiker,
            panjang = this.panjang,
            lebar = this.lebar,
            catatan = this.catatan ?: "",
            status = this.status
        )
    }
}
