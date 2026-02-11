package com.voltaccept.spideranimation.utilities

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
     * Load fuel data for a player from YAML file
     */
    fun loadPlayerFuel(player: Player): Int {
        val fuelFile = File(dataFolder, "${player.uniqueId}.yml")
        
        if (!fuelFile.exists()) {
            return 100 // Default max fuel
        }
        
        return try {
            val config = YamlConfiguration.loadConfiguration(fuelFile)
            config.getInt("fuel", 100).coerceIn(0, 100)
        } catch (e: Exception) {
            Bukkit.getLogger().warning("Failed to load fuel data for ${player.name}: ${e.message}")
            100
        }
    }
    
    /**
     * Save fuel data for a player to YAML file
     */
    fun savePlayerFuel(player: Player, fuel: Int) {
        val fuelFile = File(dataFolder, "${player.uniqueId}.yml")
        
        try {
            val config = YamlConfiguration()
            config.set("player_name", player.name)
            config.set("fuel", fuel.coerceIn(0, 100))
            config.set("last_updated", System.currentTimeMillis())
            config.save(fuelFile)
        } catch (e: Exception) {
            Bukkit.getLogger().warning("Failed to save fuel data for ${player.name}: ${e.message}")
        }
    }
    
    /**
     * Get the fuel file path for a player (for debugging)
     */
    fun getFuelFilePath(player: Player): String {
        return File(dataFolder, "${player.uniqueId}.yml").absolutePath
    }
}
