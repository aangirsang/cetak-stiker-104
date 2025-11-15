package com.girsang.server.controller

import com.girsang.server.model.dto.DataLoginDTO
import com.girsang.server.model.entity.DataPengguna
import com.girsang.server.service.DataPenggunaService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/data-pengguna")
class DataPenggunaController(private val service: DataPenggunaService) {

    @GetMapping
    fun semuaPengguna(): ResponseEntity<List<DataPengguna>>{
    return ResponseEntity.ok(service.semuaPengguna())
    }

    @GetMapping("/{id}")
    fun cariID(@PathVariable id: Long): ResponseEntity<DataPengguna> {
        return service.cariID(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }

    @PostMapping("/login")
    fun login(@RequestBody dto: DataLoginDTO): ResponseEntity<Any> {

        val pengguna = service.login(dto.namaPengguna, dto.kataSandi)

        return if (pengguna != null) {
            ResponseEntity.ok(pengguna)
        } else {
            ResponseEntity.status(401)
                .body(mapOf("error" to "Nama pengguna atau kata sandi salah"))
        }
    }

    @PostMapping
    fun simpan(@Valid @RequestBody dataPengguna: DataPengguna): ResponseEntity<DataPengguna> {
        val simpan = service.simpan(dataPengguna)
        return ResponseEntity.status(201).body(simpan)
    }

    @PutMapping("/{id}")
    fun update(@Valid @PathVariable id: Long,@Valid @RequestBody pengguna: DataPengguna): ResponseEntity<Any> {
        return try {
            val update =service.update(id, pengguna)
            ResponseEntity.ok(update)
        } catch (e: NoSuchElementException) {
            ResponseEntity.status(404).body(mapOf("message" to " Pengguna Tidak Ditemukan"))
        }
    }

    @DeleteMapping("/{id}")
    fun hapus(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        return try {
            service.hapus(id)
            ResponseEntity.ok(mapOf("message" to "Data pengguna Berhasil Dihapus"))
        } catch (e: NoSuchElementException){
            ResponseEntity.status(404).body(mapOf("message" to "Pengguna Tidak Ditemukan"))
        }
    }

    @GetMapping("/ping")
    fun ping(): ResponseEntity<Map<String, String>>{
    return ResponseEntity.ok(mapOf("Status Server " to "Terhubung"))
    }
    @GetMapping("/count")
    fun countPengguna(): Long {
        return service.count()
    }
}