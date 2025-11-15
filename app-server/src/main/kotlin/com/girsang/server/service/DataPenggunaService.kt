package com.girsang.server.service

import com.girsang.server.model.entity.DataPengguna
import com.girsang.server.repository.DataPenggunaRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class DataPenggunaService(private val repo: DataPenggunaRepository) {

    fun semuaPengguna(): List<DataPengguna> = repo.findAll()
    fun cariID(id: Long): Optional<DataPengguna> = repo.findById(id)

    fun simpan(dataPengguna: DataPengguna): DataPengguna {
        if (repo.existsByNamaPengguna(dataPengguna.namaPengguna)) {
            throw IllegalArgumentException("Nama pengguna sudah digunakan")
        }
        return repo.save(dataPengguna)
    }
    fun update(id: Long, dataPengguna: DataPengguna): DataPengguna{
        val dataLama = repo.findById(id).orElseThrow { throw NoSuchElementException("Data pengguna dengan id $id tidak ditemukan") }
        dataLama.namaLengkap = dataPengguna.namaLengkap
        dataLama.namaPengguna = dataPengguna.namaPengguna
        dataLama.kataSandi = dataPengguna.kataSandi
        dataLama.dataLevel = dataPengguna.dataLevel
        dataLama.status = dataPengguna.status

        if (repo.existsByNamaPenggunaAndIdNot(dataPengguna.namaPengguna, dataPengguna.id)) {
            throw IllegalArgumentException("Nama pengguna sudah digunakan")
        }

        return repo.save(dataLama)
    }
    fun hapus(id: Long){
        if(repo.existsById(id)){
            repo.deleteById(id)
        }else {
            throw NoSuchElementException("Data Pengguna dengan id $id tidak ditemukan")
        }
    }
    // 🔐 Fungsi Login
    fun login(namaPengguna: String, kataSandi: String): DataPengguna? {
        val pengguna = repo.findByNamaPengguna(namaPengguna)

        // Jika username tidak ditemukan
        if (pengguna == null) {
            return null
        }

        // Jika password tidak cocok
        if (pengguna.kataSandi != kataSandi) {
            return null
        }

        return pengguna
    }
    fun count(): Long {
        return repo.count()
    }
}