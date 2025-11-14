package com.girsang.server.service

import com.girsang.server.model.DTO.DataUMKMDTO
import com.girsang.server.repository.DataUmkmRepository
import org.springframework.stereotype.Service

@Service
class DataUmkmService(
    private val repo: DataUmkmRepository
) {
    fun semua(): List<DataUMKMDTO> =
        repo.findAll().map { DataUMKMDTO.fromEntity(it) }

    fun cariById(id: Long): DataUMKMDTO {
        val data = repo.findById(id).orElseThrow { NoSuchElementException("UMKM tidak ditemukan") }
        return DataUMKMDTO.fromEntity(data)
    }

    fun simpan(dto: DataUMKMDTO): DataUMKMDTO {
        if (repo.existsByEmail(dto.email)) throw IllegalArgumentException("Email sudah digunakan")
        if (repo.existsByNoKtp(dto.noKTP)) throw IllegalArgumentException("Nomor KTP sudah digunakan")

        val saved = repo.save(dto.toEntity())
        return DataUMKMDTO.fromEntity(saved)
    }

    fun ubah(id: Long, dto: DataUMKMDTO): DataUMKMDTO {
        val lama = repo.findById(id).orElseThrow { NoSuchElementException("Data tidak ditemukan") }

        lama.namaUmkm = dto.namaUmkm
        lama.namaPemilikUmkm = dto.namaPemilikUmkm
        lama.noKtp = dto.noKTP
        lama.email = dto.email
        lama.tglLahir = dto.tglLahir
        lama.alamat = dto.alamat
        lama.kategoriUsaha = dto.kategoriUsaha
        lama.facebookNama = dto.facebookNama ?: ""
        lama.facebookUrl = dto.facebookUrl ?: ""
        lama.instagramNama = dto.instagramNama ?: ""
        lama.instagramUrl = dto.instagramUrl ?: ""
        lama.status = dto.status

        val update = repo.save(lama)
        return DataUMKMDTO.fromEntity(update)
    }

    fun hapus(id: Long) {
        if (!repo.existsById(id)) throw NoSuchElementException("Data tidak ditemukan")
        repo.deleteById(id)
    }
}