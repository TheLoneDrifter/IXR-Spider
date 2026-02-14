package com.voltaccept.spideranimation.web

import com.voltaccept.spideranimation.PetSpiderSettingsManager
import com.voltaccept.spideranimation.SpiderSettings
import com.voltaccept.spideranimation.utilities.FuelDataManager
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.static.*
import kotlinx.serialization.Serializable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

@Serializable
data class SpiderData(
    val ign: String,
    val uuid: String,
    val legCount: Int,
    val eyeColor: String,
    val concreteColor: String,
    val fuel: Int,
    val lastSeen: Long
)

@Serializable
data class PlayerSearchResponse(
    val found: Boolean,
    val data: SpiderData? = null,
    val error: String? = null
)

object SpiderWebServer {
    private var server: NettyApplicationEngine? = null
    
    fun start(port: Int) {
        server = embeddedServer(Netty, port = port, host = "0.0.0.0") {
            install(ContentNegotiation) {
                json()
            }
            
            routing {
                get("/spider") {
                    call.respondText(
                        this::class.java.classLoader.getResource("web/index.html")?.readText()
                            ?: "<h1>Spider Animation Web Viewer</h1><p>Frontend not found</p>",
                        contentType = io.ktor.http.ContentType.Text.Html
                    )
                }
                
                static("/spider/static") {
                    resources("web")
                }
                
                get("/api/spider/player/{ign}") {
                    val ign = call.parameters["ign"] ?: return@get call.respond(
                        PlayerSearchResponse(found = false, error = "IGN parameter required")
                    )
                    
                    try {
                        // Try to get online player first
                        val player = Bukkit.getPlayer(ign)
                        val spiderData = if (player != null) {
                            // Player is online, get current settings
                            val settings = PetSpiderSettingsManager.getSettings(player)
                            SpiderData(
                                ign = player.name,
                                uuid = player.uniqueId.toString(),
                                legCount = settings.legCount,
                                eyeColor = settings.eyeColor.name,
                                concreteColor = settings.concreteColor.name,
                                fuel = settings.currentFuel,
                                lastSeen = System.currentTimeMillis()
                            )
                        } else {
                            // Player is offline, try to load from saved data
                            val offlineData = loadOfflinePlayerData(ign)
                            offlineData ?: return@get call.respond(
                                PlayerSearchResponse(found = false, error = "Player not found")
                            )
                        }
                        
                        call.respond(PlayerSearchResponse(found = true, data = spiderData))
                    } catch (e: Exception) {
                        call.respond(PlayerSearchResponse(found = false, error = "Server error: ${e.message}"))
                    }
                }
                
                get("/api/spider/players") {
                    try {
                        val onlinePlayers = Bukkit.getOnlinePlayers().map { player ->
                            val settings = PetSpiderSettingsManager.getSettings(player)
                            SpiderData(
                                ign = player.name,
                                uuid = player.uniqueId.toString(),
                                legCount = settings.legCount,
                                eyeColor = settings.eyeColor.name,
                                concreteColor = settings.concreteColor.name,
                                fuel = settings.currentFuel,
                                lastSeen = System.currentTimeMillis()
                            )
                        }
                        call.respond(onlinePlayers)
                    } catch (e: Exception) {
                        call.respond(mapOf("error" to "Server error: ${e.message}"))
                    }
                }
            }
        }
        
        try {
            server?.start(wait = false)
        } catch (e: Exception) {
            // If port is already in use, try port + 1
            try {
                server = embeddedServer(Netty, port = port + 1, host = "0.0.0.0") {
                    install(ContentNegotiation) { json() }
                    routing {
                        get("/spider") {
                            call.respondText(
                                this::class.java.classLoader.getResource("web/index.html")?.readText()
                                    ?: "<h1>Spider Animation Web Viewer</h1><p>Frontend not found</p>",
                                contentType = io.ktor.http.ContentType.Text.Html
                            )
                        }
                        static("/spider/static") {
                            resources("web")
                        }
                        get("/api/spider/player/{ign}") {
                            val ign = call.parameters["ign"] ?: return@get call.respond(
                                PlayerSearchResponse(found = false, error = "IGN parameter required")
                            )
                            
                            try {
                                val player = Bukkit.getPlayer(ign)
                                val spiderData = if (player != null) {
                                    val settings = PetSpiderSettingsManager.getSettings(player)
                                    SpiderData(
                                        ign = player.name,
                                        uuid = player.uniqueId.toString(),
                                        legCount = settings.legCount,
                                        eyeColor = settings.eyeColor.name,
                                        concreteColor = settings.concreteColor.name,
                                        fuel = settings.currentFuel,
                                        lastSeen = System.currentTimeMillis()
                                    )
                                } else {
                                    val offlineData = loadOfflinePlayerData(ign)
                                    offlineData ?: return@get call.respond(
                                        PlayerSearchResponse(found = false, error = "Player not found")
                                    )
                                }
                                call.respond(PlayerSearchResponse(found = true, data = spiderData))
                            } catch (e: Exception) {
                                call.respond(PlayerSearchResponse(found = false, error = "Server error: ${e.message}"))
                            }
                        }
                        get("/api/spider/players") {
                            try {
                                val onlinePlayers = Bukkit.getOnlinePlayers().map { player ->
                                    val settings = PetSpiderSettingsManager.getSettings(player)
                                    SpiderData(
                                        ign = player.name,
                                        uuid = player.uniqueId.toString(),
                                        legCount = settings.legCount,
                                        eyeColor = settings.eyeColor.name,
                                        concreteColor = settings.concreteColor.name,
                                        fuel = settings.currentFuel,
                                        lastSeen = System.currentTimeMillis()
                                    )
                                }
                                call.respond(onlinePlayers)
                            } catch (e: Exception) {
                                call.respond(mapOf("error" to "Server error: ${e.message}"))
                            }
                        }
                    }
                }
                server?.start(wait = false)
            } catch (e2: Exception) {
                throw Exception("Failed to start web server on ports $port and ${port + 1}: ${e2.message}")
            }
        }
    }
    
    fun stop() {
        server?.stop(1000, 5000)
    }
    
    private fun loadOfflinePlayerData(ign: String): SpiderData? {
        return try {
            // Look for player data files in the plugin data folder
            val pluginDataFolder = File("plugins/SpiderAnimation/player-fuel")
            if (!pluginDataFolder.exists()) return null
            
            // First try to find by exact name match in YAML files
            val playerFiles = pluginDataFolder.listFiles { file -> 
                file.extension == "yml" || file.extension == "yaml" 
            } ?: return null
            
            for (file in playerFiles) {
                try {
                    val config = YamlConfiguration.loadConfiguration(file)
                    val playerName = config.getString("player_name")
                    if (playerName?.equals(ign, ignoreCase = true) == true) {
                        val settings = FuelDataManager.loadPlayerSettingsFromFile(file)
                        settings?.let {
                            return SpiderData(
                                ign = ign,
                                uuid = file.nameWithoutExtension,
                                legCount = it.legCount,
                                eyeColor = it.eyeColor.name,
                                concreteColor = it.concreteColor.name,
                                fuel = it.currentFuel,
                                lastSeen = it.lastOfflineTime
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Continue to next file if this one fails
                    continue
                }
            }
            
            null
        } catch (e: Exception) {
            null
        }
    }
}
