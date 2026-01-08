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
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.util.Vector
import org.bukkit.entity.ArmorStand
import com.voltaccept.spideranimation.utilities.events.addEventListener
import org.bukkit.entity.LivingEntity

private fun raycastSpiderFromPlayer(player: org.bukkit.entity.Player, maxDistance: Double): Pair<SpiderBody, ECSEntity>? {
    val eyeLocation = player.eyeLocation
    val direction = eyeLocation.direction.normalize()
    
    // Get the world from the player's location (should not be null for an online player)
    val world = eyeLocation.world ?: return null
    
    // Check points along the ray
    val step = 0.2
    var currentDistance = 0.0
    
    while (currentDistance < maxDistance) {
        val checkLocation = eyeLocation.clone().add(direction.clone().multiply(currentDistance))
        
        // Check for armor stands (spider parts) in a small radius
        for (entity in world.getNearbyEntities(checkLocation, 0.5, 0.5, 0.5)) {
            if (entity is ArmorStand) {
                val spider = RenderEntityTracker.getSpider(entity)
                if (spider != null) {
                    // Find the ECS entity for this spider
                    val ecsEntity = AppState.ecs.query<ECSEntity, SpiderBody>()
                        .find { it.second === spider }?.first
                    
                    if (ecsEntity != null) {
                        return Pair(spider, ecsEntity)
                    }
                }
            }
        }
        
        currentDistance += step
    }
    
    return null
}

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

    // Add damage listener for attacks on spider from non-players
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
                    // Only handle non-player damage here (players use raycast)
                    if (damager !is org.bukkit.entity.Player) {
                        // Apply damage and make spider flee from the damager (if not the owner)
                        val oldHealth = spider.health
                        spider.damage(event.damage)
                        val ownerUUID = entity.query<com.voltaccept.spideranimation.PetSpiderOwner>()?.ownerUUID
                        if (damager is LivingEntity && damager.uniqueId != ownerUUID) {
                            entity.addComponent(FleeComponent(damager as LivingEntity, 100)) // flee for 5 seconds
                        }
                        if (spider.health < oldHealth) {
                            // Play hurt sound
                            spider.world?.playSound(spider.location(), org.bukkit.Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 1.0f)
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

    // Add raycast-based damage detection for player left clicks
    addEventListener(object : Listener {
        @EventHandler
        fun onPlayerLeftClick(event: PlayerInteractEvent) {
            // Only care about left clicks
            if (event.action != Action.LEFT_CLICK_AIR && event.action != Action.LEFT_CLICK_BLOCK) {
                return
            }
            
            val player = event.player
            val raycastResult = raycastSpiderFromPlayer(player, 4.0)
            
            if (raycastResult != null) {
                val (spider, entity) = raycastResult
                
                if (spider.isDisabled) {
                    return
                }
                
                // Apply 1 damage
                val oldHealth = spider.health
                spider.damage(1.0)
                
                if (spider.health < oldHealth) {
                    // Play hurt sound
                    spider.world?.playSound(spider.location(), org.bukkit.Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 1.0f)
                    
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