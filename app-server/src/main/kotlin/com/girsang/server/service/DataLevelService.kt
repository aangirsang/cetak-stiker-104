package com.girsang.server.service

import com.girsang.server.model.entity.DataKategori
import com.girsang.server.model.entity.DataLevel
import com.girsang.server.repository.DataKategoriRepository
import com.girsang.server.repository.DataLevelRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class DataLevelService (private val repo: DataLevelRepository) {

    fun semuaLevel(): List<DataLevel> = repo.findAll()
    fun cariId(id: Long): Optional<DataLevel> = repo.findById(id)
    fun simpan(dataLevel: DataLevel): DataLevel = repo.save(dataLevel)

    fun update(id: Long, dataLevel: DataLevel): DataLevel {
        val dataLama = repo.findById(id).orElseThrow {
            throw NoSuchElementException ("Data Level dengan id $id tidak ditemukan")
        }
        dataLama.level = dataLevel.level
        return repo.save(dataLama)
    }

    fun hapus(id: Long) {
        if(repo.existsById(id)){
            repo.deleteById(id)
        } else {
            throw NoSuchElementException ("Data Level dengan id $id tidak ditemukan")
        }
    }
}