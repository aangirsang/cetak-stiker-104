package com.girsang.server.model.dto

import com.girsang.server.model.entity.DataStiker

data class DataStikerDTO(
    val id: Long = 0,
    var dataUmkm: DataUMKMDTO? = null,
    var umkmId: Long,
    var kodeStiker: String = "",
    var namaStiker: String = "",
    var panjang: Int = 0,
    var lebar: Int = 0,
    var ukuran: String = "",
    var catatan: String? = "",
    var status: Boolean = true
) {
    companion object {
        fun fromEntity(entity: DataStiker): DataStikerDTO {
            return DataStikerDTO(
                id = entity.id,
                dataUmkm = DataUMKMDTO.fromEntity(entity.dataUmkm),
                umkmId = entity.dataUmkm.id,
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
}
