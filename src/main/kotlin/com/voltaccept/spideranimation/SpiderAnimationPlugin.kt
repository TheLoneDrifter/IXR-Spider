package com.voltaccept.spideranimation

import com.voltaccept.spideranimation.AppState.ecs
import com.voltaccept.spideranimation.kinematic_chain_visualizer.KinematicChainVisualizer
import com.voltaccept.spideranimation.kinematic_chain_visualizer.setupChainVisualizer
import com.voltaccept.spideranimation.spider.components.body.SpiderBody
import com.voltaccept.spideranimation.spider.components.rendering.SpiderRenderer
import com.voltaccept.spideranimation.spider.setupSpider
import com.voltaccept.spideranimation.laser.setupLaserPointer
import com.voltaccept.spideranimation.laser.setupLaserAttacks
import com.voltaccept.spideranimation.spider.components.PetBehaviour
import com.voltaccept.spideranimation.spider.components.setupPetBehaviour
import com.voltaccept.spideranimation.utilities.ecs.ECSEntity
import com.voltaccept.spideranimation.utilities.events.onSpawnEntity
import com.voltaccept.spideranimation.utilities.events.onTick
import com.voltaccept.spideranimation.utilities.setupCoreUtils
import com.voltaccept.spideranimation.utilities.shutdownCoreUtils
import org.bukkit.damage.DamageType
import org.bukkit.damage.DamageScaling
import org.bukkit.damage.DamageEffects
import org.bukkit.damage.DeathMessageType
import org.bukkit.NamespacedKey

@Suppress("unused")
class SpiderAnimationPlugin : JavaPlugin() {
    fun writeAndSaveConfig() {
//            for ((key, value) in options) {
//                instance.config.set(key, Serializer.toMap(value()))
//            }
//            instance.saveConfig()
    }

    override fun onDisable() {
        logger.info("Disabling Spider Animation plugin")
        PetSpiderManager.cleanup()
        shutdownCoreUtils()
    }

    override fun onEnable() {
        logger.info("Enabling Spider Animation plugin")

        setupCoreUtils()

        // Register custom damage type for spider pellets
        val damageType = DamageType("ixr:spider_pellet", DamageScaling.NEVER, 0.0f, DamageEffects.HURT, DeathMessageType.DEFAULT)
        server.damageTypeRegistry.register(NamespacedKey("ixr", "spider_pellet"), damageType)

        setupCommands(this)
        setupItems()
        setupSpider(ecs)
        setupPetBehaviour(ecs)
        setupChainVisualizer(ecs)
        setupLaserPointer(ecs)
        setupLaserAttacks(ecs)
        
        server.pluginManager.registerEvents(PetSpiderMenuListener(), this)
        server.pluginManager.registerEvents(PetSpiderPlayerListener(), this)
        server.pluginManager.registerEvents(PetMainMenuListener(), this)

        ecs.start()
        onTick {
            // sync AppState properties
            ecs.query<ECSEntity, SpiderBody>().forEach { (entity, spider) ->
                entity.query<SpiderRenderer>()?.renderDebugVisuals = AppState.renderDebugVisuals
                spider.gallop = AppState.gallop
            }

            ecs.update()
            ecs.render()
        }


        onSpawnEntity { entity ->
            // Use this command to spawn a chain visualizer
            // /summon minecraft:area_effect_cloud ~ ~ ~ {Tags:["spider.chain_visualizer"]}
            if (!entity.scoreboardTags.contains("spider.chain_visualizer")) return@onSpawnEntity

            val oldVisualizer = ecs.query<ECSEntity, KinematicChainVisualizer>().firstOrNull()?.first
            if (oldVisualizer == null) {
                AppState.createChainVisualizer(entity.location)
            } else {
                oldVisualizer.remove()
            }

            entity.remove()
        }
    }
}

