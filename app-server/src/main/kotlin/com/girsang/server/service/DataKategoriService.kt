package com.girsang.server.service

import com.girsang.server.model.entity.DataKategori
import com.girsang.server.repository.DataKategoriRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class DataKategoriService (private val repo: DataKategoriRepository) {

    fun semuaKategori(): List<DataKategori> = repo.findAll()
    fun cariId(id: Long): Optional<DataKategori> = repo.findById(id)
    fun simpan(dataKategori: DataKategori): DataKategori = repo.save(dataKategori)

    fun update(id: Long, dataKategori: DataKategori): DataKategori {
        val dataLama = repo.findById(id).orElseThrow {
            throw NoSuchElementException ("Data Kategori dengan id $id tidak ditemukan")
        }
        dataLama.kategori = dataKategori.kategori
        return repo.save(dataLama)
    }

    fun hapus(id: Long) {
        if(repo.existsById(id)){
            repo.deleteById(id)
        } else {
            throw NoSuchElementException ("Data Kategori dengan id $id tidak ditemukan")
        }
    }
}