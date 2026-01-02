package com.voltaccept.spideranimation.laser

import com.voltaccept.spideranimation.PetSpiderOwner
import com.voltaccept.spideranimation.spider.components.body.SpiderBody
import com.voltaccept.spideranimation.utilities.ecs.ECS
import com.voltaccept.spideranimation.utilities.ecs.ECSEntity
import com.voltaccept.spideranimation.utilities.events.interval
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector
import org.bukkit.entity.Snowball
import org.bukkit.Location

class LaserAttack(var target: LivingEntity, var intervalHandle: java.io.Closeable?, var laserEntity: ECSEntity?)

fun setupLaserAttacks(app: ECS) {
    val attackRange = 16.0
    val damagePerTick = 4.0 // wooden sword base damage

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
                        sb.isCritical = false
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
                    nearest.damage(damagePerTick)
                } catch (e: Exception) {
                    // ignore
                }
            }

            entity.addComponent(LaserAttack(nearest, handle, null))
        }
    }
}
