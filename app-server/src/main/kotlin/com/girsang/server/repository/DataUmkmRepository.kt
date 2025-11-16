package com.girsang.server.repository

import com.girsang.server.model.entity.DataUmkm
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DataUmkmRepository: JpaRepository<DataUmkm, Long> {
    fun existsByEmail(email: String): Boolean
    fun existsByNoKtp(noKTP: String): Boolean

    @Query("""
        SELECT U
        FROM DataUmkm U
        WHERE (:namaPemilikUmkm IS NULL OR LOWER(U.namaPemilikUmkm) LIKE LOWER(CONCAT('%', :namaPemilikUmkm, '%')))
        AND (:namaUmkm IS NULL OR LOWER(U.namaUmkm) LIKE LOWER(CONCAT('%', :namaUmkm, '%')))
        AND (:alamat IS NULL OR LOWER(U.alamat) LIKE LOWER(CONCAT('%', :alamat, '%')))
    """)
    fun cariUMKM(
        @Param("namaPemilikUmkm") namaPemilikUMKM: String?,
        @Param("namaUmkm") namaUMKM: String?,
        @Param("alamat") alamat: String?
    ): List<DataUmkm>

    fun findAllByStatusTrue(): List<DataUmkm>

}