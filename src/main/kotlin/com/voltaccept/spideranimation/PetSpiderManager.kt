package com.voltaccept.spideranimation

import com.voltaccept.spideranimation.spider.components.body.SpiderBody
import com.voltaccept.spideranimation.laser.LaserAttack
import com.voltaccept.spideranimation.utilities.ecs.ECSEntity
import org.bukkit.entity.Player
import java.util.UUID

object PetSpiderManager {
    // Maps player UUID to their spider entity
    private val playerSpiders = mutableMapOf<UUID, ECSEntity>()
    
    fun hasSpider(player: Player): Boolean {
        val entity = playerSpiders[player.uniqueId]
        return entity != null && !entity.scheduledForRemoval
    }
    
    fun getSpider(player: Player): ECSEntity? {
        val entity = playerSpiders[player.uniqueId]
        if (entity != null && entity.scheduledForRemoval) {
            playerSpiders.remove(player.uniqueId)
            return null
        }
        return entity
    }
    
    fun setSpider(player: Player, entity: ECSEntity) {
        // Remove old spider if exists
        removeSpider(player)
        playerSpiders[player.uniqueId] = entity
    }
    
    fun removeSpider(player: Player) {
        val entity = playerSpiders.remove(player.uniqueId)
        if (entity != null) {
            val attack = entity.query<LaserAttack>()
            try {
                attack?.intervalHandle?.close()
            } catch (_: Exception) {}
            try {
                attack?.laserEntity?.remove()
            } catch (_: Exception) {}
            entity.remove()
        }
    }
    
    fun getOwner(entity: ECSEntity): UUID? {
        return playerSpiders.entries.find { it.value == entity }?.key
    }
    
    fun cleanup() {
        playerSpiders.values.forEach { it.remove() }
        playerSpiders.clear()
    }
}

