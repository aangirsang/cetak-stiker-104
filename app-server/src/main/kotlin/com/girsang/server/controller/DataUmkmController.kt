package com.girsang.server.controller

import com.girsang.server.model.DTO.DataUMKMDTO
import com.girsang.server.service.DataUmkmService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/data-umkm")
class DataUmkmController(
    private val service: DataUmkmService
) {

    @GetMapping
    fun semua(): ResponseEntity<List<DataUMKMDTO>> =
        ResponseEntity.ok(service.semua())

    @GetMapping("/{id}")
    fun cariById(@PathVariable id: Long): ResponseEntity<DataUMKMDTO> =
        ResponseEntity.ok(service.cariById(id))

    @PostMapping
    fun simpan(@Valid @RequestBody dto: DataUMKMDTO): ResponseEntity<DataUMKMDTO> =
        ResponseEntity.ok(service.simpan(dto))

    @PutMapping("/{id}")
    fun ubah(@PathVariable id: Long, @Valid @RequestBody dto: DataUMKMDTO): ResponseEntity<DataUMKMDTO> =
        ResponseEntity.ok(service.ubah(id, dto))

    @DeleteMapping("/{id}")
    fun hapus(@PathVariable id: Long): ResponseEntity<Unit> {
        service.hapus(id)
        return ResponseEntity.noContent().build()
    }
}