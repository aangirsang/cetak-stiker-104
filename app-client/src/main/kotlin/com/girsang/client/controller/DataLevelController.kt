package com.girsang.client.controller

import client.util.PesanPeringatan
import com.girsang.client.dto.DataLevelDTO
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.ResourceBundle
import kotlin.concurrent.thread

class DataLevelController : Initializable{

    private var clientController: MainClientAppController? = null
    private var parentController: MainClientAppController? = null

    private val client = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }

    @FXML private lateinit var txtLevel: TextField

    @FXML private lateinit var btnSimpan: Button
    @FXML private lateinit var btnHapus: Button
    @FXML private lateinit var btnRefresh: Button
    @FXML private lateinit var btnTutup: Button
    @FXML private lateinit var listLevel: ListView<DataLevelDTO>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {

        btnRefresh.setOnAction { bersih() }
        btnTutup.setOnAction { tutup() }
        btnSimpan.setOnAction { simpanLevel() }
        btnHapus.setOnAction { hapusData() }

        listLevel.setCellFactory {
            object : ListCell<DataLevelDTO>() {
                override fun updateItem(item: DataLevelDTO?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = if (empty || item == null) "" else item.level
                }
            }
        }
        listLevel.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            if (newValue != null) {
                levelTerpilih(newValue)
            }
        }
    }

    fun setClientController(controller: MainClientAppController) {
        this.clientController = controller  // ✅ simpan controller dulu

        if (!controller.url.isNullOrBlank()) {
            loadData() // ✅ baru panggil setelah URL diset
        } else {
            println("⚠️ URL belum di-set, data tidak bisa dimuat")
        }
    }
    fun setParentController(controller: MainClientAppController) {
        this.parentController = controller
    }
    fun bersih(){
        txtLevel.clear()

        btnSimpan.text = "Simpan"

        loadData()
        listLevel.selectionModel.clearSelection()
    }
    fun loadData() {
        val baseUrl = clientController?.url
        if (baseUrl.isNullOrBlank()) {
            Platform.runLater {
                PesanPeringatan.error("Data Level", "URL server belum diset.")
            }
            return
        }

        thread {
            try {
                val builder = HttpRequest.newBuilder()
                    .uri(URI.create("$baseUrl/api/data-level"))
                    .GET()
                    .header("Content-Type", "application/json")

                // Tambahkan Authorization bila ada
                clientController?.buildAuthHeader()?.let {
                    builder.header("Authorization", it)
                }

                val request = builder.build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() in 200..299) {

                    // Decode JSON → List<LevelDTO>
                    val list = json.decodeFromString<List<DataLevelDTO>>(response.body())
                    val sortedList = list.sortedBy { it.level }

                    Platform.runLater {
                        listLevel.items = FXCollections.observableArrayList(sortedList)
                    }

                } else {
                    Platform.runLater {
                        PesanPeringatan.error("Load Data", "Server error ${response.statusCode()}")
                    }
                }

            } catch (ex: Exception) {
                Platform.runLater {
                    PesanPeringatan.error("Load Data", ex.message ?: "Gagal memuat data level")
                }
            }
        }
    }
    fun tutup() {
        parentController?.tutupForm()
    }
    fun getLevelTerpilih(): DataLevelDTO? {
        return listLevel.selectionModel.selectedItem
    }
    fun levelTerpilih(dto: DataLevelDTO){
        txtLevel.text = dto.level
        btnSimpan.text = "Ubah"
    }
    fun simpanLevel() {
        if (btnSimpan.text == "Simpan"){
            val level = txtLevel.text.trim()

            if (level.isEmpty()) {
                PesanPeringatan.warning("Peringatan","Semua field harus diisi!")
                return
            }

            Thread {
                try {
                    val dto = DataLevelDTO(level = level)
                    val body = json.encodeToString(dto)
                    val builder = HttpRequest.newBuilder()
                        .uri(URI.create("${clientController?.url}/api/data-level"))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/json")

                    clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                    val req = builder.build()
                    val resp = clientController?.makeRequest(req)

                    if (resp?.statusCode() in 200..299) {
                        Platform.runLater {
                            PesanPeringatan.info("Simpan Data","Data level berhasil disimpan.")
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
            val LevelDTO = getLevelTerpilih()
            val id = LevelDTO?.id
            val level = txtLevel.text.trim()

            if (id == null) {
                PesanPeringatan.error("Udah Data", "Data ID tidak ditemukan!")
                return
            }
            if (level.isEmpty()) {
                PesanPeringatan.warning("Peringatan","Semua field harus diisi!")
                return
            }

            val konfirm = PesanPeringatan.confirm("Ubah Data Level", "Anda yakin ingi menyimpan perubahan data?")
            if(konfirm) {
                Thread {
                    try {
                        val dto =
                            DataLevelDTO(id = id, level = level)
                        val body = json.encodeToString(dto)
                        val builder = HttpRequest.newBuilder()
                            .uri(URI.create("${clientController?.url}/api/data-level/${id}"))
                            .PUT(HttpRequest.BodyPublishers.ofString(body))
                            .header("Content-Type", "application/json")

                        clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                        val req = builder.build()
                        val resp = clientController?.makeRequest(req)

                        if (resp?.statusCode() in 200..299) {
                            Platform.runLater {
                                PesanPeringatan.info("Udah Data", "Data level berhasil diperbarui.")
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
        val LevelDTO = getLevelTerpilih()
        if (LevelDTO == null) {
            PesanPeringatan.error("Hapus Data", "Tidak ada level yang dipilih.")
            return
        }
        val id = LevelDTO.id
        if (id == null) {
            PesanPeringatan.error("Hapus Data", "ID pengguna tidak tersedia.")
            return
        }

        val konfirm = PesanPeringatan.confirm("Hapus Data","Anda yakin ingin menghapus data ini?")
        if (konfirm) {
            Thread {
                try {
                    val builder = HttpRequest.newBuilder()
                        .uri(URI.create("${clientController?.url}/api/data-level/$id"))
                        .DELETE()

                    clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                    val request = builder.build()
                    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                    Platform.runLater {
                        if (response.statusCode() in 200..299) {
                            PesanPeringatan.info("Hapus Data", "Data Level berhasil dihapus.")
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
                        PesanPeringatan.error("Hapus Data", ex.message ?: "Gagal menghapus data level")
                    }
                }
            }.start()
        }
    }

}