package com.voltaccept.spideranimation.spider.configuration

import com.voltaccept.spideranimation.spider.presets.AnimatedPalettes
import com.voltaccept.spideranimation.spider.presets.SpiderTorsoModels
import com.voltaccept.spideranimation.utilities.DisplayModel
import org.bukkit.util.Vector

class SegmentPlan(
    var length: Double,
    var initDirection: Vector,
    var model: DisplayModel = DisplayModel(listOf())
) {
    fun clone() = SegmentPlan(length, initDirection.clone(), model.clone())
}

class LegPlan(
    var attachmentPosition: Vector,
    var restPosition: Vector,
    var segments: List<SegmentPlan>,
)

class BodyPlan {
    var scale = 1.0
    var legs = emptyList<LegPlan>()

    var bodyModel = SpiderTorsoModels.EMPTY.model.clone()

    var eyePalette = AnimatedPalettes.LIME_EYES.palette
    var blinkingPalette = AnimatedPalettes.LIME_BLINKING_LIGHTS.palette

    fun scale(scale: Double) {
        this.scale *= scale
        bodyModel.scale(scale.toFloat())
        legs.forEach {
            it.attachmentPosition.multiply(scale)
            it.restPosition.multiply(scale)
            it.segments.forEach { segment ->
                segment.length *= scale
                segment.model.scale(scale.toFloat())
            }
        }
    }
}
