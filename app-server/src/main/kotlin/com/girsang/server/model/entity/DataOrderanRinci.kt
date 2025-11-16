package com.girsang.server.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
data class DataOrderanRinci (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderan_id")
    var dataOrderan: DataOrderan,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stiker_id")
    var dataStiker: DataStiker,

    var jumlah: Int
)