package com.girsang.server.model.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.validation.constraints.NotBlank

@Entity
data class DataKategori (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank(message = "Kategori tidak boleh kosong")
    var kategori: String = "",

    @OneToMany(mappedBy = "dataKategori", fetch = FetchType.LAZY, targetEntity = DataUmkm::class)
    @JsonIgnore // supaya JSON tidak error lazy loading
    var daftarUmkm: List<DataUmkm> = mutableListOf()
)