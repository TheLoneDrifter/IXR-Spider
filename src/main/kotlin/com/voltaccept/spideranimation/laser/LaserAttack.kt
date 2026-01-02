package com.voltaccept.spideranimation.laser

import com.voltaccept.spideranimation.PetSpiderOwner
import com.voltaccept.spideranimation.spider.components.body.SpiderBody
import com.voltaccept.spideranimation.utilities.ecs.ECS
import com.voltaccept.spideranimation.utilities.ecs.ECSEntity
import com.voltaccept.spideranimation.utilities.events.interval
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import java.util.UUID
import com.voltaccept.spideranimation.utilities.events.addEventListener
import org.bukkit.entity.Projectile
import org.bukkit.util.Vector
import org.bukkit.entity.BlockDisplay
import org.bukkit.util.Transformation
import org.bukkit.Material
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class LaserAttack(var target: LivingEntity, var intervalHandle: java.io.Closeable?, var laserEntity: ECSEntity?)

fun setupLaserAttacks(app: ECS) {
    val attackRange = 16.0
    val damagePerTick = 5.0 // stone sword base damage

    // map owner UUID -> target entity
    val ownerTargets = mutableMapOf<UUID, LivingEntity>()

    // register listeners to track what entity a player last damaged
    addEventListener(object : Listener {
        @EventHandler
        fun onDamage(event: EntityDamageByEntityEvent) {
            val damager = event.damager
            val victim = event.entity
            // if a player damaged an entity, record that as their target
            if (damager is org.bukkit.entity.Player && victim is LivingEntity) {
                ownerTargets[damager.uniqueId] = victim
                return
            }

            // if an entity (or projectile shot by an entity) damaged a player, record attacker as player's target
            if (victim is org.bukkit.entity.Player) {
                // resolve shooter if projectile
                val actualDamager = when (damager) {
                    is Projectile -> (damager.shooter as? LivingEntity)
                    is LivingEntity -> damager
                    else -> null
                }
                if (actualDamager != null) {
                    ownerTargets[victim.uniqueId] = actualDamager
                }
            }
        }

        @EventHandler
        fun onDeath(event: EntityDeathEvent) {
            val dead = event.entity
            // remove any mappings pointing to this dead entity
            ownerTargets.entries.removeIf { !it.value.isValid || it.value == dead }
        }
    })

    app.onTick {
        // start attacks for spiders without an attacker component
        for ((entity, spider) in app.query<ECSEntity, SpiderBody>()) {
            // only consider spiders that are owned pets
            val owner = entity.query<PetSpiderOwner>() ?: continue

            // skip if already attacking
            if (entity.query<LaserAttack>() != null) continue

            // only attack if the owner is actively targeting this entity
            val ownerUUID = owner.ownerUUID
            val ownerTarget = ownerTargets[ownerUUID]
            if (ownerTarget == null || !ownerTarget.isValid || ownerTarget.location.distanceSquared(spider.location()) > attackRange * attackRange) continue

            // start attacking this target by firing invisible redstone pellets
            lateinit var handle: java.io.Closeable
            handle = interval(0, 20) {
                // if target is dead or invalid or out of range, stop
                if (!ownerTarget.isValid || ownerTarget.health <= 0.0 || ownerTarget.location.distanceSquared(spider.location()) > attackRange * attackRange) {
                    entity.removeComponent<LaserAttack>()
                    handle.close()
                    return@interval
                }

                // spawn invisible pellet: create a moving redstone-block visual towards target
                try {
                    val eyePos = spider.location().add(0.0, spider.gait.stationary.bodyHeight, 0.0)
                    val targetPos = ownerTarget.location.toVector().add(org.bukkit.util.Vector(0.0, ownerTarget.height / 2.0, 0.0))
                    val dir = targetPos.subtract(eyePos.toVector()).normalize()
                    val speed = 4.0 // faster speed for better animation

                    // play shoot sound
                    spider.world.playSound(eyePos, "entity.arrow.shoot", 1.0f, 1.0f)

                    // spawn block display that moves towards the target
                    val bullet = spider.world.spawn<BlockDisplay>(eyePos, BlockDisplay::class.java) { bd ->
                        bd.block = org.bukkit.Material.REDSTONE_BLOCK.createBlockData()
                        bd.transformation = org.bukkit.util.Transformation(
                            org.joml.Vector3f(),
                            org.joml.Quaternionf(),
                            org.joml.Vector3f(0.125f, 0.125f, 0.125f),
                            org.joml.Quaternionf()
                        )
                    }

                    // monitor the bullet, move it towards the target, and apply damage when close
                    var tickCount = 0
                    val pelletHandle = interval(0, 1) {
                        tickCount++
                        if (!bullet.isValid) {
                            it.close()
                            return@interval
                        }
                        // move towards the target at 4.0 blocks per second
                        val currentPos = bullet.location.toVector()
                        val targetPos = ownerTarget.location.toVector().add(org.bukkit.util.Vector(0.0, ownerTarget.height / 2.0, 0.0))
                        val direction = targetPos.subtract(currentPos).normalize()
                        val speed = 4.0 / 20.0 // 4.0 blocks per second
                        val newPos = currentPos.add(direction.multiply(speed))
                        bullet.teleport(newPos.toLocation(bullet.world))
                        // check for entities within 1.5 blocks to damage
                        val nearbyEntities = bullet.world.getNearbyEntities(bullet.location, 1.5, 1.5, 1.5)
                        val validTargets = nearbyEntities.filterIsInstance<LivingEntity>().filter { entity ->
                            !(entity is org.bukkit.entity.Player) && entity.uniqueId != owner.ownerUUID && !entity.isDead
                        }
                        if (validTargets.isNotEmpty()) {
                            // hit: apply damage to all nearby entities and remove
                            bullet.remove()
                            it.close()

                            try {
                                val actualOwner = org.bukkit.Bukkit.getServer().getPlayer(owner.ownerUUID)
                                for (target in validTargets) {
                                    val damage = if (target == ownerTarget) 5.0 else 1.5
                                    target.damage(damage, actualOwner ?: bullet)
                                    // If the target is a mob, make it attack the owner
                                    if (target is org.bukkit.entity.Mob && actualOwner != null) {
                                        target.setTarget(actualOwner)
                                    }
                                }
                            } catch (e: Exception) {
                                // ignore
                            }
                            return@interval
                        }
                        // remove after 5 seconds if not hit
                        if (tickCount > 100) { // 5 seconds at 20 ticks/sec
                            bullet.remove()
                            it.close()
                        }
                    }
                    
                } catch (e: Exception) {
                    // ignore spawn errors
                }
            }

            entity.addComponent(LaserAttack(ownerTarget, handle, null))
        }
    }
}
