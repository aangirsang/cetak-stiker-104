package com.girsang.server.model.entity

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
    @field:Size(min = 6, message = "Kata sandi minimal 6 karakter")
    @Column(nullable = false)
    var kataSandi: String = "",

    @field:NotBlank(message = "Level tidak boleh kosong")
    var level: String = "",

    var status: Boolean = true
)