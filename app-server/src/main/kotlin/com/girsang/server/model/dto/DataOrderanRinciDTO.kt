package com.girsang.server.model.dto

import com.girsang.server.model.entity.DataOrderanRinci
import kotlin.Long

data class DataOrderanRinciDTO (
    val id: Long? = 0,
    var stiker: DataStikerDTO? = null,
    var stikerId: Long,
    var stikerNama: String,
    var stikerKode: String,
    var jumlah: Int,
    var ukuran: String
) {
    companion object{
        fun fromEntity(entity: DataOrderanRinci): DataOrderanRinciDTO{
            return DataOrderanRinciDTO(
                id = entity.id,
                stiker = DataStikerDTO.fromEntity(entity.dataStiker),
                stikerId = entity.dataStiker.id,
                stikerNama = entity.dataStiker.namaStiker,
                stikerKode = entity.dataStiker.kodeStiker,
                ukuran = "${entity.dataStiker.panjang} X ${entity.dataStiker.lebar}",
                jumlah = entity.jumlah
            )
        }
    }
}