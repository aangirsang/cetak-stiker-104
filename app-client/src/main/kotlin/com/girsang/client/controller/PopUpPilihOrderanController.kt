package com.girsang.client.controller

import client.util.PesanPeringatan
import com.girsang.client.dto.DataOrderanDTO
import com.girsang.client.dto.DataOrderanRinciDTO
import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.DatePicker
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.stage.Stage
import javafx.util.StringConverter
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.ResourceBundle

class PopUpPilihOrderanController : Initializable {

    @FXML private lateinit var txtCariFaktur: TextField
    @FXML private lateinit var txtCariNamaUMKM: TextField
    @FXML private lateinit var dpCariTangalMulai: DatePicker
    @FXML private lateinit var dpCariTangalAkhir: DatePicker

    @FXML private lateinit var btnRefresh: Button
    @FXML private lateinit var btnPilih: Button
    @FXML private lateinit var btnTutup: Button

    @FXML private lateinit var tblOrderan: TableView<DataOrderanDTO>
    @FXML private lateinit var kolFaktur: TableColumn<DataOrderanDTO, String>
    @FXML private lateinit var kolTanggal: TableColumn<DataOrderanDTO, String>
    @FXML private lateinit var kolNamaUMKM: TableColumn<DataOrderanDTO, String>
    @FXML private lateinit var kolTotal: TableColumn<DataOrderanDTO, Int>

    @FXML private lateinit var tblOrderanRinci: TableView<DataOrderanRinciDTO>
    @FXML private lateinit var kolKodeStiker: TableColumn<DataOrderanRinciDTO, String>
    @FXML private lateinit var kolNamaStiker: TableColumn<DataOrderanRinciDTO, String>
    @FXML private lateinit var kolJumlah: TableColumn<DataOrderanRinciDTO, Int>

    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID"))
    val title = "Data Orderan"
    var  selectedOrderan: DataOrderanDTO? = null

    private val client = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }

    private var clientController: MainClientAppController? = null
    private var searchThread: Thread? = null

    fun setClientController(controller: MainClientAppController) {
        this.clientController = controller
        if (!controller.url.isNullOrBlank()) {
            bersih() // ✅ baru panggil setelah URL diset
        } else {
            println("⚠️ URL belum di-set, data tidak bisa dimuat")
        }
    }
    fun setData(list: List<DataOrderanDTO>) {
        tblOrderan.items = FXCollections.observableArrayList(list)
    }

    override fun initialize(p0: URL?, p1: ResourceBundle?) {

        // ===== Table Orderan =====
        kolFaktur.setCellValueFactory { SimpleStringProperty(it.value.faktur) }
        kolTanggal.setCellValueFactory { cellData ->
            val tanggal = cellData.value.tanggal?.let {
                Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            SimpleStringProperty(tanggal?.format(formatter) ?: "")
        }
        kolNamaUMKM.setCellValueFactory { SimpleStringProperty(it.value.umkm?.namaUmkm) }

        kolTotal.setCellValueFactory { SimpleIntegerProperty(it.value.totalStiker).asObject() }

        // ===== Table Rinci =====

        kolKodeStiker.setCellValueFactory { SimpleStringProperty(it.value.stikerKode) }
        kolNamaStiker.setCellValueFactory { SimpleStringProperty(it.value.stikerNama) }
        kolJumlah.setCellValueFactory { SimpleIntegerProperty(it.value.jumlah).asObject() }
        // ===== DatePicker Converter =====
        dpCariTangalMulai.converter = object : StringConverter<LocalDate>() {
            override fun toString(date: LocalDate?): String = date?.format(formatter) ?: ""
            override fun fromString(string: String?): LocalDate? = if (string.isNullOrBlank()) null else LocalDate.parse(string, formatter)
        }

        dpCariTangalAkhir.converter = object : StringConverter<LocalDate>() {
            override fun toString(date: LocalDate?): String = date?.format(formatter) ?: ""
            override fun fromString(string: String?): LocalDate? = if (string.isNullOrBlank()) null else LocalDate.parse(string, formatter)
        }

        // ===== Selection Listener =====
        tblOrderan.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            selectedOrderan = tblOrderan.selectionModel.selectedItem
            loadDataOrderanRinci()
        }

        btnRefresh.setOnAction {bersih()}
        btnPilih.setOnAction {
            if(selectedOrderan == null){
                PesanPeringatan.error(title,"Tidak ada data yang dipilih. Silakan pilih salah satu UMKM dari tabel terlebih dahulu.")
            }
            val stage = btnTutup.scene?.window as? Stage
            stage?.close()
        }
        btnTutup.setOnAction {
            bersih()
            val stage = btnTutup.scene?.window as? Stage
            stage?.close()
        }

        setupSearchListener(txtCariFaktur)
    }

    fun bersih() {
        selectedOrderan = null

        txtCariFaktur.clear()
        txtCariNamaUMKM.clear()
        dpCariTangalMulai.value = null
        dpCariTangalAkhir.value = null

        txtCariFaktur.promptText = "Cari Faktur"
        txtCariNamaUMKM.promptText = "Cari Nama Usaha"
        dpCariTangalMulai.promptText = "Start Tanggal"
        dpCariTangalAkhir.promptText = "End Tanggal"

        loadDataOrderan()
    }

    fun loadDataOrderan() {
        Thread {
            try {
                val builder = HttpRequest.newBuilder()
                    .uri(URI.create("${clientController?.url}/api/data-orderan"))
                    .GET()
                    .header("Content-Type", "application/json")
                clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }
                val request = builder.build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() in 200..299) {
                    val list = json.decodeFromString<List<DataOrderanDTO>>(response.body())
                    Platform.runLater { tblOrderan.items = FXCollections.observableArrayList(list) }
                } else Platform.runLater { PesanPeringatan.error(title,"Server Error ${response.statusCode()}") }

            } catch (ex: Exception) {
                Platform.runLater { PesanPeringatan.error(title,ex.message ?: "Gagal memuat data orderan stiker") }
            }
        }.start()
    }

    fun loadDataOrderanRinci() {
        val orderan = selectedOrderan ?: run {
            tblOrderanRinci.items.clear()
            return
        }
        tblOrderanRinci.items = FXCollections.observableArrayList(orderan.rincian)
    }
    private fun setupSearchListener(field: TextField) {
        field.textProperty().addListener { _, _, newValue ->
            searchThread?.interrupt() // hentikan thread sebelumnya jika user masih mengetik
            searchThread = Thread {
                try {
                    Thread.sleep(300) // debounce 300ms
                    if (Thread.interrupted()) return@Thread

                    if (newValue.isNullOrBlank()) {
                        Platform.runLater { loadDataOrderan() }
                    } else {
                        cariFaktur(newValue)
                    }
                } catch (_: InterruptedException) {
                }
            }
            searchThread?.start()
        }
    }
    fun cariFaktur(faktur: String){
        if (clientController?.url.isNullOrBlank()) {
            Platform.runLater { PesanPeringatan.error(title,"URL server belum di set") }
            return
        }
        Thread {
            try {
                val builder = HttpRequest.newBuilder()
                    .uri(URI.create("${clientController?.url}/api/orderan-stiker/cari-faktur?faktur=$faktur"))
                    .GET()
                    .header("Content-Type", "application/json")
                clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }
                val request = builder.build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() in 200..299) {
                    val list = json.decodeFromString<List<DataOrderanDTO>>(response.body())
                    Platform.runLater { tblOrderan.items = FXCollections.observableArrayList(list) }
                } else Platform.runLater { PesanPeringatan.error(title,"Server Error ${response.statusCode()}") }

            } catch (ex: Exception) {
                Platform.runLater { PesanPeringatan.error(title,ex.message ?: "Gagal memuat data orderan stiker") }
            }
        }.start()
    }
}
