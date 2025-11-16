package com.girsang.server.service

import com.girsang.server.model.dto.DataOrderanDTO
import com.girsang.server.model.entity.DataOrderan
import com.girsang.server.model.entity.DataOrderanRinci
import com.girsang.server.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class DataOrderanService(
    private val repoOrderan: DataOrderanRepository,
    private val repoRinci: DataOrderanRinciRepository,
    private val repoPengguna: DataPenggunaRepository,
    private val repoUMKM: DataUmkmRepository,
    private val repoStiker: DataStikerRepository
) {

    @Transactional(readOnly = true)
    fun semua(): List<DataOrderanDTO> =
        repoOrderan.findAllWithRincian().map { DataOrderanDTO.fromEntity(it) }

    @Transactional(readOnly = true)
    fun cariById(id: Long): DataOrderanDTO? {
        val entity = repoOrderan.findByIdWithRincian(id) ?: return null
        return DataOrderanDTO.fromEntity(entity)
    }

    @Transactional
    fun simpan(dto: DataOrderanDTO): DataOrderanDTO {
        val pengguna = repoPengguna.findById(dto.penggunaId)
            .orElseThrow { NoSuchElementException("Pengguna tidak ditemukan") }

        val umkm = repoUMKM.findById(dto.umkmId)
            .orElseThrow { NoSuchElementException("UMKM tidak ditemukan") }

        val tahunShort = LocalDate.now().year % 100
        val faktur = generateFaktur(tahunShort)

        // total otomatis dihitung dari rincian
        val totalStiker = dto.rincian.sumOf { it.jumlah }

        val orderan = DataOrderan(
            id = 0,
            dataPengguna = pengguna,
            dataUMKM = umkm,
            faktur = faktur,
            tanggal = System.currentTimeMillis(),
            totalStiker = totalStiker
        )

        val rincianEntities = dto.rincian.map { r ->
            val stiker = repoStiker.findById(r.stikerId)
                .orElseThrow { NoSuchElementException("Stiker ID ${r.stikerId} tidak ditemukan") }
            DataOrderanRinci(
                id = 0,
                dataOrderan = orderan,
                dataStiker = stiker,
                jumlah = r.jumlah
            )
        }.toMutableList()

        orderan.rincian = rincianEntities

        val saved = repoOrderan.save(orderan)
        return DataOrderanDTO.fromEntity(saved)
    }

    @Transactional
    fun update(id: Long, dto: DataOrderanDTO): DataOrderanDTO {
        val existing = repoOrderan.findById(id)
            .orElseThrow { NoSuchElementException("Orderan tidak ditemukan") }

        val pengguna = repoPengguna.findById(dto.penggunaId)
            .orElseThrow { NoSuchElementException("Pengguna tidak ditemukan") }

        val umkm = repoUMKM.findById(dto.umkmId)
            .orElseThrow { NoSuchElementException("UMKM tidak ditemukan") }

        existing.dataPengguna = pengguna
        existing.dataUMKM = umkm
        existing.totalStiker = dto.rincian.sumOf { it.jumlah }

        // hapus rincian lama lalu ganti dengan yang baru
        existing.rincian.clear()
        val rincianBaru = dto.rincian.map { r ->
            val stiker = repoStiker.findById(r.stikerId)
                .orElseThrow { NoSuchElementException("Stiker ID ${r.stikerId} tidak ditemukan") }
            DataOrderanRinci(
                id = 0,
                dataOrderan = existing,
                dataStiker = stiker,
                jumlah = r.jumlah
            )
        }
        existing.rincian.addAll(rincianBaru)

        val updated = repoOrderan.save(existing)
        return DataOrderanDTO.fromEntity(updated)
    }

    private fun generateFaktur(tahunShort: Int): String {
        val tahunStr = tahunShort.toString().padStart(2, '0')
        val list = repoOrderan.findLastFakturByYear(tahunStr)
        val nomorBaru = if (list.isEmpty()) 1 else {
            val lastKode = list.first().faktur.takeLast(4).toIntOrNull() ?: 0
            lastKode + 1
        }
        val nomorStr = nomorBaru.toString().padStart(4, '0')
        return "RBBB-$tahunStr$nomorStr"
    }

    fun getFakturBerikutnya(): String {
        val tahunShort = LocalDate.now().year % 100
        return generateFaktur(tahunShort)
    }

    fun hapus(id: Long) {
        if (!repoOrderan.existsById(id)) throw NoSuchElementException("Orderan tidak ditemukan")
        repoOrderan.deleteById(id)
    }
}