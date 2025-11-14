package com.girsang.server.repository

import com.girsang.server.model.entity.DataStiker
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface DataStikerRepository: JpaRepository <DataStiker, Long> {
    // 🔍 Cari stiker terakhir berdasarkan tahun (2 digit)
    @Query("""
        SELECT s FROM DataStiker s
        WHERE s.kodeStiker LIKE %:tahun%
        ORDER BY s.id DESC
    """)
    fun findLastKodeByYear(tahun: String): List<DataStiker>

    fun findByDataUmkmId(umkmId: Long): List<DataStiker>
}