package com.voltaccept.spideranimation

import com.voltaccept.spideranimation.spider.presets.AnimatedPalettes
import com.voltaccept.spideranimation.utilities.FuelDataManager
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Stores player-specific spider settings
 */
data class SpiderSettings(
    var legCount: Int = 6,
    var eyeColor: AnimatedPalettes = AnimatedPalettes.LIME_EYES,
    var blinkingColor: AnimatedPalettes = AnimatedPalettes.LIME_BLINKING_LIGHTS,
    var concreteColor: ConcreteColor = ConcreteColor.BLACK,
    var lastOfflineTime: Long = System.currentTimeMillis(),
    var currentFuel: Int = 100  // Store spider fuel persistently
)

enum class ConcreteColor {
    BLACK,
    WHITE
}

/**
 * Manages player-specific spider settings
 */
object PetSpiderSettingsManager {
    private val playerSettings = mutableMapOf<UUID, SpiderSettings>()
    
    fun getSettings(player: Player): SpiderSettings {
        // Try to load from YAML first
        val savedSettings = FuelDataManager.loadPlayerSettings(player)
        if (savedSettings != null) {
            // Cache the loaded settings
            playerSettings[player.uniqueId] = savedSettings
            return savedSettings
        }
        
        // Fall back to in-memory defaults
        return playerSettings.getOrPut(player.uniqueId) { SpiderSettings() }
    }
    
    fun setLegCount(player: Player, legCount: Int) {
        val settings = getSettings(player)
        settings.legCount = legCount
        saveSettings(player, settings)
    }
    
    fun setEyeColor(player: Player, eyeColor: AnimatedPalettes) {
        val settings = getSettings(player)
        settings.eyeColor = eyeColor
        settings.blinkingColor = when (eyeColor) {
            AnimatedPalettes.CYAN_EYES -> AnimatedPalettes.CYAN_BLINKING_LIGHTS
            AnimatedPalettes.RED_EYES -> AnimatedPalettes.RED_BLINKING_LIGHTS
            AnimatedPalettes.LIME_EYES -> AnimatedPalettes.LIME_BLINKING_LIGHTS
            else -> AnimatedPalettes.LIME_BLINKING_LIGHTS
        }
        saveSettings(player, settings)
    }
    
    fun setConcreteColor(player: Player, concreteColor: ConcreteColor) {
        val settings = getSettings(player)
        settings.concreteColor = concreteColor
        saveSettings(player, settings)
    }
    
    fun updateLastOfflineTime(player: Player) {
        val settings = getSettings(player)
        settings.lastOfflineTime = System.currentTimeMillis()
        saveSettings(player, settings)
    }
    
    fun getOfflineHours(player: Player): Long {
        val settings = getSettings(player)
        val offlineMs = System.currentTimeMillis() - settings.lastOfflineTime
        return offlineMs / (60 * 60 * 1000) // Convert milliseconds to hours
    }
    
    fun saveSpiderFuel(player: Player, fuel: Int) {
        val settings = getSettings(player)
        settings.currentFuel = fuel.coerceIn(0, 100)
        saveSettings(player, settings)
    }
    
    fun getSpiderFuel(player: Player): Int {
        val settings = getSettings(player)
        return settings.currentFuel
    }
    
    fun consumeOfflineFuel(player: Player) {
        val offlineHours = getOfflineHours(player)
        if (offlineHours > 0) {
            val fuelConsumed = offlineHours.toInt()
            val currentFuel = getSpiderFuel(player)
            val newFuel = (currentFuel - fuelConsumed).coerceAtLeast(0)
            saveSpiderFuel(player, newFuel)
        }
    }
    
    fun resetSettings(player: Player) {
        val settings = getSettings(player)
        settings.legCount = 6
        settings.eyeColor = AnimatedPalettes.LIME_EYES
        settings.blinkingColor = AnimatedPalettes.LIME_BLINKING_LIGHTS
        settings.concreteColor = ConcreteColor.BLACK
        // Note: We don't reset fuel and lastOfflineTime as they should persist
        saveSettings(player, settings)
    }
    
    private fun saveSettings(player: Player, settings: SpiderSettings) {
        // Update cache
        playerSettings[player.uniqueId] = settings
        // Save to YAML
        FuelDataManager.savePlayerSettings(player, settings)
    }
    
    fun cleanup() {
        playerSettings.clear()
    }
}
