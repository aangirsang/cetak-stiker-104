package com.girsang.server.service

import com.girsang.server.model.dto.DataUMKMDTO
import com.girsang.server.model.entity.DataUmkm
import com.girsang.server.repository.DataKategoriRepository
import com.girsang.server.repository.DataUmkmRepository
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody

@Service
class DataUmkmService(
    private val repo: DataUmkmRepository,
    private val repoKategori: DataKategoriRepository
) {
    fun semua(): List<DataUMKMDTO> =
        repo.findAll().map { DataUMKMDTO.fromEntity(it) }

    fun cariById(id: Long): DataUMKMDTO {
        val data = repo.findById(id).orElseThrow { NoSuchElementException("UMKM tidak ditemukan") }
        return DataUMKMDTO.fromEntity(data)
    }

    fun cariUMKM(namaPemilikUmkm: String?, namaUmkm: String?, alamat: String?): List<DataUmkm>{
        val namaPemilikUmkm = namaPemilikUmkm?.trim()?.takeIf { it.isNotEmpty() }
        val namaUmkm = namaUmkm?.trim()?.takeIf { it.isNotEmpty() }
        val alamat = alamat?.trim()?.takeIf { it.isNotEmpty() }

        return repo.cariUMKM(namaPemilikUmkm, namaUmkm, alamat)
    }

    fun simpan(@RequestBody dto: DataUMKMDTO): ResponseEntity<DataUmkm> {
        if (repo.existsByEmail(dto.email)) throw IllegalArgumentException("Email sudah digunakan")
        if (repo.existsByNoKtp(dto.noKtp)) throw IllegalArgumentException("Nomor KTP sudah digunakan")

        val umkm = DataUmkm(
            namaUmkm = dto.namaUmkm,
            namaPemilikUmkm = dto.namaPemilikUmkm,
            noKtp = dto.noKtp,
            email = dto.email,
            tglLahir = dto.tglLahir,
            alamat = dto.alamat,
            noTelpon = dto.noTelpon,
            dataKategori = dto.dataKategori,
            facebookNama = dto.facebookNama,
            instagramNama = dto.instagramNama,
            status = dto.status
        )
        val saved = repo.save(umkm)
        return ResponseEntity.ok(saved)
    }

    fun ubah(id: Long, @RequestBody dto: DataUMKMDTO): ResponseEntity<DataUmkm> {
        val lama = repo.findById(id).orElseThrow { NoSuchElementException("Data tidak ditemukan") }

        lama.apply {
            lama.namaUmkm = dto.namaUmkm
            lama.namaPemilikUmkm = dto.namaPemilikUmkm
            lama.noKtp = dto.noKtp
            lama.email = dto.email
            lama.tglLahir = dto.tglLahir
            lama.alamat = dto.alamat
            lama.noTelpon = dto.noTelpon
            lama.dataKategori = dto.dataKategori
            lama.facebookNama = dto.facebookNama ?: ""
            lama.instagramNama = dto.instagramNama ?: ""
            lama.status = dto.status
        }


        val update = repo.save(lama)
        return ResponseEntity.ok(update)
    }

    fun hapus(id: Long) {
        if (!repo.existsById(id)) throw NoSuchElementException("Data tidak ditemukan")
        repo.deleteById(id)
    }
}