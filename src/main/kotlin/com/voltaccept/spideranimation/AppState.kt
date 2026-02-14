package com.voltaccept.spideranimation

import com.voltaccept.spideranimation.kinematic_chain_visualizer.KinematicChainVisualizer
import com.voltaccept.spideranimation.spider.components.body.SpiderBody
import com.voltaccept.spideranimation.spider.components.Cloak
import com.voltaccept.spideranimation.spider.components.Mountable
import com.voltaccept.spideranimation.spider.components.PointDetector
import com.voltaccept.spideranimation.spider.components.SoundsAndParticles
import com.voltaccept.spideranimation.spider.components.TridentHitDetector
import com.voltaccept.spideranimation.spider.components.PetBehaviour
import com.voltaccept.spideranimation.spider.presets.hexBot
import com.voltaccept.spideranimation.spider.presets.biped
import com.voltaccept.spideranimation.spider.presets.quadBot
import com.voltaccept.spideranimation.spider.presets.octoBot
import com.voltaccept.spideranimation.spider.presets.decaBot
import com.voltaccept.spideranimation.spider.presets.AnimatedPalettes
import com.voltaccept.spideranimation.spider.components.rendering.SpiderRenderer
import com.voltaccept.spideranimation.utilities.ecs.ECS
import com.voltaccept.spideranimation.utilities.ecs.ECSEntity
import org.bukkit.Location
import org.bukkit.entity.Player

object AppState {
    var options = hexBot(4, 1.0)
    var miscOptions = MiscellaneousOptions()
    var renderDebugVisuals = false

    var gallop = false

    val ecs = ECS()

    var target: Location? = null

    fun createSpider(location: Location, owner: Player? = null): ECSEntity {
        val spiderOptions = if (owner != null) {
            val settings = PetSpiderSettingsManager.getSettings(owner)
            
            // Create spider based on leg count setting
            val baseOptions = when (settings.legCount) {
                2 -> biped(4, 1.0)
                4 -> quadBot(4, 1.0)
                6 -> hexBot(4, 1.0)
                8 -> octoBot(4, 1.0)
                10 -> decaBot(4, 1.0)
                else -> hexBot(4, 1.0) // Default to 6 legs
            }
            
            baseOptions.apply { 
                setAbsoluteScale(0.5)
                // Apply eye color settings
                when (settings.eyeColor) {
                    AnimatedPalettes.WHITE_EYES -> {
                        bodyPlan.eyePalette = AnimatedPalettes.WHITE_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.WHITE_BLINKING_LIGHTS.palette
                    }
                    AnimatedPalettes.ORANGE_EYES -> {
                        bodyPlan.eyePalette = AnimatedPalettes.ORANGE_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.ORANGE_BLINKING_LIGHTS.palette
                    }
                    AnimatedPalettes.MAGENTA_EYES -> {
                        bodyPlan.eyePalette = AnimatedPalettes.MAGENTA_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.MAGENTA_BLINKING_LIGHTS.palette
                    }
                    AnimatedPalettes.LIGHT_BLUE_EYES -> {
                        bodyPlan.eyePalette = AnimatedPalettes.LIGHT_BLUE_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.LIGHT_BLUE_BLINKING_LIGHTS.palette
                    }
                    AnimatedPalettes.YELLOW_EYES -> {
                        bodyPlan.eyePalette = AnimatedPalettes.YELLOW_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.YELLOW_BLINKING_LIGHTS.palette
                    }
                    AnimatedPalettes.LIME_EYES -> {
                        bodyPlan.eyePalette = AnimatedPalettes.LIME_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.LIME_BLINKING_LIGHTS.palette
                    }
                    AnimatedPalettes.PINK_EYES -> {
                        bodyPlan.eyePalette = AnimatedPalettes.PINK_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.PINK_BLINKING_LIGHTS.palette
                    }
                    AnimatedPalettes.GRAY_EYES -> {
                        bodyPlan.eyePalette = AnimatedPalettes.GRAY_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.GRAY_BLINKING_LIGHTS.palette
                    }
                    AnimatedPalettes.LIGHT_GRAY_EYES -> {
                        bodyPlan.eyePalette = AnimatedPalettes.LIGHT_GRAY_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.LIGHT_GRAY_BLINKING_LIGHTS.palette
                    }
                    AnimatedPalettes.CYAN_EYES -> {
                        bodyPlan.eyePalette = AnimatedPalettes.CYAN_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.CYAN_BLINKING_LIGHTS.palette
                    }
                    AnimatedPalettes.PURPLE_EYES -> {
                        bodyPlan.eyePalette = AnimatedPalettes.PURPLE_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.PURPLE_BLINKING_LIGHTS.palette
                    }
                    AnimatedPalettes.BLUE_EYES -> {
                        bodyPlan.eyePalette = AnimatedPalettes.BLUE_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.BLUE_BLINKING_LIGHTS.palette
                    }
                    AnimatedPalettes.BROWN_EYES -> {
                        bodyPlan.eyePalette = AnimatedPalettes.BROWN_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.BROWN_BLINKING_LIGHTS.palette
                    }
                    AnimatedPalettes.GREEN_EYES -> {
                        bodyPlan.eyePalette = AnimatedPalettes.GREEN_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.GREEN_BLINKING_LIGHTS.palette
                    }
                    AnimatedPalettes.RED_EYES -> {
                        bodyPlan.eyePalette = AnimatedPalettes.RED_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.RED_BLINKING_LIGHTS.palette
                    }
                    else -> {
                        // Default to lime green for any unknown colors
                        bodyPlan.eyePalette = AnimatedPalettes.LIME_EYES.palette
                        bodyPlan.blinkingPalette = AnimatedPalettes.LIME_BLINKING_LIGHTS.palette
                    }
                }
                
                // Apply concrete color settings to body and legs
                val concreteColor = when (settings.concreteColor) {
                    ConcreteColor.BLACK -> org.bukkit.Material.BLACK_CONCRETE
                    ConcreteColor.WHITE -> org.bukkit.Material.WHITE_CONCRETE
                    ConcreteColor.HONEYCOMB -> org.bukkit.Material.HONEYCOMB_BLOCK
                    ConcreteColor.DIAMOND -> org.bukkit.Material.DIAMOND_BLOCK
                }
                
                // Update body model pieces
                bodyPlan.bodyModel.pieces.forEach { piece ->
                    if (piece.block.material == org.bukkit.Material.BLACK_CONCRETE ||
                        piece.block.material == org.bukkit.Material.NETHERITE_BLOCK ||
                        piece.block.material == org.bukkit.Material.ANVIL ||
                        piece.block.material == org.bukkit.Material.GRAY_CONCRETE) {
                        if (!piece.tags.contains("eye")) {
                            piece.block = concreteColor.createBlockData()
                        }
                    }
                }
                
                // Update leg segments color
                bodyPlan.legs.forEach { legPlan ->
                    legPlan.segments.forEach { segment ->
                        segment.model.pieces.forEach { piece ->
                            if (piece.block.material == org.bukkit.Material.BLACK_CONCRETE ||
                                piece.block.material == org.bukkit.Material.ANVIL ||
                                piece.block.material == org.bukkit.Material.SMOOTH_QUARTZ) {
                                if (!piece.tags.contains("eye")) {
                                    piece.block = concreteColor.createBlockData()
                                }
                            }
                        }
                    }
                }
            }
        } else {
            options
        }
        
        location.y += spiderOptions.walkGait.stationary.bodyHeight
        val entity = ecs.spawn(
            SpiderBody.fromLocation(location, spiderOptions.bodyPlan, walkGait = spiderOptions.walkGait, gallopGait = spiderOptions.gallopGait),
            TridentHitDetector(),
            Cloak(spiderOptions.cloak),
            SoundsAndParticles(spiderOptions.sound),
            Mountable(),
            PointDetector(),
            SpiderRenderer(),
        )
        
        if (owner != null) {
            entity.addComponent(PetSpiderOwner(owner.uniqueId))
            entity.addComponent(PetBehaviour())
            val spider = entity.query<SpiderBody>()
            spider?.gallop = true
            // Load saved fuel for the spider
            spider?.fuel = PetSpiderSettingsManager.getSpiderFuel(owner)
            PetSpiderManager.setSpider(owner, entity)
        }
        
        return entity
    }

    fun createChainVisualizer(location: Location): ECSEntity {
        val segmentPlans = options.bodyPlan.legs.lastOrNull()?.segments ?: throw Error("Cannot find segment plans")

        return ecs.spawn(KinematicChainVisualizer.create(
            segmentPlans = segmentPlans,
            root = location.toVector(),
            world = location.world ?: throw Error("location.world is null"),
            straightenRotation = options.walkGait.legStraightenRotation,
        ).apply {
            detailed = renderDebugVisuals
        })
    }

    fun recreateSpider() {
        val spider = ecs.query<SpiderBody>().firstOrNull() ?: return
        createSpider(spider.location())
    }
}

class MiscellaneousOptions {
    var showLaser = true
}

data class PetSpiderOwner(val ownerUUID: java.util.UUID)
