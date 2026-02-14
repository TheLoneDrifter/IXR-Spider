package com.voltaccept.spideranimation.spider.components

import com.voltaccept.spideranimation.spider.components.body.SpiderBody
import com.voltaccept.spideranimation.utilities.*
import com.voltaccept.spideranimation.utilities.ecs.ECS
import com.voltaccept.spideranimation.utilities.ecs.ECSEntity
import com.voltaccept.spideranimation.utilities.events.addEventListener
import com.voltaccept.spideranimation.utilities.events.onInteractEntity
import com.voltaccept.spideranimation.utilities.maths.rotate
import com.voltaccept.spideranimation.utilities.overloads.direction
import com.voltaccept.spideranimation.utilities.overloads.playSound
import com.voltaccept.spideranimation.utilities.overloads.position
import com.voltaccept.spideranimation.utilities.overloads.yawRadians
import com.voltaccept.spideranimation.utilities.rendering.RenderEntity
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Pig
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.vehicle.VehicleEnterEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import org.joml.Quaternionf

class Mountable {
    var currentMarker: ArmorStand? = null
    var currentPig: Pig? = null
    fun getRider() = currentMarker?.passengers?.firstOrNull() as? Player
}

fun setupMountable(app: ECS) {
    // Mountable functionality disabled - spiders are now pet-only
    // Keeping basic interaction for potential future features
    onInteractEntity { player, entity, hand ->
        for (mountable in app.query<Mountable>()) {
            val currentPig = mountable.currentPig ?: continue
            if (entity != currentPig) continue
            if (hand != EquipmentSlot.HAND) continue
            // No interaction functionality - pet only
        }
    }

    // Mounting events disabled - spiders are pets only

    // Render pig and marker
    app.onRender {
        for ((spider, mountable) in app.query<SpiderBody, Mountable>()) {
            val location = spider.location().add(spider.velocity)

            val pigLocation = location.clone().add(Vector(.0, -.6, .0))
            val markerLocation = location.clone().add(Vector(.0, .3, .0))

            RenderEntity(
                clazz = Pig::class.java,
                location = pigLocation,
                init = {
                    it.setGravity(false)
                    it.setAI(false)
                    it.isInvisible = true
                    it.isInvulnerable = true
                    it.isSilent = true
                    it.isCollidable = false
                },
                update = {
                    mountable.currentPig = it
                }
            ).submit(spider to "mountable.pig")

            RenderEntity(
                clazz = ArmorStand::class.java,
                location = markerLocation,
                init = {
                    it.setGravity(false)
                    it.isInvisible = true
                    it.isInvulnerable = true
                    it.isSilent = true
                    it.isCollidable = false
                    it.isMarker = true
                },
                update = update@{
                    mountable.currentMarker = it
                    if (mountable.getRider() == null) return@update

                    // This is the only way to preserve passengers when teleporting.
                    // Paper has a TeleportFlag, but it is not supported by Spigot.
                    // https://jd.papermc.io/paper/1.21/io/papermc/paper/entity/TeleportFlag.EntityState.html
                    runCommandSilently("execute as ${it.uniqueId} at @s run tp ${markerLocation.x} ${markerLocation.y} ${markerLocation.z}")
                }
            ).submit(spider to "mountable.marker")
        }
    }
}
