package com.girsang.server.controller

import com.girsang.server.model.DTO.DataOrderanDTO
import com.girsang.server.service.DataOrderanService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/data-orderan")
class DataOrderanController(
    private val service: DataOrderanService
) {

    @GetMapping
    fun semua(): ResponseEntity<List<DataOrderanDTO>> =
        ResponseEntity.ok(service.semua())

    @GetMapping("/{id}")
    fun cariById(@PathVariable id: Long): ResponseEntity<DataOrderanDTO> =
        ResponseEntity.ok(service.cariById(id))

    @PostMapping
    fun simpan(@RequestBody dto: DataOrderanDTO): ResponseEntity<DataOrderanDTO> =
        ResponseEntity.ok(service.simpan(dto))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody dto: DataOrderanDTO): ResponseEntity<DataOrderanDTO> =
        ResponseEntity.ok(service.update(id, dto))

    @DeleteMapping("/{id}")
    fun hapus(@PathVariable id: Long): ResponseEntity<Unit> {
        service.hapus(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/faktur")
    fun getFaktur(): ResponseEntity<Map<String, String>> {
        val faktur = service.getFakturBerikutnya()
        return ResponseEntity.ok(mapOf("faktur" to faktur))
    }
}