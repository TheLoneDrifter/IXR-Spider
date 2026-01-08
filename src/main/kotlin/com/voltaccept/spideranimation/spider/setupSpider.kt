package com.voltaccept.spideranimation.spider

import com.voltaccept.spideranimation.AppState
import com.voltaccept.spideranimation.spider.components.FleeComponent
import com.voltaccept.spideranimation.spider.components.body.setupSpiderBody
import com.voltaccept.spideranimation.spider.components.setupBehaviours
import com.voltaccept.spideranimation.spider.components.SpiderBehaviour
import com.voltaccept.spideranimation.spider.components.StayStillBehaviour
import com.voltaccept.spideranimation.spider.components.setupCloak
import com.voltaccept.spideranimation.spider.components.setupMountable
import com.voltaccept.spideranimation.spider.components.setupPointDetector
import com.voltaccept.spideranimation.spider.components.setupSoundAndParticles
import com.voltaccept.spideranimation.spider.components.setupTridentHitDetector
import com.voltaccept.spideranimation.spider.components.body.SpiderBody
import com.voltaccept.spideranimation.spider.components.rendering.setupRenderer
import com.voltaccept.spideranimation.utilities.ecs.ECS
import com.voltaccept.spideranimation.utilities.ecs.ECSEntity
import com.voltaccept.spideranimation.utilities.rendering.RenderEntityTracker
import com.voltaccept.spideranimation.utilities.events.onInteractEntity
import org.bukkit.Material
import org.bukkit.GameMode
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import com.voltaccept.spideranimation.utilities.events.addEventListener
import org.bukkit.entity.LivingEntity

fun setupSpider(app: ECS) {
    setupSpiderBody(app)
    setupBehaviours(app)

    app.onTick {
        for ((entity, spider) in app.query<ECSEntity, SpiderBody>()) {
            if (spider.isDisabled && System.currentTimeMillis() >= spider.disabledUntil) {
                spider.isDisabled = false
                spider.health = spider.maxHealth // Reset health when reactivated
                val ownerComponent = entity.query<com.voltaccept.spideranimation.PetSpiderOwner>()
                if (ownerComponent != null) {
                    val owner = org.bukkit.Bukkit.getPlayer(ownerComponent.ownerUUID)
                    owner?.sendMessage("§aYour SP1D.3R has been reactivated!")
                }
            }
            
            if (!spider.isDisabled) {
                entity.replaceComponent<SpiderBehaviour>(StayStillBehaviour())
            }
        }
    }

    setupCloak(app)
    setupMountable(app)
    setupPointDetector(app)
    setupSoundAndParticles(app)
    setupTridentHitDetector(app)
    setupRenderer(app)
    setupFeeding(app)

    // Add damage listener for attacks on spider
    addEventListener(object : Listener {
        @EventHandler
        fun onDamage(event: EntityDamageByEntityEvent) {
            val damaged = event.entity
            val damager = event.damager
            val spider = RenderEntityTracker.getSpider(damaged)
            if (spider != null) {
                if (spider.isDisabled) {
                    event.isCancelled = true
                    return
                }
                
                // Find the ECS entity
                val entity = AppState.ecs.query<ECSEntity, SpiderBody>().find { it.second === spider }?.first
                if (entity != null) {
                    if (damager is org.bukkit.entity.Player) {
                        // Players can damage the spider
                        val damageAmount = if (event.damage > 1.0) event.damage else 1.0 // at least 1 damage
                        val oldHealth = spider.health
                        spider.damage(damageAmount)
                        if (spider.health < oldHealth) {
                            // Play hurt sound
                            spider.world.playSound(spider.location(), org.bukkit.Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 1.0f)
                        }
                        
                        if (spider.health <= 0 && !spider.isDisabled) {
                            spider.isDisabled = true
                            spider.disabledUntil = System.currentTimeMillis() + (3 * 60 * 1000) // 3 minutes
                            val ownerComponent = entity.query<com.voltaccept.spideranimation.PetSpiderOwner>()
                            if (ownerComponent != null) {
                                val owner = org.bukkit.Bukkit.getPlayer(ownerComponent.ownerUUID)
                                owner?.sendMessage("§cYour SP1D.3R has been disabled due to internal damage")
                            }
                        }
                    } else {
                        // Apply damage and make spider flee from the damager (if not the owner)
                        val oldHealth = spider.health
                        spider.damage(event.damage)
                        val ownerUUID = entity.query<com.voltaccept.spideranimation.PetSpiderOwner>()?.ownerUUID
                        if (damager is LivingEntity && damager.uniqueId != ownerUUID) {
                            entity.addComponent(FleeComponent(damager as LivingEntity, 100)) // flee for 5 seconds
                        }
                        if (spider.health < oldHealth) {
                            // Play hurt sound
                            spider.world.playSound(spider.location(), org.bukkit.Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 1.0f)
                        }
                        
                        if (spider.health <= 0 && !spider.isDisabled) {
                            spider.isDisabled = true
                            spider.disabledUntil = System.currentTimeMillis() + (3 * 60 * 1000) // 3 minutes
                            val ownerComponent = entity.query<com.voltaccept.spideranimation.PetSpiderOwner>()
                            if (ownerComponent != null) {
                                val owner = org.bukkit.Bukkit.getPlayer(ownerComponent.ownerUUID)
                                owner?.sendMessage("§cYour SP1D.3R has been disabled due to internal damage")
                            }
                        }
                    }
                }
            }
        }
    })
}

fun setupFeeding(app: ECS) {
    // Owner can right-click their spider's rendered entity with redstone to heal it.
    onInteractEntity { event ->
        val player = event.player
        val hand = event.hand
        if (hand != EquipmentSlot.HAND) return@onInteractEntity
        val clicked = event.rightClicked

        // Find which spider (if any) this entity belongs to via RenderEntityTracker
        val spider = RenderEntityTracker.getSpider(clicked) ?: return@onInteractEntity

        // find the ECS entity that owns this SpiderBody
        val ownerEntry = app.query<ECSEntity, SpiderBody>().firstOrNull { it.second === spider } ?: return@onInteractEntity
        val ownerEntity = ownerEntry.first
        val ownerComponent = ownerEntity.query<com.voltaccept.spideranimation.PetSpiderOwner>() ?: return@onInteractEntity

        // ensure only owner can feed
        if (player.uniqueId != ownerComponent.ownerUUID) return@onInteractEntity

        val item = player.inventory.itemInMainHand
        if (item.type != Material.REDSTONE) return@onInteractEntity

        // heal amount per redstone
        val healAmount = 4.0
        val before = spider.health
        spider.heal(healAmount)
        val healed = spider.health - before

        if (healed > 0.0) {
            // consume one redstone
            val amt = item.amount - 1
            if (amt <= 0) player.inventory.setItemInMainHand(null) else item.amount = amt
            player.sendMessage("§aYou fed your SP1D.3R and healed ${healed} health.")
            player.world.playSound(player.location, org.bukkit.Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f)
        } else {
            player.sendMessage("§7Your SP1D.3R is already at full health.")
        }
    }
}