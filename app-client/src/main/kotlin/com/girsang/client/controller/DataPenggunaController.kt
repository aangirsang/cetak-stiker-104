package com.girsang.client.controller

import client.util.PesanPeringatan
import com.girsang.client.dto.DataLevelDTO
import com.girsang.client.dto.DataPenggunaDTO
import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.util.ResourceBundle
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class DataPenggunaController : Initializable {

    private val client = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }

    @FXML private lateinit var txtNamaPengguna: TextField
    @FXML private lateinit var txtNamaAkun: TextField
    @FXML private lateinit var txtPassword: TextField
    @FXML private lateinit var txtUlangPassword: TextField

    @FXML private lateinit var cboLevel: ComboBox<DataLevelDTO>
    @FXML private lateinit var chkStatus: CheckBox

    @FXML private lateinit var btnTutup: Button
    @FXML private lateinit var btnSimpan: Button
    @FXML private lateinit var btnRefresh: Button
    @FXML private lateinit var btnHapus: Button

    @FXML private lateinit var tblPengguna: TableView<DataPenggunaDTO>
    @FXML private lateinit var colId: TableColumn<DataPenggunaDTO, Int>
    @FXML private lateinit var colNama: TableColumn<DataPenggunaDTO, String>
    @FXML private lateinit var colAkun: TableColumn<DataPenggunaDTO, String>
    @FXML private lateinit var colLevel: TableColumn<DataPenggunaDTO, String>
    @FXML private lateinit var colStatus: TableColumn<DataPenggunaDTO, String>

    private var clientController: MainClientAppController? = null
    private var parentController: MainClientAppController? = null

    override fun initialize(p0: URL?, p1: ResourceBundle?) {
        colId.setCellValueFactory { cellData ->
            SimpleIntegerProperty(
                tblPengguna.items.indexOf(cellData.value) + 1
            ).asObject()
        }
        colId.text = "Nomor"
        colId.isResizable = true
        colNama.setCellValueFactory { SimpleStringProperty(it.value.namaLengkap) }
        colAkun.setCellValueFactory { SimpleStringProperty(it.value.namaPengguna) }
        colLevel.setCellValueFactory { SimpleStringProperty(it.value.dataLevel.level) }
        colStatus.setCellValueFactory { cellData ->
            val status = cellData.value.status
            SimpleStringProperty(if (status) "Aktif" else "Non-Aktif")
        }

        btnTutup.setOnAction { tutup() }
        btnSimpan.setOnAction { simpanPengguna() }
        btnRefresh.setOnAction { bersih() }
        btnHapus.setOnAction {hapusData()}

        // 🔹 Tambahkan listener untuk selection
        tblPengguna.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            if (newValue != null) {
                penggunaTerpilih(newValue)
            }
        }

    }
    fun setClientController(controller: MainClientAppController) {
        this.clientController = controller  // ✅ simpan controller dulu

        if (!controller.url.isNullOrBlank()) {
            loadDataPengguna() // ✅ baru panggil setelah URL diset
            loadLevel()
        } else {
            println("⚠️ URL belum di-set, data tidak bisa dimuat")
        }
    }
    fun setParentController(controller: MainClientAppController) {
        this.parentController = controller
    }
    fun bersih() {
        txtNamaPengguna.clear()
        txtNamaAkun.clear()
        txtPassword.clear()
        txtUlangPassword.clear()

        tblPengguna.selectionModel.clearSelection()
        cboLevel.selectionModel.clearSelection()

        chkStatus.isSelected = false

        btnSimpan.text  = "Simpan"

        loadDataPengguna()
    }
    fun tutup() {
        parentController?.tutupForm()
    }
    fun simpanPengguna() {
        if (btnSimpan.text == "Simpan"){
            val namaPengguna = txtNamaPengguna.text.trim()
            val namaAkun = txtNamaAkun.text.trim()
            val password = txtPassword.text.trim()
            val ulangPassword = txtUlangPassword.text.trim()
            val level = getLevelTerpilih()
            val status = chkStatus.isSelected

            if (namaPengguna.isEmpty() || namaAkun.isEmpty() || password.isEmpty() || ulangPassword.isEmpty() || level==null) {
                PesanPeringatan.warning("Peringatan","Semua field harus diisi!")
                return
            }
            if (password != ulangPassword) {
                PesanPeringatan.warning("Peringatan","Kata sandi tidak cocok!")
                return
            }

            Thread {
                try {
                    val dto = DataPenggunaDTO(
                        namaLengkap = namaPengguna,
                        namaPengguna = namaAkun,
                        kataSandi = password,
                        dataLevel = level,
                        status = status)
                    println("namaLengkap = $namaPengguna")
                    println("namaPengguna = $namaAkun")
                    println("kataSandi = $password")
                    println("dataLevel = ${level.id}")
                    println("status = $status")
                    val body = json.encodeToString(dto)
                    val builder = HttpRequest.newBuilder()
                        .uri(URI.create("${clientController?.url}/api/data-pengguna"))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/json")

                    clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                    val req = builder.build()
                    val resp = clientController?.makeRequest(req)

                    if (resp?.statusCode() in 200..299) {
                        Platform.runLater {
                            PesanPeringatan.info("Simpan Data","Data pengguna berhasil disimpan.")
                            bersih()
                        }
                    } else {
                        Platform.runLater {
                            println("Server returned ${resp?.statusCode()} : ${resp?.body()}")
                            PesanPeringatan.error("Simpan Data","Server returned ${resp?.statusCode()} : ${resp?.body()}")
                        }
                    }
                } catch (ex: Exception) {
                    Platform.runLater {
                        PesanPeringatan.error("Simpan Data",ex.message ?:"Error saat menyimpan data" )
                    }
                }
            }.start()
        }else if (btnSimpan.text == "Ubah") {
            val pengguna = getPenggunaTerpilih()
            val id = pengguna?.id
            val namaPengguna = txtNamaPengguna.text.trim()
            val namaAkun = txtNamaAkun.text.trim()
            val password = txtPassword.text.trim()
            val ulangPassword = txtUlangPassword.text.trim()
            val level = getLevelTerpilih()
            val status = chkStatus.isSelected

            if (id == null) {
                PesanPeringatan.error("Udah Data", "Data ID tidak ditemukan!")
                return
            }
            if (namaPengguna.isEmpty() || namaAkun.isEmpty() || password.isEmpty() || ulangPassword.isEmpty() || level==null) {
                PesanPeringatan.warning("Peringatan","Semua field harus diisi!")
                return
            }
            if (password != ulangPassword) {
                PesanPeringatan.error("Udah Data", "Kata sandi tidak cocok!")
                return
            }

            val konfirm = PesanPeringatan.confirm("Ubah Data Pengguna", "Anda yakin ingi menyimpan perubahan data?")
            if(konfirm) {
                Thread {
                    try {
                        val dto =
                            DataPenggunaDTO(
                                id = id,
                                namaLengkap = namaPengguna,
                                namaPengguna = namaAkun,
                                kataSandi = password,
                                dataLevel = level,
                                status = status)
                        val body = json.encodeToString(dto)
                        val builder = HttpRequest.newBuilder()
                            .uri(URI.create("${clientController?.url}/api/data-pengguna/${id}"))
                            .PUT(HttpRequest.BodyPublishers.ofString(body))
                            .header("Content-Type", "application/json")

                        clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                        val req = builder.build()
                        val resp = clientController?.makeRequest(req)

                        if (resp?.statusCode() in 200..299) {
                            Platform.runLater {
                                PesanPeringatan.info("Udah Data", "Data pengguna berhasil diperbarui.")
                                bersih()
                            }
                        } else {
                            Platform.runLater {
                                println("Server returned ${resp?.statusCode()} : ${resp?.body()}")
                                PesanPeringatan.error("Simpan Data","Server returned ${resp?.statusCode()} : ${resp?.body()}")
                            }
                        }
                    } catch (ex: Exception) {
                        Platform.runLater {
                            PesanPeringatan.error("Simpan Data",ex.message ?:"Error saat memperbarui data" )
                        }
                    }
                }.start()
            }
        }

    }
    fun hapusData(){
        val pengguna = getPenggunaTerpilih()
        if (pengguna == null) {
            PesanPeringatan.error("Hapus Data", "Tidak ada pengguna yang dipilih.")
            return
        }
        val id = pengguna.id
        if (id == null) {
            PesanPeringatan.error("Hapus Data", "ID pengguna tidak tersedia.")
            return
        }

        val konfirm = PesanPeringatan.confirm("Hapus Data","Anda yakin ingin menghapus data ini?")
        if (konfirm) {
            Thread {
                try {
                    val builder = HttpRequest.newBuilder()
                        .uri(URI.create("${clientController?.url}/api/data-pengguna/$id"))
                        .DELETE()

                    clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                    val request = builder.build()
                    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                    Platform.runLater {
                        if (response.statusCode() in 200..299) {
                            PesanPeringatan.info("Hapus Data", "Pengguna berhasil dihapus.")
                            bersih()
                        } else {
                            PesanPeringatan.error(
                                "Hapus Data",
                                "Server returned ${response.statusCode()} : ${response.body()}"
                            )
                        }
                    }
                } catch (ex: Exception) {
                    Platform.runLater {
                        PesanPeringatan.error("Hapus Data", ex.message ?: "Gagal menghapus pengguna")
                    }
                }
            }.start()
        }
    }
    fun loadDataPengguna() {
        if (clientController?.url.isNullOrBlank()) {
            Platform.runLater {
                PesanPeringatan.error("Data Pengguna","URL server belum diset.")
            }
            return
        }

        Thread {
            try {
                val builder = HttpRequest.newBuilder()
                    .uri(URI.create("${clientController?.url}/api/data-pengguna"))
                    .GET()
                    .header("Content-Type", "application/json")

                // 🔐 Tambahkan header Authorization
                clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                val request = builder.build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() in 200..299) {
                    val list = json.decodeFromString<List<DataPenggunaDTO>>(response.body())
                    val sortedList = list.sortedBy { it.namaLengkap }
                    Platform.runLater {
                        tblPengguna.items = FXCollections.observableArrayList(sortedList)
                    }
                } else {
                    Platform.runLater {
                        PesanPeringatan.error("Load Data","Server error ${response.statusCode()}")
                    }
                }
            } catch (ex: Exception) {
                Platform.runLater {
                    PesanPeringatan.error("Load Data", ex.message ?: "Gagal menghapus pengguna")
                }
            }
        }.start()
    }
    fun loadLevel() {
        val baseUrl = clientController?.url
        if (baseUrl.isNullOrBlank()) {
            Platform.runLater {
                PesanPeringatan.error("Data Level", "URL server belum diset.")
            }
            return
        }

        Thread {
            try {
                val request = HttpRequest.newBuilder()
                    .uri(URI.create("$baseUrl/api/data-level"))
                    .GET()
                    .header("Content-Type", "application/json")
                    .apply {
                        clientController?.buildAuthHeader()?.let { header("Authorization", it) }
                    }
                    .build()

                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() in 200..299) {
                    val list = json.decodeFromString<List<DataLevelDTO>>(response.body())
                    val sortedList = list.sortedBy { it.level }

                    Platform.runLater {
                        cboLevel.items = FXCollections.observableArrayList(sortedList)
                    }

                } else {
                    Platform.runLater {
                        PesanPeringatan.error("Load Level", "Server error ${response.statusCode()}")
                    }
                }

            } catch (ex: Exception) {
                Platform.runLater {
                    PesanPeringatan.error("Load Level", ex.message ?: "Gagal memuat data level")
                }
            }
        }.start()
    }
    fun penggunaTerpilih(dto: DataPenggunaDTO){
        txtNamaPengguna.text = dto.namaLengkap
        txtNamaAkun.text = dto.namaPengguna
        chkStatus.isSelected = dto.status
        cboLevel.selectionModel.select(dto.dataLevel)
        txtPassword.clear()
        txtUlangPassword.clear()
        btnSimpan.text = "Ubah"
    }
    fun getPenggunaTerpilih(): DataPenggunaDTO? {
        return tblPengguna.selectionModel.selectedItem
    }
    fun getLevelTerpilih(): DataLevelDTO? {
        return cboLevel.selectionModel.selectedItem
    }

}
