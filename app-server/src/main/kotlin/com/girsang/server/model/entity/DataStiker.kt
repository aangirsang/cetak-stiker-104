package com.girsang.server.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import jakarta.validation.constraints.*

@Entity
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
data class DataStiker(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    // 🔁 Relasi ke DataUMKM
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "umkm_id", nullable = false)
    @JsonIgnoreProperties("daftarStiker")
    var dataUmkm: DataUmkm,

    @field:NotBlank(message = "Kode stiker tidak boleh kosong")
    @Column(nullable = false, unique = true)
    var kodeStiker: String = "",

    @field:NotBlank(message = "Nama stiker tidak boleh kosong")
    @Column(nullable = false)
    var namaStiker: String = "",

    @field:Positive(message = "Panjang harus lebih besar dari 0")
    @Column(nullable = false)
    var panjang: Int = 0,

    @field:Positive(message = "Lebar harus lebih besar dari 0")
    @Column(nullable = false)
    var lebar: Int = 0,

    @Column(nullable = true, columnDefinition = "TEXT")
    var catatan: String? = null,

    @Column(nullable = false)
    var status: Boolean = true
)
