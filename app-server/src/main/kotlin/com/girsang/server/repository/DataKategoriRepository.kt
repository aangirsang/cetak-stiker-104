package com.girsang.server.repository

import com.girsang.server.model.entity.DataKategori
import org.springframework.data.jpa.repository.JpaRepository

interface DataKategoriRepository: JpaRepository<DataKategori, Long>