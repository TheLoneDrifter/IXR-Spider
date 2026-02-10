package com.voltaccept.spideranimation

import com.voltaccept.spideranimation.spider.presets.AnimatedPalettes
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
        return playerSettings.getOrPut(player.uniqueId) { SpiderSettings() }
    }
    
    fun setLegCount(player: Player, legCount: Int) {
        val settings = getSettings(player)
        settings.legCount = legCount
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
    }
    
    fun setConcreteColor(player: Player, concreteColor: ConcreteColor) {
        val settings = getSettings(player)
        settings.concreteColor = concreteColor
    }
    
    fun updateLastOfflineTime(player: Player) {
        val settings = getSettings(player)
        settings.lastOfflineTime = System.currentTimeMillis()
    }
    
    fun getOfflineHours(player: Player): Long {
        val settings = getSettings(player)
        val offlineMs = System.currentTimeMillis() - settings.lastOfflineTime
        return offlineMs / (60 * 60 * 1000) // Convert milliseconds to hours
    }
    
    fun saveSpiderFuel(player: Player, fuel: Int) {
        val settings = getSettings(player)
        settings.currentFuel = fuel.coerceIn(0, 100)
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
    
    fun clearSettings(player: Player) {
        playerSettings.remove(player.uniqueId)
    }
    
    fun cleanup() {
        playerSettings.clear()
    }
}
