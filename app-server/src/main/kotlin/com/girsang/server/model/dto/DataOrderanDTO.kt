package com.girsang.server.model.dto

import com.girsang.server.model.entity.DataOrderan
import com.girsang.server.model.entity.DataPengguna
import com.girsang.server.model.entity.DataUmkm
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DataOrderanDTO (
    val id: Long,
    var penggunaId: Long,
    var dataPengguna: DataPengguna? = null,
    var umkmId: Long,
    var umkm: DataUmkm? = null,
    var umkmNama: String = "",
    var faktur: String,
    var tanggal: Long,
    var totalStiker: Int,
    var rincian: List<DataOrderanRinciDTO> = emptyList()
){
    companion object{
        fun fromEntity(entity: DataOrderan): DataOrderanDTO {
            return DataOrderanDTO(
                id = entity.id,
                penggunaId = entity.dataPengguna.id,
                dataPengguna = entity.dataPengguna,
                umkm = entity.dataUMKM,
                umkmId = entity.dataUMKM.id,
                umkmNama = entity.dataUMKM.namaUmkm,
                faktur = entity.faktur,
                tanggal = entity.tanggal,
                totalStiker = entity.totalStiker,
                rincian = entity.rincian.map { DataOrderanRinciDTO.fromEntity(it) }
            )
        }
    }
}