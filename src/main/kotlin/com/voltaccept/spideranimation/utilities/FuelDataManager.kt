package com.voltaccept.spideranimation.utilities

import com.voltaccept.spideranimation.ConcreteColor
import com.voltaccept.spideranimation.SpiderSettings
import com.voltaccept.spideranimation.spider.presets.AnimatedPalettes
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.UUID

object FuelDataManager {
    private val dataFolder: File by lazy {
        val pluginFolder = Bukkit.getPluginManager().getPlugin("SpiderAnimation")?.dataFolder
        val folder = if (pluginFolder != null) File(pluginFolder, "player-fuel") else File("player-fuel")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        folder
    }
    
    /**
     * Load all settings for a player from YAML file
     */
    fun loadPlayerSettings(player: Player): SpiderSettings? {
        val settingsFile = File(dataFolder, "${player.uniqueId}.yml")
        
        if (!settingsFile.exists()) {
            return null // Return null to use defaults
        }
        
        return try {
            val config = YamlConfiguration.loadConfiguration(settingsFile)
            val settings = SpiderSettings()
            
            // Load fuel
            settings.currentFuel = config.getInt("fuel", 100).coerceIn(0, 100)
            
            // Load spider settings if they exist
            settings.legCount = config.getInt("spider_settings.leg_count", 6)
            settings.concreteColor = try {
                ConcreteColor.valueOf(config.getString("spider_settings.concrete_color", "BLACK") ?: "BLACK")
            } catch (e: IllegalArgumentException) {
                ConcreteColor.BLACK
            }
            settings.eyeColor = try {
                AnimatedPalettes.valueOf(config.getString("spider_settings.eye_color", "LIME_EYES") ?: "LIME_EYES")
            } catch (e: IllegalArgumentException) {
                AnimatedPalettes.LIME_EYES
            }
            settings.blinkingColor = try {
                AnimatedPalettes.valueOf(config.getString("spider_settings.blinking_color", "LIME_BLINKING_LIGHTS") ?: "LIME_BLINKING_LIGHTS")
            } catch (e: IllegalArgumentException) {
                AnimatedPalettes.LIME_BLINKING_LIGHTS
            }
            
            settings.lastOfflineTime = config.getLong("spider_settings.last_offline_time", System.currentTimeMillis())
            
            settings
        } catch (e: Exception) {
            Bukkit.getLogger().warning("Failed to load settings data for ${player.name}: ${e.message}")
            null
        }
    }
    
    /**
     * Save all settings for a player to YAML file
     */
    fun savePlayerSettings(player: Player, settings: SpiderSettings) {
        val settingsFile = File(dataFolder, "${player.uniqueId}.yml")
        
        try {
            val config = YamlConfiguration()
            
            // Save basic player info
            config.set("player_name", player.name)
            config.set("last_updated", System.currentTimeMillis())
            
            // Save fuel
            config.set("fuel", settings.currentFuel.coerceIn(0, 100))
            
            // Save spider settings
            config.set("spider_settings.leg_count", settings.legCount)
            config.set("spider_settings.concrete_color", settings.concreteColor.name)
            config.set("spider_settings.eye_color", settings.eyeColor.name)
            config.set("spider_settings.blinking_color", settings.blinkingColor.name)
            config.set("spider_settings.last_offline_time", settings.lastOfflineTime)
            
            config.save(settingsFile)
        } catch (e: Exception) {
            Bukkit.getLogger().warning("Failed to save settings data for ${player.name}: ${e.message}")
        }
    }
    
    /**
     * Load settings from a specific file (for offline players)
     */
    fun loadPlayerSettingsFromFile(settingsFile: File): SpiderSettings? {
        if (!settingsFile.exists()) {
            return null
        }
        
        return try {
            val config = YamlConfiguration.loadConfiguration(settingsFile)
            val settings = SpiderSettings()
            
            // Load fuel
            settings.currentFuel = config.getInt("fuel", 100).coerceIn(0, 100)
            
            // Load spider settings if they exist
            settings.legCount = config.getInt("spider_settings.leg_count", 6)
            settings.concreteColor = try {
                ConcreteColor.valueOf(config.getString("spider_settings.concrete_color", "BLACK") ?: "BLACK")
            } catch (e: IllegalArgumentException) {
                ConcreteColor.BLACK
            }
            settings.eyeColor = try {
                AnimatedPalettes.valueOf(config.getString("spider_settings.eye_color", "LIME_EYES") ?: "LIME_EYES")
            } catch (e: IllegalArgumentException) {
                AnimatedPalettes.LIME_EYES
            }
            settings.blinkingColor = try {
                AnimatedPalettes.valueOf(config.getString("spider_settings.blinking_color", "LIME_BLINKING_LIGHTS") ?: "LIME_BLINKING_LIGHTS")
            } catch (e: IllegalArgumentException) {
                AnimatedPalettes.LIME_BLINKING_LIGHTS
            }
            
            settings.lastOfflineTime = config.getLong("spider_settings.last_offline_time", System.currentTimeMillis())
            
            settings
        } catch (e: Exception) {
            Bukkit.getLogger().warning("Failed to load settings from file ${settingsFile.name}: ${e.message}")
            null
        }
    }
    
    /**
     * Get the settings file path for a player (for debugging)
     */
    fun getSettingsFilePath(player: Player): String {
        return File(dataFolder, "${player.uniqueId}.yml").absolutePath
    }
    
    // Legacy fuel methods for backward compatibility
    fun loadPlayerFuel(player: Player): Int {
        val settings = loadPlayerSettings(player)
        return settings?.currentFuel ?: 100
    }
    
    fun savePlayerFuel(player: Player, fuel: Int) {
        val settings = loadPlayerSettings(player) ?: SpiderSettings()
        settings.currentFuel = fuel.coerceIn(0, 100)
        savePlayerSettings(player, settings)
    }
}
