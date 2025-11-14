package com.girsang.server.repository

import com.girsang.server.model.entity.DataPengguna
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DataPenggunaRepository: JpaRepository<DataPengguna, Long> {
    fun findByNamaPengguna(namaPengguna: String): DataPengguna?
    fun existsByNamaPengguna(namaPengguna: String): Boolean
    fun existsByNamaPenggunaAndIdNot(namaPengguna: String, id: Long): Boolean
}