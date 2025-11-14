package com.girsang.server.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import jakarta.validation.constraints.*

@Entity
data class DataPengguna(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank(message = "Nama lengkap tidak boleh kosong")
    var namaLengkap: String = "",

    @field:NotBlank(message = "Nama pengguna tidak boleh kosong")
    @Column(nullable = false, unique = true)
    var namaPengguna: String = "",

    @field:NotBlank(message = "Kata sandi tidak boleh kosong")
    @Column(nullable = false)
    var kataSandi: String = "",

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "data_level_id")
    var dataLevel: DataLevel,

    var status: Boolean = true
)