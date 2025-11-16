package com.girsang.client.controller

import client.util.PesanPeringatan
import com.girsang.client.dto.DataKategoriDTO
import com.girsang.client.dto.DataUmkmDTO
import javafx.application.Platform
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.text.Text
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.ResourceBundle

class DataUmkmController : Initializable{

    private val client = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }


    @FXML private lateinit var btnSimpan: Button
    @FXML private lateinit var btnRefresh: Button
    @FXML private lateinit var btnHapus: Button
    @FXML private lateinit var btnTutup: Button

    @FXML private lateinit var lblTotal: Label

    @FXML private lateinit var txtNamaUsaha: TextField
    @FXML private lateinit var txtNamaPemilik: TextField
    @FXML private lateinit var txtNoKtp: TextField
    @FXML private lateinit var txtEmail: TextField
    @FXML private lateinit var txtAlamat: TextArea
    @FXML private lateinit var txtNoTelp: TextField
    @FXML private lateinit var txtInstagram: TextField
    @FXML private lateinit var txtFacebook: TextField
    @FXML private lateinit var txtCariNamaPemilik: TextField
    @FXML private lateinit var txtCariNamaUsaha: TextField
    @FXML private lateinit var txtCariAlamat: TextField

    @FXML private lateinit var dpTglLahir: DatePicker

    @FXML private lateinit var cboKategoriUsaha: ComboBox<DataKategoriDTO>

    @FXML private lateinit var chkStatus: CheckBox

    @FXML private lateinit var tblUmkm: TableView<DataUmkmDTO>

    @FXML private lateinit var kolNo: TableColumn<DataUmkmDTO, Int>
    @FXML private lateinit var kolNamaUsaha: TableColumn<DataUmkmDTO, String>
    @FXML private lateinit var kolNamaPemilik: TableColumn<DataUmkmDTO, String>
    @FXML private lateinit var kolKtp: TableColumn<DataUmkmDTO, String>
    @FXML private lateinit var kolTglLahir: TableColumn<DataUmkmDTO, String>
    @FXML private lateinit var kolEmail: TableColumn<DataUmkmDTO, String>
    @FXML private lateinit var kolKontak: TableColumn<DataUmkmDTO, String>
    @FXML private lateinit var kolInstagram: TableColumn<DataUmkmDTO, String>
    @FXML private lateinit var kolFacebook: TableColumn<DataUmkmDTO, String>
    @FXML private lateinit var kolStatus: TableColumn<DataUmkmDTO, String>

    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

    private var clientController: MainClientAppController? = null
    private var parentController: MainClientAppController? = null
    private var searchThread: Thread? = null

    override fun initialize(p0: URL?, p1: ResourceBundle?) {

        //Tabel UMKM
        kolNo.setCellValueFactory { cellData ->
            SimpleIntegerProperty(
                tblUmkm.items.indexOf(cellData.value) + 1
            ).asObject()
        }

        kolNamaUsaha.setCellValueFactory {SimpleStringProperty(it.value.namaUmkm)}
        kolNamaPemilik.setCellValueFactory {SimpleStringProperty(it.value.namaPemilikUmkm)}
        kolKtp.setCellValueFactory {SimpleStringProperty(it.value.noKtp)}

        kolTglLahir.setCellValueFactory {
            val millis = it.value.tglLahir
            val date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            SimpleStringProperty(date.format(formatter))
        }

        kolEmail.setCellValueFactory {SimpleStringProperty(it.value.email)}
        kolKontak.setCellValueFactory {SimpleStringProperty(it.value.noTelpon)}
        kolInstagram.setCellValueFactory {SimpleStringProperty(it.value.instagramNama)}
        kolFacebook.setCellValueFactory {SimpleStringProperty(it.value.facebookNama)}

        kolStatus.setCellValueFactory { cellData ->
            val status = cellData.value.status
            SimpleStringProperty(if (status) "Aktif" else "Non-Aktif")
        }

        btnTutup.setOnAction { parentController?.tutupForm() }
        btnRefresh.setOnAction { bersih() }
        btnSimpan.setOnAction { simpanDataUmkm() }
        btnHapus.setOnAction { hapusData() }

        tblUmkm.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            if (newValue != null) {
                umkmTerpilih(newValue)
            }
        }
        fun wrapColumnText(column: TableColumn<DataUmkmDTO, String>) {
            column.setCellFactory {
                TableCell<DataUmkmDTO, String>().apply {
                    val textNode = Text()
                    textNode.wrappingWidthProperty().bind(column.widthProperty().subtract(15)) // biar pas kolomnya
                    textNode.textProperty().bind(itemProperty())

                    graphic = textNode
                    contentDisplay = ContentDisplay.GRAPHIC_ONLY
                }
            }
        }

        setupSearchListener(txtCariNamaPemilik)
        setupSearchListener(txtCariNamaUsaha)
        setupSearchListener(txtCariAlamat)


        tblUmkm.columnResizePolicy = TableView.UNCONSTRAINED_RESIZE_POLICY

    }
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
    private fun setupSearchListener(field: TextField) {
        field.textProperty().addListener { _, _, newValue ->
            searchThread?.interrupt() // hentikan thread sebelumnya jika user masih mengetik
            searchThread = Thread {
                try {
                    Thread.sleep(300) // debounce 300ms
                    if (Thread.interrupted()) return@Thread

                    if (newValue.isNullOrBlank()) {
                        Platform.runLater { loadDataUMKM() }
                    } else {
                        cariDataUmkm(txtCariNamaPemilik.text,
                            txtCariNamaUsaha.text,
                            txtCariAlamat.text)
                    }
                } catch (_: InterruptedException) {
                    println("error")
                }
            }
            searchThread?.start()
        }
    }
    fun bersih(){
        lblTotal.text = "Total Data Dalam Tabel: 0"

        txtNamaUsaha.clear()
        txtNamaPemilik.clear()
        txtNoKtp.clear()
        txtEmail.clear()
        txtAlamat.clear()
        txtNoTelp.clear()
        txtInstagram.clear()
        txtFacebook.clear()

        dpTglLahir.value = null
        cboKategoriUsaha.selectionModel.clearSelection()
        chkStatus.isSelected = false

        txtCariNamaPemilik.clear()
        txtCariNamaUsaha.clear()
        txtCariAlamat.clear()

        txtAlamat.isWrapText = true

        txtNamaUsaha.promptText = "Data Nama Usaha"
        txtNamaPemilik.promptText = "Data Nama Pemilik Usaha"
        txtNoKtp.promptText = "Data Nomor KTP Pemilik Usaha"
        txtEmail.promptText = "Data Email Pemilik Usaha"
        txtAlamat.promptText = "Data Alamat"
        txtNoTelp.promptText = "Data Kontak No. Handphone atau WhatsApp"
        txtInstagram.promptText = "Nama Akun Instagram"
        txtFacebook.promptText = "Nama Akun Facebook"

        txtCariNamaPemilik.promptText = "Cari Nama Pemilik Usaha"
        txtCariNamaUsaha.promptText = "Cari Nama Usaha"
        txtCariAlamat.promptText = "Cari Alamat Usaha"

        tblUmkm.selectionModel.clearSelection()

        btnSimpan.text = "Simpan"

        loadDataUMKM()
        loadKategori()
    }
    fun loadDataUMKM(){
        Thread {
            try {
                val builder = HttpRequest.newBuilder()
                    .uri(URI.create("${clientController?.url}/api/data-umkm"))
                    .GET()
                    .header("Content-Type", "application/json")

                clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                val request = builder.build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                if(response.statusCode() in 200..299){
                    val list = json.decodeFromString<List<DataUmkmDTO>>(response.body())
                    Platform.runLater {
                        tblUmkm.items = FXCollections.observableArrayList(list)
                        lblTotal.text = "Total Data Dalam Tabel: ${tblUmkm.items.count()}"
                    }
                } else {
                    Platform.runLater {
                        PesanPeringatan.error("Load Data UMKM","Server Error ${response.statusCode()}")
                    }
                }
            } catch (ex: Exception){
                Platform.runLater {
                    PesanPeringatan.error("Load Data UMKM",ex.message ?: "Gagal memeuat data UMKM")
                }
            }
        }.start()
    }
    fun loadKategori() {
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
                    .uri(URI.create("$baseUrl/api/data-kategori"))
                    .GET()
                    .header("Content-Type", "application/json")
                    .apply {
                        clientController?.buildAuthHeader()?.let { header("Authorization", it) }
                    }
                    .build()

                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() in 200..299) {
                    val list = json.decodeFromString<List<DataKategoriDTO>>(response.body())
                    val sortedList = list.sortedBy { it.kategori }

                    Platform.runLater {
                        cboKategoriUsaha.items = FXCollections.observableArrayList(sortedList)
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
    fun simpanDataUmkm() {
        val namaPemilik = txtNamaPemilik.text.trim()
        val namaUsaha = txtNamaUsaha.text.trim()
        val noKtp = txtNoKtp.text.trim()
        val email = txtEmail.text.trim()
        val tglLahir = dpTglLahir.value
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val alamat = txtAlamat.text.trim()
        val noTelp = txtNoTelp.text.trim()
        val instagram = txtInstagram.text.trim()
        val facebook = txtFacebook.text.trim()
        val status = chkStatus.isSelected

        // 🔥 Aman dari null
        val kategori = cboKategoriUsaha.selectionModel.selectedItem
        if (kategori?.id == null) {
            PesanPeringatan.warning("Simpan Data UMKM", "Kategori belum valid atau belum memiliki ID!")
            return
        }

        if (namaPemilik.isEmpty() ||
            namaUsaha.isEmpty() ||
            noKtp.isEmpty() ||
            email.isEmpty() ||
            tglLahir <=0 ||
            noTelp.isEmpty() ||
            instagram.isEmpty()||
            facebook.isEmpty()||
            alamat.isEmpty()){
            PesanPeringatan.warning("Simpan Data UMKM","Semua field harus diisi!")
            return
        }

        if (btnSimpan.text == "Simpan") {
            Thread{
                try{
                    val dto = DataUmkmDTO(
                        namaUmkm = namaUsaha,
                        namaPemilikUmkm = namaPemilik,
                        noKtp = noKtp,
                        email = email,
                        tglLahir = tglLahir,
                        alamat = alamat,
                        noTelpon = noTelp,
                        dataKategori = getKategoriTerpilih(),
                        facebookNama = facebook,
                        instagramNama = instagram,
                        status = status
                        )
                    val body = json.encodeToString(dto)
                    val builder = HttpRequest.newBuilder()
                        .uri(URI.create("${clientController?.url}/api/data-umkm"))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/json")

                    clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                    val req = builder.build()
                    val resp = clientController?.makeRequest(req)

                    println("Kategori yg terpilih: ${kategori.kategori}")

                    if(resp?.statusCode() in 200..299){
                        Platform.runLater {
                            PesanPeringatan.info("Simpan Data UMKM","Data UMKM berhasil disimpan.")
                            bersih()
                        }
                    }else {
                        Platform.runLater {
                            PesanPeringatan.error("Simpan Data UMKM","Server returned ${resp?.statusCode()} : ${resp?.body()}")
                            println("Server returned ${resp?.statusCode()} : ${resp?.body()}")
                        }
                    }
                } catch (ex: Exception) {
                    Platform.runLater {
                        PesanPeringatan.error("Simpan Data UMKM",ex.message ?: "Error saat menyimpan data")
                    }
                }
            }.start()
        } else {
            val umkm = getUmkmTerpilih()
            val id = umkm?.id

            if (id==null) {
                PesanPeringatan.error("Ubah Data UMKM","ID UMKM tidak tersedia")
                return
            }

            val konfirm = PesanPeringatan.confirm("Ubah Data UMKM", "Anda yakin ingi menyimpan perubahan data?")
            if(konfirm) {
                Thread {
                    try {
                        val dto = DataUmkmDTO(
                            id = id,
                            namaUmkm = namaUsaha,
                            namaPemilikUmkm = namaPemilik,
                            noKtp = noKtp,
                            email = email,
                            tglLahir = tglLahir,
                            alamat = alamat,
                            noTelpon = noTelp,
                            dataKategori = getKategoriTerpilih(),
                            facebookNama = facebook,
                            instagramNama = instagram,
                            status = status
                        )
                        val body = json.encodeToString(dto)
                        val builder = HttpRequest.newBuilder()
                            .uri(URI.create("${clientController?.url}/api/data-umkm/${id}"))
                            .PUT(HttpRequest.BodyPublishers.ofString(body))
                            .header("Content-Type", "application/json")

                        clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                        val req = builder.build()
                        val resp = clientController?.makeRequest(req)

                        if (resp?.statusCode() in 200..299) {
                            Platform.runLater {
                                PesanPeringatan.info("Udah Data UMKM", "Data UMKM berhasil diperbarui.")
                                bersih()
                            }
                        } else {
                            Platform.runLater {
                                println("Server returned ${resp?.statusCode()} : ${resp?.body()}")
                                PesanPeringatan.error("Ubah Data UMKM","Server returned ${resp?.statusCode()} : ${resp?.body()}")
                            }
                        }
                    } catch (ex: Exception) {
                        Platform.runLater {
                            PesanPeringatan.error("Ubah Data UMKM",ex.message ?:"Error saat memperbarui data" )
                        }
                    }
                }.start()
            }
        }
    }
    fun hapusData(){
        val umkm = getUmkmTerpilih()
        val id = umkm?.id
        if (umkm == null) {
            PesanPeringatan.error("Hapus Data", "Tidak ada UMKM yang dipilih.")
            return
        }

        if(id == null) {
            PesanPeringatan.error("Hapus Data", "ID UMKM tidak tersedia.")
            return
        }

        val konfirm = PesanPeringatan.confirm("Hapus Data","Anda yakin ingin menghapus data ini?")
        if (konfirm) {
            Thread {
                try {
                    val builder = HttpRequest.newBuilder()
                        .uri(URI.create("${clientController?.url}/api/data-umkm/${id}"))
                        .DELETE()

                    clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                    val request = builder.build()
                    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                    Platform.runLater {
                        if (response.statusCode() in 200..299) {
                            PesanPeringatan.info("Hapus Data", "UMKM berhasil dihapus.")
                            bersih()
                        } else {
                            PesanPeringatan.error(
                                "Hapus Data",
                                "Server returned ${response.statusCode()} : ${response.body()}")
                            println("Server returned ${response.statusCode()} : ${response.body()}")
                        }
                    }
                } catch (ex: Exception) {
                    Platform.runLater {
                        PesanPeringatan.error("Hapus Data", ex.message ?: "Gagal menghapus UMKM")
                    }
                }
            }.start()
        }
    }
    fun cariDataUmkm(namaPemilik: String, namaUsaha: String, alamat: String) {
        if (clientController?.url.isNullOrBlank()) {
            Platform.runLater { PesanPeringatan.error("Data UMKM","URL server belum di set") }
            return
        }

        Thread {
            try {
                val uri = "${clientController?.url}/api/data-umkm/cari?" +
                        "namaPemilik=${namaPemilik}&namaUsaha=${namaUsaha}&alamat=${alamat}"
                val builder = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .GET()
                    .header("Content-Type", "application/json")

                clientController?.buildAuthHeader()?.let { builder.header("Authorization", it) }

                val request = builder.build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() in 200..299) {
                    val hasil = json.decodeFromString<List<DataUmkmDTO>>(response.body())

                    Platform.runLater {
                        if (hasil.isEmpty()) {
                            // ⚠️ Kosongkan tabel jika tidak ada hasil
                            tblUmkm.items = FXCollections.observableArrayList()
                            clientController?.showInfo(
                                "Tidak ada data yang cocok untuk pencarian " +
                                        "\"$namaPemilik\" & \"$namaUsaha\"& \"$alamat\"")
                        } else {
                            // ✅ Tampilkan hasil pencarian
                            tblUmkm.items = FXCollections.observableArrayList(hasil)
                        }
                    }
                } else {
                    Platform.runLater {
                        PesanPeringatan.error("Data UMKM","Server Error ${response.statusCode()}")
                        tblUmkm.items = FXCollections.observableArrayList() // kosongkan tabel juga
                    }
                }
            } catch (ex: Exception) {
                Platform.runLater {
                    PesanPeringatan.error("Data UMKM",ex.message ?: "Gagal mencari data UMKM")
                    tblUmkm.items = FXCollections.observableArrayList() // kosongkan tabel jika error
                }
            }
        }.start()
    }
    fun umkmTerpilih(dto: DataUmkmDTO){
        txtNamaPemilik.text = dto.namaPemilikUmkm
        txtNamaUsaha.text = dto.namaUmkm
        txtNoTelp.text = dto.noTelpon
        txtInstagram.text = dto.instagramNama
        txtAlamat.text = dto.alamat

        txtNamaUsaha.text = dto.namaUmkm
        txtNamaPemilik.text = dto.namaPemilikUmkm
        txtNoKtp.text = dto.noKtp
        txtEmail.text = dto.email
        txtAlamat.text = dto.alamat
        txtNoTelp.text = dto.noTelpon
        txtInstagram.text = dto.instagramNama
        txtFacebook.text = dto.facebookNama

        val localDate = Instant.ofEpochMilli(dto.tglLahir)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        dpTglLahir.value = localDate
        cboKategoriUsaha.selectionModel.select(dto.dataKategori)
        chkStatus.isSelected = dto.status

        btnSimpan.text = "Ubah"
    }
    fun getUmkmTerpilih(): DataUmkmDTO? {
        return tblUmkm.selectionModel.selectedItem
    }
    fun getKategoriTerpilih(): DataKategoriDTO {
        return cboKategoriUsaha.selectionModel.selectedItem
    }
}