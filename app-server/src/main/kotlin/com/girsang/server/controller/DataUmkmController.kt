package com.girsang.server.controller

import com.girsang.server.model.dto.DataUMKMDTO
import com.girsang.server.model.entity.DataUmkm
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

    @GetMapping("/aktif")
    fun semuaAktif(): ResponseEntity<List<DataUMKMDTO>> =
        ResponseEntity.ok(service.semuaAktif())

    @GetMapping("/{id}")
    fun cariById(@PathVariable id: Long): ResponseEntity<DataUMKMDTO> =
        ResponseEntity.ok(service.cariById(id))

    @GetMapping("/cari")
    fun cariUMKM(
        @RequestParam(required = false) namaPemilik: String?,
        @RequestParam(required = false) namaUsaha: String?,
        @RequestParam(required = false) alamat: String?
    ): List<DataUmkm> {
        return service.cariUMKM(namaPemilik, namaUsaha, alamat)
    }

    @PostMapping
    fun simpan(@Valid @RequestBody dto: DataUMKMDTO): ResponseEntity<Any> =
        try {
            val simpan = service.simpan(dto)
            ResponseEntity.status(201).body(simpan)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("message" to e.message))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("message" to e.message))
        }

    @PutMapping("/{id}")
    fun ubah(@PathVariable id: Long, @Valid @RequestBody dto: DataUMKMDTO): ResponseEntity<Any> =
        try {
            val simpan = service.ubah(id, dto)
            ResponseEntity.status(201).body(simpan)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("message" to e.message))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("message" to e.message))
        }

    @DeleteMapping("/{id}")
    fun hapus(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
            service.hapus(id)
            ResponseEntity.ok(mapOf("message" to "Data berhasil dihapus"))
        } catch (e: RuntimeException) {
            ResponseEntity.status(400).body(mapOf("error" to e.message))
        }
    }
}