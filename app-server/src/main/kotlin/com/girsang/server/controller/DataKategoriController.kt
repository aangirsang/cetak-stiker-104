package com.girsang.server.controller

import com.girsang.server.model.entity.DataKategori
import com.girsang.server.service.DataKategoriService
import jakarta.validation.Valid
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/data-kategori")
class DataKategoriController(private val service: DataKategoriService) {

    @GetMapping
    fun semuaKategori(): ResponseEntity<List<DataKategori>> =
        ResponseEntity.ok(service.semuaKategori())

    @GetMapping("/{id}")
    fun cariId(@PathVariable id: Long): ResponseEntity<DataKategori> {
        return service.cariId(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @PostMapping
    fun simpan(@Valid @RequestBody dataKategori: DataKategori): ResponseEntity<DataKategori> {
        val simpan = service.simpan(dataKategori)
        return ResponseEntity.status(201).body(simpan)
    }

    @PutMapping("/{id}")
    fun delete(@PathVariable id: Long, @RequestBody dataKategori: DataKategori): ResponseEntity<Any> {
        return try {
            val update = service.update(id, dataKategori)
            ResponseEntity.ok(update)
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("message" to " Kategori Tidak Ditemukan"))
        }
    }

    @DeleteMapping("/{id}")
    fun hapus(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        return try {
            service.hapus(id)
            ResponseEntity.ok(mapOf("message" to "Data Kategori Berhasil Dihapus"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("message" to " Kategori Tidak Ditemukan"))
        }
    }
}