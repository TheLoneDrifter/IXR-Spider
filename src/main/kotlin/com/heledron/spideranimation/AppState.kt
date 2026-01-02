package com.heledron.spideranimation

import com.heledron.spideranimation.kinematic_chain_visualizer.KinematicChainVisualizer
import com.heledron.spideranimation.spider.components.body.SpiderBody
import com.heledron.spideranimation.spider.components.Cloak
import com.heledron.spideranimation.spider.components.Mountable
import com.heledron.spideranimation.spider.components.PointDetector
import com.heledron.spideranimation.spider.components.SoundsAndParticles
import com.heledron.spideranimation.spider.components.TridentHitDetector
import com.heledron.spideranimation.spider.components.PetBehaviour
import com.heledron.spideranimation.spider.presets.hexBot
import com.heledron.spideranimation.spider.components.rendering.SpiderRenderer
import com.heledron.spideranimation.utilities.ecs.ECS
import com.heledron.spideranimation.utilities.ecs.ECSEntity
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
            hexBot(4, 1.0).apply { 
                setAbsoluteScale(0.25) 
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
