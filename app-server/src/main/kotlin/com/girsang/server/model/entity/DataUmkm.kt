package com.girsang.server.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import jakarta.validation.constraints.*

@Entity
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
data class DataUmkm(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank(message = "Nama UMKM tidak boleh kosong")
    @Column(nullable = false)
    var namaUmkm: String = "",

    @field:NotBlank(message = "Nama Pemilik UMKM tidak boleh kosong")
    @Column(nullable = false)
    var namaPemilikUmkm: String = "",

    @field:NotNull(message = "Nomor KTP tidak boleh kosong")
    @Column(nullable = false)
    var noKtp: String = "",

    @field:Email(message = "Format email tidak valid")
    @field:NotBlank(message = "Email tidak boleh kosong")
    @Column(nullable = false, unique = true)
    var email: String = "",

    @field:NotNull(message = "Tanggal lahir tidak boleh kosong")
    @Column(nullable = false)
    var tglLahir: Long = 0L,

    @field:NotBlank(message = "Alamat tidak boleh kosong")
    @Column(nullable = false, columnDefinition = "TEXT")
    var alamat: String = "",

    @field:NotBlank(message = "Kategori usaha harus diisi")
    @Column(nullable = false)
    var kategoriUsaha: String = "",

    @Column(nullable = true)
    var facebookNama: String? = null,

    @Column(nullable = true)
    var facebookUrl: String? = null,

    @Column(nullable = true)
    var instagramNama: String? = null,

    @Column(nullable = true)
    var instagramUrl: String? = null,

    @Column(nullable = false)
    var status: Boolean = true,

    // 🔁 Relasi ke DataStiker
    @OneToMany(mappedBy = "dataUmkm", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("dataUmkm")
    var daftarStiker: MutableList<DataStiker> = mutableListOf()
)
