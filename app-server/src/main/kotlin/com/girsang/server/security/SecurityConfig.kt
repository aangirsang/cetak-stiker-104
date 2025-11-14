package com.girsang.server.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

@Configuration
class SecurityConfig {

    private val configPath = Paths.get("config", "server-config.properties")
    private val encoder = BCryptPasswordEncoder()

    private val properties: Properties = Properties().apply {
        ensureConfigFileExists()
        Files.newInputStream(configPath).use { load(it) }
    }

    private fun ensureConfigFileExists() {
        if (!Files.exists(configPath)) {
            Files.createDirectories(configPath.parent)

            val defaultProps = Properties().apply {
                setProperty("server.port", "8080")
                setProperty("app.security.user", "admin")
                setProperty("app.security.password", encoder.encode("secret"))
                setProperty("app.security.roles", "USER")
            }

            Files.newOutputStream(
                configPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            ).use {
                defaultProps.store(it, "⚙️ Default configuration created automatically")
            }

            println("       File konfigurasi dibuat: ${configPath.toAbsolutePath()}")
        }
    }

    @Bean
    fun passwordEncoder() = encoder

    @Bean
    fun userDetailsService(): UserDetailsService {
        val username = properties.getProperty("app.security.user", "admin")
        val password = properties.getProperty("app.security.password", encoder.encode("secret"))
        val roles = properties.getProperty("app.security.roles", "USER")

        val user = User.withUsername(username)
            .password(password)
            .roles(*roles.split(",").map { it.trim() }.toTypedArray())
            .build()

        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .headers { it.frameOptions { frame -> frame.sameOrigin() } }
            .authorizeHttpRequests {
                it.requestMatchers("/h2-console/**").permitAll()
                it.requestMatchers("/api/pengguna/ping").permitAll()
                it.requestMatchers("/api/**").authenticated()
                it.anyRequest().permitAll()
            }
            .httpBasic { }

        return http.build()
    }
}
