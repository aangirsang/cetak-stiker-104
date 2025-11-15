package com.girsang.client.controller

import client.util.PesanPeringatan
import com.girsang.client.dto.DataLoginDTO
import com.girsang.client.dto.DataPenggunaDTO
import com.girsang.client.util.SessionClient
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.TextField
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.ResourceBundle

class LoginController : Initializable {

    @FXML private lateinit var txtNamaPengguna: TextField
    @FXML private lateinit var txtKataSandi: TextField

    @FXML private lateinit var btnLogin: Button

    private var clientController: MainClientAppController? = null
    private var parentController: MainClientAppController? = null

    private val client = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }

    override fun initialize(p0: URL?, p1: ResourceBundle?) {

        btnLogin.setOnAction {login()}
        bersih()
    }
    fun setClientController(controller: MainClientAppController) {
        this.clientController = controller  // ✅ simpan controller dulu
    }
    fun setParentController(controller: MainClientAppController) {
        this.parentController = controller
    }

    private fun bersih(){
        txtKataSandi.clear()
        txtNamaPengguna.clear()

        txtKataSandi.promptText = "Kata Sandi"
        txtNamaPengguna.promptText = "Nama Pengguna"
    }

    private fun login(){
        val namaPengguna = txtNamaPengguna.text.trim()
        val kataSandi = txtKataSandi.text.trim()

        if (namaPengguna.isEmpty() || kataSandi.isEmpty() ) {
            PesanPeringatan.warning("Peringatan","Semua field harus diisi!")
            return
        }
        Thread {
            try {
                val dto = DataLoginDTO(
                    namaPengguna = namaPengguna,
                    kataSandi = kataSandi
                )
                val body = json.encodeToString(dto)
                val builder = HttpRequest.newBuilder()
                    .uri(URI.create("${clientController?.url}/api/data-pengguna/login"))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "application/json")

                // 🔐 Tambahkan header Authorization
                clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                val request = builder.build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() in 200..299) {
                    val pengguna = json.decodeFromString<DataPenggunaDTO>(response.body())
                    SessionClient.penggunaLogin = pengguna
                    Platform.runLater {
                        println("Login berhasil oleh: ${pengguna.namaLengkap}")
                        parentController?.tutupForm()
                        parentController?.loginBerhasil()
                    }
                } else {
                    Platform.runLater {
                        try {
                            // ambil pesan error dari server
                            val errorMap = json.decodeFromString<Map<String, String>>(response.body())

                            val pesan = errorMap["error"] ?: "Terjadi kesalahan pada server"

                            PesanPeringatan.error("Login Gagal", pesan)

                        } catch (e: Exception) {
                            // fallback jika body bukan JSON
                            PesanPeringatan.error(
                                "Login Gagal",
                                "Server error ${response.statusCode()}\n${response.body()}"
                            )
                        }
                    }
                }
            } catch (ex: Exception) {
                Platform.runLater {
                    PesanPeringatan.error("Load Data", ex.message ?: "Gagal menghapus pengguna")
                }
            }
        }.start()
    }



}