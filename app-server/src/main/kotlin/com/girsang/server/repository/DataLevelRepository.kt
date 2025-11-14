package com.girsang.server.repository

import com.girsang.server.model.entity.DataLevel
import org.springframework.data.jpa.repository.JpaRepository

interface DataLevelRepository: JpaRepository<DataLevel, Long>