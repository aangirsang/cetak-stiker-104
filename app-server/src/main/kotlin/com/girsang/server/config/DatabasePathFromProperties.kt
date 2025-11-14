package com.girsang.server.config

import java.util.Properties

object DatabasePathFromProperties {
    fun getDatabasePathFromProperties(): String {
        val props = Properties()

        // Cari file application.properties dari classpath (resources)
        val inputStream = this::class.java.classLoader.getResourceAsStream("application.properties")

        if (inputStream == null) {
            println("       File application.properties tidak ditemukan di resources.")
            return "./data/cetak-stiker.db" // fallback default
        }

        props.load(inputStream)

        val url = props.getProperty("spring.datasource.url") ?: "jdbc:sqlite:./data/cetak-stiker.db"
        val dbPath = url.substringAfter("jdbc:sqlite:").trim()

        return dbPath
    }
}
