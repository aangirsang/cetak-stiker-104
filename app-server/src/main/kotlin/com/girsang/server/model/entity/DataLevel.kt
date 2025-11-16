package com.girsang.server.model.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.validation.constraints.NotBlank

@Entity
data class DataLevel (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank(message = "Data Level tidak boleh kosong")
    var level: String = "",

    @OneToMany(mappedBy = "dataLevel", fetch = FetchType.LAZY, targetEntity = DataPengguna::class)
    @JsonIgnore // supaya JSON tidak error lazy loading
    var daftarPengguna: List<DataPengguna> = mutableListOf()
)