package com.girsang.server.model.dto

import com.girsang.server.model.entity.DataOrderanRinci
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
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
                stiker = DataStikerDTO.fromEntity(entity.stiker),
                stikerId = entity.stiker.id,
                stikerNama = entity.stiker.namaStiker,
                stikerKode = entity.stiker.kodeStiker,
                ukuran = "${entity.stiker.panjang} X ${entity.stiker.lebar}",
                jumlah = entity.jumlah
            )
        }
    }
}