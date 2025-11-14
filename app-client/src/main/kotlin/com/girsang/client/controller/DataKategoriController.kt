package com.girsang.client.controller

import client.util.PesanPeringatan
import com.girsang.client.dto.DataKategoriDTO
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
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

class DataKategoriController : Initializable{

    private var clientController: MainClientAppController? = null
    private var parentController: MainClientAppController? = null

    private val client = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }

    @FXML private lateinit var txtKategori: TextField

    @FXML private lateinit var btnSimpan: Button
    @FXML private lateinit var btnHapus: Button
    @FXML private lateinit var btnRefresh: Button
    @FXML private lateinit var btnTutup: Button
    @FXML private lateinit var listKategori: ListView<DataKategoriDTO>

    override fun initialize(p0: URL?, p1: ResourceBundle?) {

        btnRefresh.setOnAction { bersih() }
        btnTutup.setOnAction { tutup() }
        btnSimpan.setOnAction { simpanKategori() }
        btnHapus.setOnAction { hapusData() }

        listKategori.setCellFactory {
            object : javafx.scene.control.ListCell<DataKategoriDTO>() {
                override fun updateItem(item: DataKategoriDTO?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = if (empty || item == null) "" else item.kategori
                }
            }
        }
        listKategori.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            if (newValue != null) {
                kategoriTerpilih(newValue)
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
        txtKategori.clear()

        btnSimpan.text = "Simpan"

        loadData()
        listKategori.selectionModel.clearSelection()
    }
    fun loadData() {
        val baseUrl = clientController?.url
        if (baseUrl.isNullOrBlank()) {
            Platform.runLater {
                PesanPeringatan.error("Data Kategori", "URL server belum diset.")
            }
            return
        }

        thread {
            try {
                val builder = HttpRequest.newBuilder()
                    .uri(URI.create("$baseUrl/api/data-kategori"))
                    .GET()
                    .header("Content-Type", "application/json")

                // Tambahkan Authorization bila ada
                clientController?.buildAuthHeader()?.let {
                    builder.header("Authorization", it)
                }

                val request = builder.build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() in 200..299) {

                    // Decode JSON → List<KategoriDTO>
                    val list = json.decodeFromString<List<DataKategoriDTO>>(response.body())
                    val sortedList = list.sortedBy { it.kategori }

                    Platform.runLater {
                        listKategori.items = FXCollections.observableArrayList(sortedList)
                    }

                } else {
                    Platform.runLater {
                        PesanPeringatan.error("Load Data", "Server error ${response.statusCode()}")
                    }
                }

            } catch (ex: Exception) {
                Platform.runLater {
                    PesanPeringatan.error("Load Data", ex.message ?: "Gagal memuat data kategori")
                }
            }
        }
    }
    fun tutup() {
        parentController?.tutupForm()
    }
    fun getKategoriTerpilih(): DataKategoriDTO? {
        return listKategori.selectionModel.selectedItem
    }
    fun kategoriTerpilih(dto: DataKategoriDTO){
        txtKategori.text = dto.kategori
        btnSimpan.text = "Ubah"
    }
    fun simpanKategori() {
        if (btnSimpan.text == "Simpan"){
            val kategori = txtKategori.text.trim()

            if (kategori.isEmpty()) {
                PesanPeringatan.warning("Peringatan","Semua field harus diisi!")
                return
            }

            Thread {
                try {
                    val dto = DataKategoriDTO(kategori = kategori)
                    val body = json.encodeToString(dto)
                    val builder = HttpRequest.newBuilder()
                        .uri(URI.create("${clientController?.url}/api/data-kategori"))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/json")

                    clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                    val req = builder.build()
                    val resp = clientController?.makeRequest(req)

                    if (resp?.statusCode() in 200..299) {
                        Platform.runLater {
                            PesanPeringatan.info("Simpan Data","Data kategori berhasil disimpan.")
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
            val kategoriDTO = getKategoriTerpilih()
            val id = kategoriDTO?.id
            val kategori = txtKategori.text.trim()

            if (id == null) {
                PesanPeringatan.error("Udah Data", "Data ID tidak ditemukan!")
                return
            }
            if (kategori.isEmpty()) {
                PesanPeringatan.warning("Peringatan","Semua field harus diisi!")
                return
            }

            val konfirm = PesanPeringatan.confirm("Ubah Data Kategori", "Anda yakin ingi menyimpan perubahan data?")
            if(konfirm) {
                Thread {
                    try {
                        val dto =
                            DataKategoriDTO(id = id, kategori = kategori)
                        val body = json.encodeToString(dto)
                        val builder = HttpRequest.newBuilder()
                            .uri(URI.create("${clientController?.url}/api/data-kategori/${id}"))
                            .PUT(HttpRequest.BodyPublishers.ofString(body))
                            .header("Content-Type", "application/json")

                        clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                        val req = builder.build()
                        val resp = clientController?.makeRequest(req)

                        if (resp?.statusCode() in 200..299) {
                            Platform.runLater {
                                PesanPeringatan.info("Udah Data", "Data kategori berhasil diperbarui.")
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
        val kategoriDTO = getKategoriTerpilih()
        if (kategoriDTO == null) {
            PesanPeringatan.error("Hapus Data", "Tidak ada kategori yang dipilih.")
            return
        }
        val id = kategoriDTO.id
        if (id == null) {
            PesanPeringatan.error("Hapus Data", "ID pengguna tidak tersedia.")
            return
        }

        val konfirm = PesanPeringatan.confirm("Hapus Data","Anda yakin ingin menghapus data ini?")
        if (konfirm) {
            Thread {
                try {
                    val builder = HttpRequest.newBuilder()
                        .uri(URI.create("${clientController?.url}/api/data-kategori/$id"))
                        .DELETE()

                    clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                    val request = builder.build()
                    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                    Platform.runLater {
                        if (response.statusCode() in 200..299) {
                            PesanPeringatan.info("Hapus Data", "Data Kategori berhasil dihapus.")
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
                        PesanPeringatan.error("Hapus Data", ex.message ?: "Gagal menghapus data kategori")
                    }
                }
            }.start()
        }
    }

}