package com.girsang.server.service

import com.girsang.server.model.DTO.DataStikerDTO
import com.girsang.server.model.entity.DataStiker
import com.girsang.server.repository.DataStikerRepository
import com.girsang.server.repository.DataUmkmRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DataStikerService(
    private val repo: DataStikerRepository,
    private val umkmRepo: DataUmkmRepository
) {

    fun semua(): List<DataStikerDTO> =
        repo.findAll().map { DataStikerDTO.fromEntity(it) }

    fun cariById(id: Long): DataStikerDTO {
        val stiker = repo.findById(id).orElseThrow { NoSuchElementException("Stiker tidak ditemukan") }
        return DataStikerDTO.fromEntity(stiker)
    }

    fun cariByUMKM(umkmId: Long): List<DataStikerDTO> {
        val daftar = repo.findByDataUmkmId(umkmId)
        return daftar.map { DataStikerDTO.fromEntity(it) }
    }

    fun simpan(dto: DataStikerDTO): DataStikerDTO {
        val umkm = umkmRepo.findById(dto.umkmId)
            .orElseThrow { NoSuchElementException("UMKM tidak ditemukan") }

        // 🔹 Generate kode otomatis
        val tahunShort = LocalDate.now().year % 100 // contoh: 2025 -> 25
        val kode = generateKodeStiker(umkm.namaUmkm, tahunShort)

        val saved = repo.save(dto.toEntity(umkm, kode))
        return DataStikerDTO.fromEntity(saved)
    }

    private fun generateKodeStiker(namaUMKM: String, tahunShort: Int): String {
        val tahunStr = tahunShort.toString().padStart(2, '0')

        // ambil data terakhir berdasarkan tahun
        val lastList = repo.findLastKodeByYear(tahunStr)
        val nomorBaru = if (lastList.isEmpty()) {
            1
        } else {
            val lastKode = lastList.first().kodeStiker
            val nomor = lastKode.takeLast(3).toIntOrNull() ?: 0
            nomor + 1
        }

        val nomorStr = nomorBaru.toString().padStart(3, '0')
        return "${namaUMKM.trim()} - $tahunStr$nomorStr"
    }

    fun getKodeStikerBerikutnya(umkmId: Long): String {
        val umkm = umkmRepo.findById(umkmId)
            .orElseThrow { NoSuchElementException("UMKM tidak ditemukan") }
        val tahunShort = LocalDate.now().year % 100
        return generateKodeStiker(umkm.namaUmkm, tahunShort)
    }

    fun ubah(id: Long, dto: DataStikerDTO): DataStikerDTO {
        val stiker = repo.findById(id).orElseThrow { NoSuchElementException("Stiker tidak ditemukan") }
        val umkm = umkmRepo.findById(dto.umkmId)
            .orElseThrow { NoSuchElementException("UMKM tidak ditemukan") }

        stiker.dataUmkm = umkm
        stiker.namaStiker = dto.namaStiker
        stiker.panjang = dto.panjang
        stiker.lebar = dto.lebar
        stiker.catatan = dto.catatan
        stiker.status = dto.status

        val updated = repo.save(stiker)
        return DataStikerDTO.fromEntity(updated)
    }

    fun hapus(id: Long) {
        if (!repo.existsById(id)) throw NoSuchElementException("Stiker tidak ditemukan")
        repo.deleteById(id)
    }
}