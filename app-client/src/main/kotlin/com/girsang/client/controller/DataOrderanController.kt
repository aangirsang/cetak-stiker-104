package com.girsang.client.controller

import client.util.LocalDateTimeSerializer
import client.util.PesanPeringatan
import com.girsang.client.dto.DataOrderanDTO
import com.girsang.client.dto.DataOrderanRinciDTO
import com.girsang.client.dto.DataStikerDTO
import com.girsang.client.dto.DataUmkmDTO
import com.girsang.client.util.SessionClient
import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.control.cell.TextFieldTableCell
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.converter.IntegerStringConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.ResourceBundle

class DataOrderanController : Initializable {

    private var selectedUmkm: DataUmkmDTO? = null
    private var selectedStiker: DataStikerDTO? = null
    private var selectedOrder: DataOrderanDTO? = null

    private val client = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }

    private var clientController: MainClientAppController? = null
    private var parentController: MainClientAppController? = null

    fun setClientController(controller: MainClientAppController) {
        this.clientController = controller  // ✅ simpan controller dulu

        if (!controller.url.isNullOrBlank()) {
            bersih() // ✅ baru panggil setelah URL diset
        } else {
            println("⚠️ URL belum di-set, data tidak bisa dimuat")
        }
    }
    fun setParentController(controller: MainClientAppController) {
        this.parentController = controller
    }

    @FXML private lateinit var lblTotalStiker: Label
    @FXML private lateinit var txtNamaUsaha: TextField
    @FXML private lateinit var txtNamaPemilik: TextField
    @FXML private lateinit var txtInstagram: TextField
    @FXML private lateinit var txtTanggal: TextField
    @FXML private lateinit var txtFaktur: TextField
    @FXML private lateinit var txtKontak: TextField

    @FXML private lateinit var btnCariUMKM: Button
    @FXML private lateinit var btnTambahkan: Button
    @FXML private lateinit var btnSimpan: Button
    @FXML private lateinit var btnHapus: Button
    @FXML private lateinit var btnRefresh: Button
    @FXML private lateinit var btnTutup: Button

    @FXML private lateinit var tblStiker: TableView<DataOrderanRinciDTO>
    @FXML private lateinit var kolKodeStiker: TableColumn<DataOrderanRinciDTO, Int>
    @FXML private lateinit var kolNamaStiker: TableColumn<DataOrderanRinciDTO, String>
    @FXML private lateinit var kolUkuran: TableColumn<DataOrderanRinciDTO, String>
    @FXML private lateinit var kolJumlah: TableColumn<DataOrderanRinciDTO, Int>

    override fun initialize(location: URL?, resources: ResourceBundle?) {

        kolKodeStiker.setCellValueFactory { cellData ->
            SimpleIntegerProperty(
                tblStiker.items.indexOf(cellData.value) + 1
            ).asObject()
        }
        kolKodeStiker.text = "No."

        kolNamaStiker.setCellValueFactory { SimpleStringProperty(it.value.stikerNama) }
        kolUkuran.setCellValueFactory {
            SimpleStringProperty(it.value.ukuran)
        }
        tblStiker.isEditable = true
        kolJumlah.setCellValueFactory { SimpleIntegerProperty(it.value.jumlah).asObject() }
        kolJumlah.cellFactory = TextFieldTableCell.forTableColumn(IntegerStringConverter())

        btnCariUMKM.setOnAction {showCariUmkmPopup()}
        btnRefresh.setOnAction { bersih() }
        btnTambahkan.setOnAction { showCariStikerPopup() }
        btnSimpan.setOnAction { onSimpanOrderan() }
        btnHapus.setOnAction { onHapusOrderan() }
        btnTutup.setOnAction { parentController?.tutupForm() }

        txtFaktur.setOnMouseClicked { event ->
            if (event.clickCount == 2) {
                showCariOrderaPopup()
            }
        }

        kolJumlah.setOnEditCommit { event ->
            var stiker = event.rowValue
            val nilaiBaru = event.newValue ?: 0

            if (nilaiBaru <= 0) {
                // ⚠️ Hapus item dari tabel
                event.tableView.items.remove(stiker)
                println("❌ Baris dihapus karena jumlah <= 0")
            } else {
                // ✅ Update jumlah dan refresh tampilan
                stiker.jumlah = nilaiBaru
                event.tableView.refresh()
                println("Jumlah diubah jadi: $nilaiBaru")
            }

            // 🔹 Hitung ulang total setelah setiap edit
            hitungTotalStiker()
        }

    }
    fun bersih(){
        selectedUmkm = null
        selectedOrder = null
        selectedStiker = null
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        txtTanggal.text = LocalDate.now().format(formatter)

        lblTotalStiker.text = "Total Stiker = 0"
        txtNamaUsaha.clear()
        txtNamaPemilik.clear()
        txtInstagram.clear()
        txtFaktur.clear()
        txtKontak.clear()

        txtNamaUsaha.promptText = "Nama Usaha"
        txtNamaPemilik.promptText = "Nama Pemilik Usaha"
        txtInstagram.promptText = "Akun Instagram Usaha"
        txtKontak.promptText = "Kontak Pemilik Usaha"
        txtFaktur.clear()

        btnSimpan.text = "Simpan"
        btnCariUMKM.isDisable = false

        tblStiker.selectionModel.clearSelection()
        fakturOtomatis()
        tblStiker.items.clear()
    }
    fun umkmTerpilih(dto: DataUmkmDTO){
        println("UMKM Terpilih: ${dto.namaUmkm}")
        selectedUmkm = dto
        txtNamaUsaha.text = dto.namaUmkm
        txtNamaPemilik.text = dto.namaPemilikUmkm
        txtInstagram.text = dto.instagramNama
        txtKontak.text = dto.noTelpon
    }
    fun stikerTerpilih(dto: DataStikerDTO){
        selectedStiker = dto
    }
    fun showCariUmkmPopup() {
        selectedUmkm = null
        try {
            // 🔹 Ambil data dari server
            val builder = HttpRequest.newBuilder()
                .uri(URI.create("${clientController?.url}/api/data-umkm"))
                .GET()
                .header("Content-Type", "application/json")

            clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }
            val request = builder.build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() !in 200..299) {
                PesanPeringatan.error("Data Orderan","Gagal memuat data UMKM (${response.statusCode()})")
                return
            }

            val list = json.decodeFromString<List<DataUmkmDTO>>(response.body())

            // 🔹 Muat FXML popup
            val loader = FXMLLoader(javaClass.getResource("/fxml/popup-pilih-umkm.fxml"))
            val root = loader.load<Parent>()
            val controller = loader.getController<PopUpPilihUMKMController>()
            controller.setClientController(clientController!!)
            controller.setData(list)

            val stage = Stage()
            stage.title = "Pilih Data UMKM"
            stage.scene = Scene(root)
            stage.initModality(Modality.APPLICATION_MODAL)
            stage.showAndWait()

            // 🔹 Setelah popup ditutup
            val selected = controller.selectedUmkm
            if (selected != null) {
                umkmTerpilih(selected)
            }

        } catch (e: Exception) {
            PesanPeringatan.error("Data UMKM", "Error: ${e.message}")
        }
    }
    fun showCariOrderaPopup() {
        try {
            // 🔹 Ambil data orderan dari server
            val builder = HttpRequest.newBuilder()
                .uri(URI.create("${clientController?.url}/api/data-orderan"))
                .GET()
                .header("Content-Type", "application/json")

            clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

            val response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() !in 200..299) {
                PesanPeringatan.error("Data Orderan", "Gagal memuat data orderan (${response.statusCode()})")
                return
            }

            val list = json.decodeFromString<List<DataOrderanDTO>>(response.body())

            // 🔹 Muat FXML popup dengan pengecekan null
            val fxmlUrl = javaClass.getResource("/fxml/popup-pilih-orderan.fxml")
            val loader = FXMLLoader(fxmlUrl)
            val root = loader.load<Parent>()
            val controller = loader.getController<PopUpPilihOrderanController>()

            val clientCtrl = clientController
            if (clientCtrl == null) {
                PesanPeringatan.error("Error", "ClientController belum diinisialisasi!")
                return
            }
            controller.setClientController(clientCtrl)
            controller.setData(list) // pastikan method setData ada di PopUpPilihOrderanController

            val stage = Stage()
            stage.title = "Pilih Data Orderan"
            stage.scene = Scene(root)
            stage.initModality(Modality.APPLICATION_MODAL)
            stage.showAndWait()

            // 🔹 Setelah popup ditutup
            val selected = controller.selectedOrderan
            if (selected != null) {
                println("Faktur: ${selected.faktur}")
                println("umkm: ${selected.umkm}")
                selectedOrder = selected
                selectedUmkm = selected.umkm
                umkmTerpilih(selectedUmkm!!)
                loadDataOrderRinci(selected)

                txtFaktur.text = selected.faktur
                val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
                selected.tanggal?.let {
                    val tanggal = Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    txtTanggal.text = tanggal.format(formatter)
                } ?: run {
                    txtTanggal.text = "-"
                }

                lblTotalStiker.text = "Total Stiker = ${selected.totalStiker} Lembar"
                btnSimpan.text = "Update"
                btnCariUMKM.isDisable = true
            }

        } catch (e: Exception) {
            e.printStackTrace()
            PesanPeringatan.error("Data Orderan", "Error PopUp Orderan: ${e.message}")
        }
    }
    fun showCariStikerPopup() {
        try {
            // 🔹 Ambil data dari server
            if(selectedUmkm==null){
                PesanPeringatan.error("Pilih Setiker", "Pilih UMKM terlebih dahulu!")
                return
            }
            val idUMKM = selectedUmkm?.id
            val builder = HttpRequest.newBuilder()
                .uri(URI.create("${clientController?.url}/api/data-stiker/umkm/$idUMKM"))
                .GET()
                .header("Content-Type", "application/json")

            clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }
            val request = builder.build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() !in 200..299) {
                PesanPeringatan.error("Pilih Stiker", "Gagal memuat data stiker (${response.statusCode()})")
                return
            }

            val list = json.decodeFromString<List<DataStikerDTO>>(response.body())

            // 🔹 Muat FXML popup
            val loader = FXMLLoader(javaClass.getResource("/fxml/popup-pilih-stiker.fxml"))
            val root = loader.load<Parent>()
            val controller = loader.getController<PopUpPilihStikerController>()
            controller.setClientController(clientController!!)
            controller.setData(list, selectedUmkm!!)

            val stage = Stage()
            stage.title = "Pilih Data Stiker"
            stage.scene = Scene(root)
            stage.initModality(Modality.APPLICATION_MODAL)
            stage.showAndWait()

            // 🔹 Setelah popup ditutup
            val selected = controller.selectedStiker
            if (selected != null) {
                stikerTerpilih(selected)
                tambahStiker()
            }

        } catch (e: Exception) {
            PesanPeringatan.error("Pilih Setiker", "Error: ${e.message}")
        }
    }
    fun loadDataOrderRinci(order: DataOrderanDTO?) {
        if (order == null) {
            tblStiker.items.clear()
            return
        }

        // 🔹 Tambahkan indikator loading di tengah tabel
        val loadingIndicator = ProgressIndicator()
        loadingIndicator.progress = -1.0
        loadingIndicator.maxWidth = 50.0
        loadingIndicator.maxHeight = 50.0
        tblStiker.placeholder = loadingIndicator

        Thread {
            try {
                val id = order.id ?: return@Thread

                val builder = HttpRequest.newBuilder()
                    .uri(URI.create("${clientController?.url}/api/data-orderan/$id"))
                    .GET()
                    .header("Content-Type", "application/json")

                clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                val request = builder.build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() in 200..299) {
                    val body = response.body().trim()

                    // 🔹 Decode 1 orderan, bukan list!
                    val orderan = json.decodeFromString<DataOrderanDTO>(body)
                    val rincianList = orderan.rincian ?: emptyList()

                    Platform.runLater {
                        if (rincianList.isEmpty()) {
                            tblStiker.placeholder = Label("Tidak ada rincian orderan.")
                        }
                        tblStiker.items = FXCollections.observableArrayList(rincianList)
                    }

                } else {
                    Platform.runLater {
                        tblStiker.placeholder = Label("Gagal memuat data (${response.statusCode()})")
                        PesanPeringatan.error("Data Orderan", "Server Error ${response.statusCode()}")
                    }
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
                Platform.runLater {
                    tblStiker.placeholder = Label("Gagal memuat rincian orderan.")
                    PesanPeringatan.error("Data Orderan", "Gagal memuat rincian orderan: ${ex.message}")
                }
            }
        }.start()
    }
    fun fakturOtomatis(){
        Thread {
            try {
                val builder = HttpRequest.newBuilder()
                    .uri(URI.create("${clientController?.url}/api/data-orderan/faktur"))
                    .GET()
                    .header("Content-Type", "application/json")

                // Tambahkan Authorization header jika diperlukan
                clientController?.buildAuthHeader()?.let {
                    builder.header("Authorization", it)
                }

                val request = builder.build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() in 200..299) {
                    val faktur = response.body().trim()
                    Platform.runLater {
                        txtFaktur.text = faktur
                    }
                } else if (response.statusCode() == 401) {
                    Platform.runLater {
                        PesanPeringatan.error("Faktur Otomatis","Akses ditolak (401). Silakan login terlebih dahulu.")
                    }
                } else {
                    Platform.runLater {
                        PesanPeringatan.error("Faktur Otomatis", "Server Error ${response.statusCode()}")
                    }
                }
            } catch (ex: Exception) {
                Platform.runLater {
                    PesanPeringatan.error("Faktur Otomatis", ex.message ?: "Gagal mengambil nomor faktur otomatis")
                }
            }
        }.start()
    }
    fun tambahStiker(){
        if (selectedUmkm == null) {
            PesanPeringatan.warning("Pilih Stiker", "Pilih UMKM terlebih dahulu!")
            return
        }
        val jumlah = 1

        val currentItems = tblStiker.items ?: FXCollections.observableArrayList()

        // Cek apakah stiker sudah ada
        val existing = currentItems.find { it.stiker?.id == selectedStiker?.id }
        if (existing != null) {
            // Jika sudah ada, tambahkan jumlahnya
            existing.jumlah += jumlah
        } else {
            // Jika belum ada, buat baris baru
            val rinci = DataOrderanRinciDTO(
                stiker = selectedStiker!!,
                stikerId = selectedStiker?.id!!,
                stikerKode = selectedStiker!!.kodeStiker,
                stikerNama = selectedStiker!!.namaStiker,
                ukuran = "${selectedStiker!!.panjang} x ${selectedStiker!!.lebar}",
                jumlah = jumlah
            )
            currentItems.add(rinci)
        }

        tblStiker.items = currentItems
        tblStiker.refresh()
        hitungTotalStiker()

        selectedStiker = null
    }
    fun hitungTotalStiker() {
        val total = tblStiker.items.sumOf { it.jumlah }
        lblTotalStiker.text = "Total Stiker = $total Lembar"
    }
    fun onSimpanOrderan() {
        if (selectedUmkm == null) {
            PesanPeringatan.warning("Simpan Data", "Pilih UMKM terlebih dahulu!")
            return
        }
        if (tblStiker.items.isEmpty()) {
            PesanPeringatan.warning("Simpan Data", "Belum ada stiker yang ditambahkan!")
            return
        }
        //simpan
        if(btnSimpan.text == "Simpan"){
            val orderan = DataOrderanDTO(
                id = 0,
                faktur = txtFaktur.text,
                tanggal = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                umkm = selectedUmkm,
                umkmId = selectedUmkm!!.id,
                penggunaId = SessionClient.penggunaLogin?.id!!,
                totalStiker = tblStiker.items.sumOf { it.jumlah },
                rincian = tblStiker.items.map {
                    DataOrderanRinciDTO(
                        stikerId = it.stikerId,
                        stikerKode = it.stiker?.kodeStiker ?: "",
                        stikerNama = it.stiker?.namaStiker ?: "",
                        ukuran = "${it.stiker?.panjang} x ${it.stiker?.lebar}",
                        jumlah = it.jumlah
                    )
                }
            )

            Thread {
                try {
                    val body = json.encodeToString(orderan)
                    val builder = HttpRequest.newBuilder()
                        .uri(URI.create("${clientController?.url}/api/data-orderan"))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/json")

                    clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                    val req = builder.build()
                    val resp = clientController?.makeRequest(req)
                    if (resp?.statusCode() in 200..299) {
                        Platform.runLater {
                            PesanPeringatan.info("Simpan Data", "Orderan berhasil disimpan!")
                            bersih()
                        }
                    } else {
                        Platform.runLater {
                            PesanPeringatan.error("Simpan Data","Gagal menyimpan orderan: ${resp?.statusCode()}")
                            println("Server returned ${resp?.statusCode()} : ${resp?.body()}")
                        }
                    }
                } catch (e: Exception) {
                    Platform.runLater {
                        PesanPeringatan.error("Simpan Data", "Error: ${e.message}")
                    }
                }
            }.start()
        }
        //update
        else {
            val orderan = DataOrderanDTO(
                id = selectedOrder?.id!!,
                faktur = selectedOrder?.faktur.toString(),
                penggunaId = SessionClient.penggunaLogin?.id!!,
                tanggal = selectedOrder?.tanggal!!,
                umkm = selectedUmkm,
                umkmId = selectedUmkm!!.id,
                totalStiker = tblStiker.items.sumOf { it.jumlah },
                rincian = tblStiker.items.map {
                    DataOrderanRinciDTO(
                        stikerId = it.stikerId,
                        stikerKode = it.stiker?.kodeStiker ?: "",
                        stikerNama = it.stiker?.namaStiker ?: "",
                        ukuran = "${it.stiker?.panjang} x ${it.stiker?.lebar}",
                        jumlah = it.jumlah
                    )
                }
            )
            val konfirm = PesanPeringatan.confirm("Ubah Data", "Apakah anda yajin ingin menyimpan perubahan data ini?")
            if (konfirm){
                Thread {
                    try {
                        val id = selectedOrder?.id
                        val body = json.encodeToString(orderan)
                        val builder = HttpRequest.newBuilder()
                            .uri(URI.create("${clientController?.url}/api/data-orderan/$id"))
                            .PUT(HttpRequest.BodyPublishers.ofString(body))
                            .header("Content-Type", "application/json")

                        clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                        val req = builder.build()
                        val resp = clientController?.makeRequest(req)
                        if (resp?.statusCode() in 200..299) {
                            Platform.runLater {
                                PesanPeringatan.info("Ubah Data", "Orderan berhasil disimpan!")
                                bersih()
                            }
                        } else {
                            Platform.runLater {
                                PesanPeringatan.error("Ubah Data","Gagal menyimpan orderan: ${resp?.statusCode()}")
                            }
                        }
                    } catch (e: Exception) {
                        Platform.runLater {
                            PesanPeringatan.error("Simpan Data", "Error: ${e.message}")
                        }
                    }
                }.start()
            }
        }


    }
    fun onHapusOrderan(){
        val order = selectedOrder
        val id = order?.id
        if (order == null) {
            PesanPeringatan.warning("Hapus Data", "Tidak ada orderan yang dipilih.")
            return
        }
        val konfirm = PesanPeringatan.confirm("Hapus Data", "Anda yakin ingin menghapus data ini?")
            if (konfirm) {
                try {
                    val builder = HttpRequest.newBuilder()
                        .uri(URI.create("${clientController?.url}/api/data-orderan/$id"))
                        .DELETE()

                    clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                    val request = builder.build()
                    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                    Platform.runLater {
                        if (response.statusCode() in 200..299) {
                            bersih()
                            clientController?.showInfo("Orderan berhasil dihapus.")
                        } else {
                            PesanPeringatan.error("Data Orderan","Server returned ${response.statusCode()} : ${response.body()}")
                        }
                    }
                } catch (ex: Exception) {
                    Platform.runLater {
                        println("Gagal menghapus orderan")
                        PesanPeringatan.error("Data Orderan",ex.message ?: "Gagal menghapus orderan")
                    }
                }
            }
    }
}