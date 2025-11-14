package com.girsang.server.controller

import com.girsang.server.model.DTO.DataStikerDTO
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
    fun simpan(@RequestBody dto: DataStikerDTO): ResponseEntity<DataStikerDTO> =
        ResponseEntity.ok(service.simpan(dto))

    @PutMapping("/{id}")
    fun ubah(@PathVariable id: Long, @RequestBody dto: DataStikerDTO): ResponseEntity<DataStikerDTO> =
        ResponseEntity.ok(service.ubah(id, dto))

    @DeleteMapping("/{id}")
    fun hapus(@PathVariable id: Long): ResponseEntity<Unit> {
        service.hapus(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/kode/{umkmId}")
    fun getKodeStiker(@PathVariable umkmId: Long): ResponseEntity<Map<String, String>> {
        val kode = service.getKodeStikerBerikutnya(umkmId)
        return ResponseEntity.ok(mapOf("kodeStiker" to kode))
    }
}