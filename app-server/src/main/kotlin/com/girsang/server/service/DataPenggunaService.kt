package com.girsang.server.service

import com.girsang.server.model.entity.DataPengguna
import com.girsang.server.repository.DataPenggunaRepository
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class DataPenggunaService(private val repo: DataPenggunaRepository) {

    fun semuaPengguna(): List<DataPengguna> = repo.findAll()
    fun cariID(id: Long): Optional<DataPengguna> = repo.findById(id)

    fun simpan(dataPengguna: DataPengguna): DataPengguna = repo.save(dataPengguna)
    fun update(id: Long, dataPengguna: DataPengguna): DataPengguna{
        val dataLama = repo.findById(id).orElseThrow { throw NoSuchElementException("Data pengguna dengan id $id tidak ditemukan") }
        dataLama.namaLengkap = dataPengguna.namaLengkap
        dataLama.namaPengguna = dataPengguna.namaPengguna
        dataLama.kataSandi = dataPengguna.kataSandi
        dataLama.level = dataPengguna.level
        dataLama.status = dataPengguna.status

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
    fun login(namaPengguna: String, kataSandi: String): DataPengguna {
        val pengguna = repo.findByNamaPengguna(namaPengguna)
            ?: throw NoSuchElementException("Pengguna dengan nama '$namaPengguna' tidak ditemukan")

        if (pengguna.kataSandi != kataSandi) throw IllegalArgumentException("Kata sandi salah")

        if (!pengguna.status) throw IllegalStateException("Akun tidak aktif")

        return pengguna
    }
}