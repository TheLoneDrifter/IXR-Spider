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
                    val targetPos = ownerTarget.location.toVector()
                    val dir = targetPos.subtract(eyePos.toVector()).normalize()
                    val speed = 1.6 // same as snowball

                    // play shoot sound
                    spider.world.playSound(eyePos, "entity.arrow.shoot", 1.0f, 1.0f)

                    // create pellet visual at eye position
                    val pelletVisual = LaserPoint(spider.world, eyePos.toVector(), true)
                    val pelletEntity = app.spawn(pelletVisual)

                    // move pellet towards target
                    val pelletHandle = interval(0, 1) {
                        val currentPos = pelletEntity.query<LaserPoint>()?.position ?: return@interval
                        val distanceToTarget = currentPos.distance(targetPos)
                        if (distanceToTarget < 0.5) {
                            // hit: apply damage and remove pellet
                            pelletEntity.remove()
                            it.close()

                            // apply damage with custom type
                            try {
                                // Try to call NMS to apply a custom damage source named "ixr:spider_pellet".
                                // Reflection is used to avoid hardcoding server package versions.
                                try {
                                    val serverPackage = org.bukkit.Bukkit.getServer()::class.java.getPackage().name
                                    val craftLivingClass = Class.forName("$serverPackage.entity.CraftLivingEntity")
                                    val getHandle = craftLivingClass.getMethod("getHandle")
                                    val nmsTarget = getHandle.invoke(ownerTarget)

                                    val dmgClass = Class.forName("net.minecraft.world.damagesource.DamageSource")
                                    val dmgCtor = try {
                                        dmgClass.getConstructor(String::class.java)
                                    } catch (_: NoSuchMethodException) {
                                        null
                                    }

                                    val dmgInstance = if (dmgCtor != null) {
                                        dmgCtor.newInstance("ixr:spider_pellet")
                                    } else {
                                        // fallback: try a static factory method if constructor not available
                                        val ofMethod = dmgClass.methods.firstOrNull { m -> m.parameterCount == 1 && m.parameterTypes[0] == String::class.java }
                                        if (ofMethod != null) ofMethod.invoke(null, "ixr:spider_pellet") else null
                                    }

                                    if (dmgInstance != null) {
                                        // call hurt(DamageSource, float)
                                        val hurtMethod = nmsTarget.javaClass.methods.firstOrNull { m ->
                                            m.name == "hurt" && m.parameterCount == 2
                                        }
                                        if (hurtMethod != null) {
                                            hurtMethod.invoke(nmsTarget, dmgInstance, damagePerTick.toFloat())
                                        } else {
                                            // last resort: use Bukkit API
                                            ownerTarget.damage(damagePerTick)
                                        }
                                    } else {
                                        ownerTarget.damage(damagePerTick)
                                    }
                                } catch (e: Exception) {
                                    // reflection failed for some reason; fall back to Bukkit damage
                                    ownerTarget.damage(damagePerTick)
                                }
                            } catch (e: Exception) {
                                // final safety: ignore any unexpected errors
                            }
                            return@interval
                        }

                        // move towards target
                        val moveVec = dir.clone().multiply(speed / 20.0) // per tick movement
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
