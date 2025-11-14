package com.girsang.server.model.entity

import jakarta.persistence.*

@Entity
data class DataOrderan (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    // 🔗 Relasi ke DataPengguna
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pengguna_id", nullable = false)
    var dataPengguna: DataPengguna,

    // 🔗 Relasi ke DataUMKM
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "umkm_id", nullable = false)
    var dataUMKM: DataUmkm,

    var faktur: String,
    var tanggal: Long,
    var totalStiker: Int,

    @OneToMany(mappedBy = "orderan", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var rincian: MutableList<DataOrderanRinci> = mutableListOf()
)