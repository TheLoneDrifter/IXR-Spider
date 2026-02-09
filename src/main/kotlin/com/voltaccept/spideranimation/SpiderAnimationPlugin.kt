package com.voltaccept.spideranimation

import com.voltaccept.spideranimation.AppState.ecs
import com.voltaccept.spideranimation.kinematic_chain_visualizer.KinematicChainVisualizer
import com.voltaccept.spideranimation.kinematic_chain_visualizer.setupChainVisualizer
import com.voltaccept.spideranimation.spider.components.body.SpiderBody
import com.voltaccept.spideranimation.spider.components.rendering.SpiderRenderer
import com.voltaccept.spideranimation.spider.setupSpider
import com.voltaccept.spideranimation.laser.setupLaserPointer
import com.voltaccept.spideranimation.spider.components.PetBehaviour
import com.voltaccept.spideranimation.spider.components.setupPetBehaviour
import com.voltaccept.spideranimation.utilities.ecs.ECSEntity
import com.voltaccept.spideranimation.utilities.events.onSpawnEntity
import com.voltaccept.spideranimation.utilities.events.onTick
import com.voltaccept.spideranimation.utilities.setupCoreUtils
import com.voltaccept.spideranimation.utilities.shutdownCoreUtils
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class SpiderAnimationPlugin : JavaPlugin() {
    fun writeAndSaveConfig() {
//            for ((key, value) in options) {
//                instance.config.set(key, Serializer.toMap(value()))
//            }
//            instance.saveConfig()
    }

    override fun onDisable() {
        this.logger.info("Disabling Spider Animation plugin")
        PetSpiderManager.cleanup()
        PetSpiderSettingsManager.cleanup()
        this.shutdownCoreUtils()
    }

    override fun onEnable() {
        this.logger.info("Enabling Spider Animation plugin")

        this.setupCoreUtils()

        setupCommands(this)
        setupItems()
        setupSpider(ecs)
        setupPetBehaviour(ecs)
        setupChainVisualizer(ecs)
        setupLaserPointer(ecs)
        
        this.server.pluginManager.registerEvents(PetSpiderMenuListener(), this)
        this.server.pluginManager.registerEvents(PetSpiderPlayerListener(), this)
        this.server.pluginManager.registerEvents(PetMainMenuListener(), this)
        this.server.pluginManager.registerEvents(com.voltaccept.spideranimation.menus.SpiderSettingsMenuListener(), this)
        this.server.pluginManager.registerEvents(com.voltaccept.spideranimation.menus.LegCountMenuListener(), this)
        this.server.pluginManager.registerEvents(com.voltaccept.spideranimation.menus.EyeColorMenuListener(), this)

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
