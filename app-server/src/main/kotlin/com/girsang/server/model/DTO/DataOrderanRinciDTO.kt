package com.girsang.server.model.DTO

import com.girsang.server.model.entity.DataOrderanRinci
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.Long

data class DataOrderanRinciDTO (
    val id: Long,
    var orderanId: Long,
    var tanggal: String,
    var stikerId: Long,
    var stikerNama: String,
    var stikerKode: String,
    var stikerUkuran: String,
    var jumlah: Int
) {
    companion object{
        private val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID"))

        fun fromEntity(entity: DataOrderanRinci): DataOrderanRinciDTO{
            val localDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(entity.orderan.tanggal),
                ZoneId.systemDefault()
            )
            return DataOrderanRinciDTO(
                id = entity.id,
                orderanId = entity.orderan.id,
                tanggal = localDate.format(Companion.formatter),
                stikerId = entity.stiker.id,
                stikerNama = entity.stiker.namaStiker,
                stikerKode = entity.stiker.kodeStiker,
                stikerUkuran = "${entity.stiker.panjang} X ${entity.stiker.lebar}",
                jumlah = entity.jumlah
            )
        }
    }
}