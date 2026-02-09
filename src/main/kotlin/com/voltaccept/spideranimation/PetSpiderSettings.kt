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
    var blinkingColor: AnimatedPalettes = AnimatedPalettes.LIME_BLINKING_LIGHTS
)

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
    
    fun clearSettings(player: Player) {
        playerSettings.remove(player.uniqueId)
    }
    
    fun cleanup() {
        playerSettings.clear()
    }
}
