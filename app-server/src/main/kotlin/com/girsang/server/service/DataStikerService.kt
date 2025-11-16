package com.girsang.server.service

import com.girsang.server.model.dto.DataStikerDTO
import com.girsang.server.model.entity.DataStiker
import com.girsang.server.model.entity.DataUmkm
import com.girsang.server.repository.DataStikerRepository
import com.girsang.server.repository.DataUmkmRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import java.time.LocalDate

@Service
class DataStikerService(
    private val repo: DataStikerRepository,
    private val umkmRepo: DataUmkmRepository,
    private val deletionService: EntityDeletionService
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

    fun simpan(@RequestBody dto: DataStikerDTO): ResponseEntity<Any> {

        // 🔹 Ambil entity DataUmkm dari database
        val umkmEntity = umkmRepo.findById(dto.umkmId)
            .orElse(null)
            ?: return ResponseEntity.badRequest()
                .body(mapOf("error" to "Data UMKM dengan ID ${dto.umkmId} tidak ditemukan"))

        // 🔹 Generate kode otomatis
        val tahunShort = LocalDate.now().year % 100
        val kode = generateKodeStiker(umkmEntity.namaUmkm, tahunShort)

        // 🔹 Buat entity DataStiker dari DTO
        val stiker = DataStiker(
            dataUmkm = umkmEntity,
            kodeStiker = kode,
            namaStiker = dto.namaStiker,
            panjang = dto.panjang,
            lebar = dto.lebar,
            catatan = dto.catatan,
            status = dto.status
        )

        val simpan = repo.save(stiker)

        // 🔹 Kembalikan DTO sebagai response
        val responseDto = DataStikerDTO.fromEntity(simpan)

        return ResponseEntity.ok(responseDto)
    }

    fun ubah(id: Long, @RequestBody dto: DataStikerDTO): ResponseEntity<Any> {
        val stiker = repo.findById(id).orElseThrow { NoSuchElementException("Stiker tidak ditemukan") }

        val umkmEntity = umkmRepo.findById(dto.umkmId)
            .orElse(null)
            ?: return ResponseEntity.badRequest()
                .body(mapOf("error" to "Data UMKM dengan ID ${dto.umkmId} tidak ditemukan"))


        stiker.apply {
            stiker.dataUmkm = umkmEntity
            stiker.namaStiker = dto.namaStiker
            stiker.panjang = dto.panjang
            stiker.lebar = dto.lebar
            stiker.catatan = dto.catatan
            stiker.status = dto.status
        }



        val updated = repo.save(stiker)
        return ResponseEntity.ok(updated)
    }

    fun hapus(id: Long) {
        if (!repo.existsById(id)) throw NoSuchElementException("Data tidak ditemukan")
        deletionService.safeDelete(DataStiker::class.java, id)
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

    fun cariStiker(namaStiker: String?, namaUsaha: String?): List<DataStiker> {
        val keyStiker = namaStiker?.trim()?.takeIf { it.isNotEmpty() }
        val keyUmkm = namaUsaha?.trim()?.takeIf { it.isNotEmpty() }

        return repo.cariStiker(keyStiker, keyUmkm)
    }
}