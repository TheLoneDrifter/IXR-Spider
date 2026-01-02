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

            // find nearest living target in world within range excluding owner
            val candidates = spider.world.entities.filterIsInstance<LivingEntity>()
                .filter { it != owner && it != null && it.isValid }
                .filter { it.location.world == spider.world }
                .mapNotNull { it as? LivingEntity }
                .filter { it.location.distanceSquared(spider.location()) <= attackRange * attackRange }

            val nearest = candidates.minByOrNull { it.location.distanceSquared(spider.location()) } ?: continue

            // only attack if the owner is actively targeting this entity
            val ownerUUID = owner.ownerUUID
            val ownerTarget = ownerTargets[ownerUUID]
            if (ownerTarget == null || !ownerTarget.isValid) continue
            if (ownerTarget != nearest) continue

            // start attacking this target by firing redstone pellets (snowballs)
            lateinit var handle: java.io.Closeable
            handle = interval(0, 20) {
                // if target is dead or invalid or out of range, stop
                if (!nearest.isValid || nearest.health <= 0.0 || nearest.location.distanceSquared(spider.location()) > attackRange * attackRange) {
                    entity.removeComponent<LaserAttack>()
                    handle.close()
                    return@interval
                }

                // spawn a snowball pellet from spider eye towards target for visuals
                try {
                    val eyePos = spider.location().add(0.0, spider.gait.stationary.bodyHeight, 0.0)
                    val snowball = spider.world.spawn(eyePos, Snowball::class.java) { sb ->
                        val dir = nearest.location.toVector().subtract(eyePos.toVector()).normalize()
                        sb.velocity = dir.multiply(1.6)
                    }

                    // create a small redstone-block visual for the pellet using LaserPoint
                    val pelletVisual = LaserPoint(spider.world, snowball.location.toVector(), true)
                    val pelletEntity = app.spawn(pelletVisual)

                    // update visual every tick until snowball is gone
                    val pelletHandle = interval(0, 1) {
                        if (!snowball.isValid) {
                            pelletEntity.remove()
                            it.close()
                            return@interval
                        }
                        pelletEntity.query<LaserPoint>()?.position = snowball.location.toVector()
                    }
                    
                } catch (e: Exception) {
                    // ignore spawn errors
                }

                // apply damage immediately (pellet represents an accurate hit)
                try {
                    // Try to call NMS to apply a custom damage source named "ixr:spider_pellet".
                    // Reflection is used to avoid hardcoding server package versions.
                    try {
                        val serverPackage = org.bukkit.Bukkit.getServer()::class.java.getPackage().name
                        val craftLivingClass = Class.forName("$serverPackage.entity.CraftLivingEntity")
                        val getHandle = craftLivingClass.getMethod("getHandle")
                        val nmsNearest = getHandle.invoke(nearest)

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
                            val hurtMethod = nmsNearest.javaClass.methods.firstOrNull { m ->
                                m.name == "hurt" && m.parameterCount == 2
                            }
                            if (hurtMethod != null) {
                                hurtMethod.invoke(nmsNearest, dmgInstance, damagePerTick.toFloat())
                            } else {
                                // last resort: use Bukkit API
                                nearest.damage(damagePerTick)
                            }
                        } else {
                            nearest.damage(damagePerTick)
                        }
                    } catch (e: Exception) {
                        // reflection failed for some reason; fall back to Bukkit damage
                        nearest.damage(damagePerTick)
                    }
                } catch (e: Exception) {
                    // final safety: ignore any unexpected errors
                }
            }

            entity.addComponent(LaserAttack(nearest, handle, null))
        }
    }
}
