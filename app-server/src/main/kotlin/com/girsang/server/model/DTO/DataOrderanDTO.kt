package com.girsang.server.model.DTO

import com.girsang.server.model.entity.DataOrderan
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DataOrderanDTO (
    val id: Long,
    var penggunaId: Long,
    var penggunaNama: String,
    var umkmId: Long,
    var umkmNama: String,
    var faktur: String,
    var tanggal: String,
    var totalStiker: Int,
    val rincian: List<DataOrderanRinciDTO> = emptyList()
){
    companion object{
        private val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID"))

        fun fromEntity(entity: DataOrderan): DataOrderanDTO {
            val localDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(entity.tanggal),
                ZoneId.systemDefault()
            )

            return DataOrderanDTO(
                id = entity.id,
                penggunaId = entity.dataPengguna.id,
                penggunaNama = entity.dataPengguna.namaPengguna,
                umkmId = entity.dataUMKM.id,
                umkmNama = entity.dataUMKM.namaUmkm,
                faktur = entity.faktur,
                tanggal = localDate.format(formatter),
                totalStiker = entity.totalStiker,
                rincian = entity.rincian.map { DataOrderanRinciDTO.fromEntity(it) }
            )
        }
    }
}