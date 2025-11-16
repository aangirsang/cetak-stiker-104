package com.girsang.server.controller

import com.girsang.server.model.dto.DataStikerDTO
import com.girsang.server.model.entity.DataStiker
import com.girsang.server.service.DataStikerService
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/data-stiker")
@Validated
class DataStikerController(
    private val service: DataStikerService
) {

    @GetMapping
    fun semua(): ResponseEntity<List<DataStikerDTO>> =
        ResponseEntity.ok(service.semua())

    @GetMapping("/{id}")
    fun cariById(@PathVariable id: Long): ResponseEntity<DataStikerDTO> =
        ResponseEntity.ok(service.cariById(id))

    @GetMapping("/umkm/{umkmId}")
    fun cariByUMKM(@PathVariable umkmId: Long): ResponseEntity<List<DataStikerDTO>> =
        ResponseEntity.ok(service.cariByUMKM(umkmId))

    @PostMapping
    fun simpan(@RequestBody dto: DataStikerDTO): ResponseEntity<Any> =
        try {
            ResponseEntity.ok(service.simpan(dto))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("message" to e.message))
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("message" to e.message))
        }

    @PutMapping("/{id}")
    fun ubah(@PathVariable id: Long, @RequestBody dto: DataStikerDTO): ResponseEntity<Any> =
        try {
            ResponseEntity.ok(service.ubah(id, dto))
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

    @GetMapping("/kode/{umkmId}")
    fun getKodeStiker(@PathVariable umkmId: Long): ResponseEntity<Map<String, String>> {
        val kode = service.getKodeStikerBerikutnya(umkmId)
        return ResponseEntity.ok(mapOf("kodeStiker" to kode))
    }

    @GetMapping("/cari")
    fun search(
        @RequestParam(required = false) namaStiker: String?,
        @RequestParam(required = false) namaUsaha: String?
    ): List<DataStiker> {
        return service.cariStiker(namaStiker, namaUsaha)
    }
}