package com.girsang.client.controller

import client.util.PesanPeringatan
import com.girsang.client.config.ClientConfig
import com.girsang.client.dto.DataLevelDTO
import com.girsang.client.dto.DataPenggunaDTO
import com.girsang.client.util.SessionClient
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.layout.BorderPane
import javafx.scene.control.Label
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import kotlin.jvm.javaClass
import kotlin.let
import kotlin.text.removeSuffix
import kotlin.text.toByteArray
import kotlin.text.trim

class MainClientAppController : Initializable {

    private val client = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }

    @FXML private lateinit var mainPane: BorderPane
    @FXML lateinit var lblWaktu: Label
    @FXML private lateinit var lblStatusServer: Label
    @FXML private lateinit var lblURL: Label
    @FXML private lateinit var lblNamaPengguna: Label

    @FXML private lateinit var menuBar: MenuBar
    @FXML private lateinit var mnPengguna: MenuItem
    @FXML private lateinit var mnKategori: MenuItem
    @FXML private lateinit var mnLevel: MenuItem
    @FXML private lateinit var mnPengaturan: MenuItem
    @FXML private lateinit var mnLogOut: MenuItem
    @FXML private lateinit var mnUMKM: MenuItem
    @FXML private lateinit var mnDataStiker: MenuItem
    @FXML private lateinit var mnOrderStiker: MenuItem

    val user = ClientConfig.getUser()
    val pass = ClientConfig.getPass()
    val ip = ClientConfig.getIP()
    val port = ClientConfig.getPort()
    val url = "http://$ip:$port"
    val baseUrl = this.url.trim().removeSuffix("/")   // gunakan this.url, bukan url lokal

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        mnPengguna.setOnAction { tampilFormPengguna() }
        mnKategori.setOnAction { tampilFormKategori() }
        mnLevel.setOnAction { tampilFormLevel() }
        mnPengaturan.setOnAction { tampilSettings() }
        mnLogOut.setOnAction { logout() }
        mnUMKM.setOnAction { tampilFormUMKM() }
        mnDataStiker.setOnAction { tampilFormStiker() }
        mnOrderStiker.setOnAction { tampilOrdertiker() }
        lblURL.text = ""
        if(baseUrl == "") {
            tampilSettings()
        } else {
            konekServer(baseUrl)
        }
    }

    fun tampilFormPengguna() {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/fxml/form-data-pengguna.fxml"))
        val content: AnchorPane = fxmlLoader.load()
        val controller = fxmlLoader.getController<DataPenggunaController>()
        controller.setClientController(this)  // kirim parent controller
        controller.setParentController(this)     // sudah ada ✅
        mainPane.center = content
    }
    private fun tampilFormKategori() {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/fxml/form-data-kategori.fxml"))
        val content: AnchorPane = fxmlLoader.load()
        val controller = fxmlLoader.getController<DataKategoriController>()
        controller.setClientController(this)  // kirim parent controller
        controller.setParentController(this)     // sudah ada ✅
        mainPane.center = content
    }
    private fun tampilFormLevel() {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/fxml/form-data-level.fxml"))
        val content: AnchorPane = fxmlLoader.load()
        val controller = fxmlLoader.getController<DataLevelController>()
        controller.setClientController(this)  // kirim parent controller
        controller.setParentController(this)     // sudah ada ✅
        mainPane.center = content
    }
    private fun tampilFormLogin() {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/fxml/form-login.fxml"))
        val content: AnchorPane = fxmlLoader.load()
        val controller = fxmlLoader.getController<LoginController>()
        controller.setClientController(this)  // kirim parent controller
        controller.setParentController(this)     // sudah ada ✅
        mainPane.center = content
    }

    private fun tampilFormUMKM() {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/fxml/form-data-umkm.fxml"))
        val content: AnchorPane = fxmlLoader.load()
        val controller = fxmlLoader.getController<DataUmkmController>()
        controller.setClientController(this)  // kirim parent controller
        controller.setParentController(this)     // sudah ada ✅
        mainPane.center = content
    }
    private fun tampilFormStiker() {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/fxml/form-data-stiker.fxml"))
        val content: AnchorPane = fxmlLoader.load()
        val controller = fxmlLoader.getController<DataStikerController>()
        controller.setClientController(this)  // kirim parent controller
        controller.setParentController(this)     // sudah ada ✅
        mainPane.center = content
    }
    private fun tampilOrdertiker() {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/fxml/form-data-orderan.fxml"))
        val content: AnchorPane = fxmlLoader.load()
        val controller = fxmlLoader.getController<DataOrderanController>()
        controller.setClientController(this)  // kirim parent controller
        controller.setParentController(this)     // sudah ada ✅
        mainPane.center = content
    }
    fun tutupForm() {
        mainPane.center = null
    }

    fun loginBerhasil() {
        println("login berhasil")
        val pengguna = SessionClient.penggunaLogin
        lblNamaPengguna.text = "Nama Pengguna: ${pengguna?.namaLengkap}"
        menuBar.isDisable = false
    }
    fun logout(){
        tutupForm()
        lblNamaPengguna.text = "Belum Ada Data Login"
        menuBar.isDisable = true
        tampilFormLogin()
        SessionClient.penggunaLogin = null
    }
    fun konekServer(baseUrl: String) {
        lblStatusServer.text = "Mencoba terhubung ke server…"
        println("Server: $url/api/data-pengguna/ping")
        println("User: $user")
        println("Password: $pass")

        val builder = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/api/data-pengguna/ping"))
            .GET()

        buildAuthHeader()?.let { builder.header("Authorization", it) }
        val req = builder.build()

        Thread {
            try {
                val resp = makeRequest(req)

                Platform.runLater {
                    when (resp.statusCode()) {
                        200 -> {
                            lblStatusServer.text = "${resp.body()}"
                            lblURL.text = "URL Server: $url"
                        }
                        401 -> {
                            lblStatusServer.text = "Autentikasi gagal (401)"
                            showError("Username atau password salah.\nSilakan periksa pengaturan koneksi Anda.")
                            return@runLater
                        }
                        else -> {
                            lblStatusServer.text = "⚠️ Server merespons kode ${resp.statusCode()}"
                            showError("Koneksi ke server berhasil tapi tidak valid.\nKode: ${resp.statusCode()}")
                            return@runLater
                        }
                    }
                }

                // cek jumlah pengguna di background thread
                val jumlah = jumlahPengguna()
                Platform.runLater {
                    when {
                        jumlah == 0L -> {
                            menuBar.isDisable = true
                            buatPenggunaAdmin()
                            tampilFormPengguna()
                            println("Tidak ada pengguna, membuat admin dan tampilkan form tambah pengguna")
                        }
                        jumlah > 0L -> {
                            menuBar.isDisable = false
                            tampilFormLogin()
                            println("Pengguna sudah ada, tampilkan form login")
                        }
                        else -> {
                            showError("Gagal mengecek jumlah pengguna.")
                            println("Gagal mengecek jumlah pengguna.")
                        }
                    }
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
                Platform.runLater {
                    lblStatusServer.text = "Server tidak dapat dihubungi"
                    val confirm = PesanPeringatan.confirmWithSettings(
                        "Konfirmasi",
                        "Koneksi ke server gagal.\nApakah ingin mencoba kembali?"
                    )

                    when (confirm) {
                        "OK" -> konekServer(baseUrl)
                        "SETTING" -> {
                            tampilSettings()
                            (lblWaktu.scene.window as Stage).close()
                        }
                        "CANCEL" -> (lblWaktu.scene.window as Stage).close()
                    }
                }
            }
        }.start()
    }

    // fungsi bantu buat admin jika belum ada pengguna
    private fun buatPenggunaAdmin() {
        try {
            // buat level admin
            val dtoLevel = DataLevelDTO(level = "Admin") // hapus id jika server auto-generate
            val bodyLevel = json.encodeToString(dtoLevel)
            val builderLevel = HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/data-level"))
                .POST(HttpRequest.BodyPublishers.ofString(bodyLevel))
                .header("Content-Type", "application/json")
            buildAuthHeader()?.let { builderLevel.header("Authorization", it) }
            val reqLevel = builderLevel.build()
            val respLevel = makeRequest(reqLevel)

            if (respLevel.statusCode() != 200 && respLevel.statusCode() != 201) {
                Platform.runLater {
                    PesanPeringatan.error("Simpan Level", "Gagal membuat level admin. Status: ${respLevel.statusCode()}")
                }
                return
            }

            // ambil ID level dari respons server jika auto-generate
            val savedLevel = json.decodeFromString<DataLevelDTO>(respLevel.body())

            // buat pengguna admin
            val dtoPengguna = DataPenggunaDTO(
                namaLengkap = "Admin",
                namaPengguna = "Admin",
                kataSandi = "Admin",
                dataLevel = savedLevel,
                status = true
            )
            val bodyPengguna = json.encodeToString(dtoPengguna)
            val builderPengguna = HttpRequest.newBuilder()
                .uri(URI.create("${baseUrl}/api/data-pengguna"))
                .POST(HttpRequest.BodyPublishers.ofString(bodyPengguna))
                .header("Content-Type", "application/json")
            buildAuthHeader()?.let { builderPengguna.header("Authorization", it) }
            val reqPengguna = builderPengguna.build()
            val respPengguna = makeRequest(reqPengguna)

            SessionClient.penggunaLogin = dtoPengguna
            loginBerhasil()

            if (respPengguna.statusCode() != 200 && respPengguna.statusCode() != 201) {
                Platform.runLater {
                    PesanPeringatan.error("Simpan Pengguna", "Gagal membuat pengguna admin. Status: ${respPengguna.statusCode()}")
                }
            }

        } catch (ex: Exception) {
            Platform.runLater {
                PesanPeringatan.error("Simpan Data", ex.message ?: "Error saat menyimpan data")
            }
        }
    }

    // pastikan jumlahPengguna pakai auth header
    private fun jumlahPengguna(): Long {
        return try {
            val builder = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/api/data-pengguna/count"))
                .GET()

            buildAuthHeader()?.let { builder.header("Authorization", it) }
            val req = builder.build()

            val resp = makeRequest(req)
            if (resp.statusCode() == 200) resp.body().toLong() else -1L
        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        }
    }

    fun buildAuthHeader(): String? {
        val token = Base64.getEncoder().encodeToString("$user:$pass".toByteArray())
        return "Basic $token"
    }

    fun makeRequest(req: HttpRequest): HttpResponse<String> =
        client.send(req, HttpResponse.BodyHandlers.ofString())

    fun showError(pesan: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Perhatian"
        alert.headerText = null
        alert.contentText = pesan
        alert.showAndWait()

    }
    fun showInfo(pesan: String) {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "Informasi"
        alert.headerText = null
        alert.contentText = pesan
        alert.showAndWait()
    }
    private fun tampilSettings() {
        val stage = Stage()
        val loader = FXMLLoader(javaClass.getResource("/fxml/form-settings.fxml"))
        stage.scene = Scene(loader.load())
        stage.title = "Pengaturan Koneksi"
        stage.show()
    }
}
