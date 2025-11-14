package com.girsang.server.repository

import com.girsang.server.model.entity.DataOrderan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface DataOrderanRepository : JpaRepository<DataOrderan, Long> {

    @Query("""
        SELECT o FROM DataOrderan o
        WHERE o.faktur LIKE %:tahun%
        ORDER BY o.id DESC
    """)
    fun findLastFakturByYear(tahun: String): List<DataOrderan>
}