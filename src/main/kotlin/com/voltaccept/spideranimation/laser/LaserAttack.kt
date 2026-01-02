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
import org.bukkit.entity.Snowball
import org.bukkit.Location

class LaserAttack(var target: LivingEntity, var intervalHandle: java.io.Closeable?, var laserEntity: ECSEntity?)

fun setupLaserAttacks(app: ECS) {
    val attackRange = 16.0
    val damagePerTick = 4.0 // wooden sword base damage

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

                    // spawn invisible snowball for projectile behavior
                    val snowball = spider.world.spawn(eyePos, Snowball::class.java) { sb ->
                        sb.velocity = dir.multiply(speed)
                        sb.setVisible(false) // make invisible
                    }

                    // create pellet visual at eye position
                    val pelletVisual = LaserPoint(spider.world, eyePos.toVector(), true)
                    val pelletEntity = app.spawn(pelletVisual)

                    // move pellet towards target, updating direction each tick for homing
                    val pelletHandle = interval(0, 1) {
                        if (!snowball.isValid) {
                            pelletEntity.remove()
                            it.close()
                            return@interval
                        }
                        val currentPos = pelletEntity.query<LaserPoint>()?.position ?: return@interval
                        val updatedTargetPos = ownerTarget.location.toVector().add(org.bukkit.util.Vector(0.0, ownerTarget.height / 2.0, 0.0))
                        val distanceToTarget = currentPos.distance(updatedTargetPos)
                        if (distanceToTarget < 0.5) {
                            // hit: apply damage and remove pellet
                            pelletEntity.remove()
                            snowball.remove()
                            it.close()

                            try {
                                val actualOwner = org.bukkit.Bukkit.getServer().getPlayer(owner.ownerUUID)
                                if (actualOwner != null) {
                                    ownerTarget.damage(damagePerTick, actualOwner)
                                } else {
                                    ownerTarget.damage(damagePerTick)
                                }
                            } catch (e: Exception) {
                                // ignore
                            }
                            return@interval
                        }

                        // update direction towards moving target
                        val updatedDir = updatedTargetPos.subtract(currentPos).normalize()
                        val moveVec = updatedDir.multiply(speed)
                        pelletEntity.query<LaserPoint>()?.position?.add(moveVec)
                    }
                    
                } catch (e: Exception) {
                    // ignore spawn errors
                }
            }

            entity.addComponent(LaserAttack(ownerTarget, handle, null))
        }
    }
}
