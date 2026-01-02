package com.voltaccept.spideranimation.spider

import com.voltaccept.spideranimation.spider.components.body.setupSpiderBody
import com.voltaccept.spideranimation.spider.components.*
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


fun setupSpider(app: ECS) {
    setupSpiderBody(app)
    setupBehaviours(app)

    app.onTick {
        for ((entity, _) in app.query<ECSEntity, SpiderBody>()) {
            entity.replaceComponent<SpiderBehaviour>(StayStillBehaviour())
        }
    }

    setupCloak(app)
    setupMountable(app)
    setupPointDetector(app)
    setupSoundAndParticles(app)
    setupTridentHitDetector(app)
    setupRenderer(app)
    setupFeeding(app)

    // Add damage listener for player attacks on spider
    addEventListener(object : Listener {
        @EventHandler
        fun onDamage(event: EntityDamageByEntityEvent) {
            val damaged = event.entity
            val damager = event.damager
            if (damager is org.bukkit.entity.Player) {
                val spider = RenderEntityTracker.getSpider(damaged)
                if (spider != null) {
                    // Players cannot damage the spider, just cancel the event
                    event.isCancelled = true
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
            // consume one redstone (unless creative)
            if (player.gameMode != GameMode.CREATIVE) {
                val amt = item.amount - 1
                if (amt <= 0) player.inventory.setItemInMainHand(null) else item.amount = amt
            }
            player.sendMessage("§aYou fed your spider and healed ${healed} health.")
            player.world.playSound(player.location, org.bukkit.Sound.ENTITY_GENERIC_EAT, 1.0f, 1.0f)
        } else {
            player.sendMessage("§7Your spider is already at full health.")
        }
    }
}

