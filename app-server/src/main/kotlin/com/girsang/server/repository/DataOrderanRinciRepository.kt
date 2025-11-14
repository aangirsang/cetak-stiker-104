package com.girsang.server.repository

import com.girsang.server.model.entity.DataOrderanRinci
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DataOrderanRinciRepository: JpaRepository<DataOrderanRinci, Long>