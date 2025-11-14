package com.girsang.server.controller

import com.girsang.server.model.entity.DataLevel
import com.girsang.server.service.DataLevelService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/data-level")
class DataLevelController(private val service: DataLevelService) {

    @GetMapping
    fun semuaLevel(): ResponseEntity<List<DataLevel>> =
        ResponseEntity.ok(service.semuaLevel())

    @GetMapping("/{id}")
    fun cariId(@PathVariable id: Long): ResponseEntity<DataLevel> {
        return service.cariId(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @PostMapping
    fun simpan(@Valid @RequestBody dataLevel: DataLevel): ResponseEntity<DataLevel> {
        val simpan = service.simpan(dataLevel)
        return ResponseEntity.status(201).body(simpan)
    }

    @PutMapping("/{id}")
    fun delete(@PathVariable id: Long, @RequestBody dataLevel: DataLevel): ResponseEntity<Any> {
        return try {
            val update = service.update(id, dataLevel)
            ResponseEntity.ok(update)
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("message" to " Level Tidak Ditemukan"))
        }
    }

    @DeleteMapping("/{id}")
    fun hapus(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        return try {
            service.hapus(id)
            ResponseEntity.ok(mapOf("message" to "Data Level Berhasil Dihapus"))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("message" to " Level Tidak Ditemukan"))
        }
    }
}