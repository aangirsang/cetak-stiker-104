package com.girsang.server

import javafx.application.Application
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.io.File

@EnableScheduling
@SpringBootApplication
class SpringApp

fun main(args: Array<String>) {
    val dataDir = File("./data")
    if (!dataDir.exists()) {
        println("       Membuat folder data di: ${dataDir.absolutePath}")
        dataDir.mkdirs()
    }

    Application.launch(ServerUI::class.java, *args)
}