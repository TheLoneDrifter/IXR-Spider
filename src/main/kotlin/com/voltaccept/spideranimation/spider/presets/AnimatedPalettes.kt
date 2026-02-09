package com.voltaccept.spideranimation.spider.presets

import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Display

enum class AnimatedPalettes(val palette: List<Pair<BlockData, Display.Brightness>>) {
    CYAN_EYES(arrayOf(
        * Array(3) { Material.CYAN_SHULKER_BOX },
        Material.CYAN_CONCRETE,
        Material.CYAN_CONCRETE_POWDER,

        Material.LIGHT_BLUE_SHULKER_BOX,
        Material.LIGHT_BLUE_CONCRETE,
        Material.LIGHT_BLUE_CONCRETE_POWDER,
    ).map { it.createBlockData() to Display.Brightness(15,15) }),

    CYAN_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.VERDANT_FROGLIGHT to Display.Brightness(15,15) },
        Material.LIGHT_BLUE_SHULKER_BOX to Display.Brightness(15,15),
        Material.LIGHT_BLUE_CONCRETE to Display.Brightness(15,15),
        Material.LIGHT_BLUE_CONCRETE_POWDER to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),


    RED_EYES(arrayOf(
        * Array(3) { Material.RED_SHULKER_BOX },
        Material.RED_CONCRETE,
        Material.RED_CONCRETE_POWDER,

        Material.FIRE_CORAL_BLOCK,
        Material.REDSTONE_BLOCK,
    ).map { it.createBlockData() to Display.Brightness(15,15) }),

    RED_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.PEARLESCENT_FROGLIGHT to Display.Brightness(15,15) },
        Material.RED_TERRACOTTA to Display.Brightness(15,15),
        Material.REDSTONE_BLOCK to Display.Brightness(15,15),
        Material.FIRE_CORAL_BLOCK to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),

    LIME_EYES(arrayOf(
        * Array(3) { Material.LIME_SHULKER_BOX },
        Material.LIME_CONCRETE,
        Material.LIME_CONCRETE_POWDER,

        Material.GREEN_SHULKER_BOX,
        Material.GREEN_CONCRETE,
        Material.GREEN_CONCRETE_POWDER,
    ).map { it.createBlockData() to Display.Brightness(15,15)}),

    LIME_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.LIME_SHULKER_BOX to Display.Brightness(15,15) },
        Material.GREEN_SHULKER_BOX to Display.Brightness(15,15),
        Material.LIME_CONCRETE to Display.Brightness(15,15),
        Material.LIME_CONCRETE_POWDER to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),

    WHITE_EYES(arrayOf(
        * Array(3) { Material.WHITE_SHULKER_BOX },
        Material.WHITE_CONCRETE,
        Material.WHITE_CONCRETE_POWDER,

        Material.LIGHT_GRAY_SHULKER_BOX,
        Material.LIGHT_GRAY_CONCRETE,
        Material.LIGHT_GRAY_CONCRETE_POWDER,
    ).map { it.createBlockData() to Display.Brightness(15,15)}),

    WHITE_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.WHITE_SHULKER_BOX to Display.Brightness(15,15) },
        Material.LIGHT_GRAY_SHULKER_BOX to Display.Brightness(15,15),
        Material.WHITE_CONCRETE to Display.Brightness(15,15),
        Material.WHITE_CONCRETE_POWDER to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),

    ORANGE_EYES(arrayOf(
        * Array(3) { Material.ORANGE_SHULKER_BOX },
        Material.ORANGE_CONCRETE,
        Material.ORANGE_CONCRETE_POWDER,

        Material.ACACIA_WOOD,
        Material.ORANGE_TERRACOTTA,
    ).map { it.createBlockData() to Display.Brightness(15,15)}),

    ORANGE_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.ORANGE_SHULKER_BOX to Display.Brightness(15,15) },
        Material.ACACIA_WOOD to Display.Brightness(15,15),
        Material.ORANGE_CONCRETE to Display.Brightness(15,15),
        Material.ORANGE_CONCRETE_POWDER to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),

    MAGENTA_EYES(arrayOf(
        * Array(3) { Material.MAGENTA_SHULKER_BOX },
        Material.MAGENTA_CONCRETE,
        Material.MAGENTA_CONCRETE_POWDER,

        Material.PURPUR_BLOCK,
        Material.MAGENTA_WOOL,
    ).map { it.createBlockData() to Display.Brightness(15,15)}),

    MAGENTA_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.MAGENTA_SHULKER_BOX to Display.Brightness(15,15) },
        Material.PURPUR_BLOCK to Display.Brightness(15,15),
        Material.MAGENTA_CONCRETE to Display.Brightness(15,15),
        Material.MAGENTA_CONCRETE_POWDER to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),

    LIGHT_BLUE_EYES(arrayOf(
        * Array(3) { Material.LIGHT_BLUE_SHULKER_BOX },
        Material.LIGHT_BLUE_CONCRETE,
        Material.LIGHT_BLUE_CONCRETE_POWDER,

        Material.BLUE_ICE,
        Material.LIGHT_BLUE_WOOL,
    ).map { it.createBlockData() to Display.Brightness(15,15)}),

    LIGHT_BLUE_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.LIGHT_BLUE_SHULKER_BOX to Display.Brightness(15,15) },
        Material.BLUE_ICE to Display.Brightness(15,15),
        Material.LIGHT_BLUE_CONCRETE to Display.Brightness(15,15),
        Material.LIGHT_BLUE_CONCRETE_POWDER to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),

    YELLOW_EYES(arrayOf(
        * Array(3) { Material.YELLOW_SHULKER_BOX },
        Material.YELLOW_CONCRETE,
        Material.YELLOW_CONCRETE_POWDER,

        Material.GOLD_BLOCK,
        Material.SUNFLOWER,
    ).map { it.createBlockData() to Display.Brightness(15,15)}),

    YELLOW_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.YELLOW_SHULKER_BOX to Display.Brightness(15,15) },
        Material.GOLD_BLOCK to Display.Brightness(15,15),
        Material.YELLOW_CONCRETE to Display.Brightness(15,15),
        Material.YELLOW_CONCRETE_POWDER to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),

    LIGHT_GRAY_EYES(arrayOf(
        * Array(3) { Material.LIGHT_GRAY_SHULKER_BOX },
        Material.LIGHT_GRAY_CONCRETE,
        Material.LIGHT_GRAY_CONCRETE_POWDER,

        Material.STONE,
        Material.GRAY_WOOL,
    ).map { it.createBlockData() to Display.Brightness(15,15)}),

    LIGHT_GRAY_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.LIGHT_GRAY_SHULKER_BOX to Display.Brightness(15,15) },
        Material.STONE to Display.Brightness(15,15),
        Material.LIGHT_GRAY_CONCRETE to Display.Brightness(15,15),
        Material.LIGHT_GRAY_CONCRETE_POWDER to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),

    GRAY_EYES(arrayOf(
        * Array(3) { Material.GRAY_SHULKER_BOX },
        Material.GRAY_CONCRETE,
        Material.GRAY_CONCRETE_POWDER,

        Material.ANDESITE,
        Material.GRAY_WOOL,
    ).map { it.createBlockData() to Display.Brightness(15,15)}),

    GRAY_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.GRAY_SHULKER_BOX to Display.Brightness(15,15) },
        Material.ANDESITE to Display.Brightness(15,15),
        Material.GRAY_CONCRETE to Display.Brightness(15,15),
        Material.GRAY_CONCRETE_POWDER to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),

    PINK_EYES(arrayOf(
        * Array(3) { Material.PINK_SHULKER_BOX },
        Material.PINK_CONCRETE,
        Material.PINK_CONCRETE_POWDER,

        Material.PINK_WOOL,
        Material.CHERRY_LOG,
    ).map { it.createBlockData() to Display.Brightness(15,15)}),

    PINK_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.PINK_SHULKER_BOX to Display.Brightness(15,15) },
        Material.PINK_WOOL to Display.Brightness(15,15),
        Material.PINK_CONCRETE to Display.Brightness(15,15),
        Material.PINK_CONCRETE_POWDER to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),

    PURPLE_EYES(arrayOf(
        * Array(3) { Material.PURPLE_SHULKER_BOX },
        Material.PURPLE_CONCRETE,
        Material.PURPLE_CONCRETE_POWDER,

        Material.AMETHYST_BLOCK,
        Material.PURPLE_WOOL,
    ).map { it.createBlockData() to Display.Brightness(15,15)}),

    PURPLE_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.PURPLE_SHULKER_BOX to Display.Brightness(15,15) },
        Material.AMETHYST_BLOCK to Display.Brightness(15,15),
        Material.PURPLE_CONCRETE to Display.Brightness(15,15),
        Material.PURPLE_CONCRETE_POWDER to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),

    BLUE_EYES(arrayOf(
        * Array(3) { Material.BLUE_SHULKER_BOX },
        Material.BLUE_CONCRETE,
        Material.BLUE_CONCRETE_POWDER,

        Material.LAPIS_BLOCK,
        Material.BLUE_WOOL,
    ).map { it.createBlockData() to Display.Brightness(15,15)}),

    BLUE_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.BLUE_SHULKER_BOX to Display.Brightness(15,15) },
        Material.LAPIS_BLOCK to Display.Brightness(15,15),
        Material.BLUE_CONCRETE to Display.Brightness(15,15),
        Material.BLUE_CONCRETE_POWDER to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),

    BROWN_EYES(arrayOf(
        * Array(3) { Material.BROWN_SHULKER_BOX },
        Material.BROWN_CONCRETE,
        Material.BROWN_CONCRETE_POWDER,

        Material.OAK_WOOD,
        Material.BROWN_WOOL,
    ).map { it.createBlockData() to Display.Brightness(15,15)}),

    BROWN_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.BROWN_SHULKER_BOX to Display.Brightness(15,15) },
        Material.OAK_WOOD to Display.Brightness(15,15),
        Material.BROWN_CONCRETE to Display.Brightness(15,15),
        Material.BROWN_CONCRETE_POWDER to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),

    GREEN_EYES(arrayOf(
        * Array(3) { Material.GREEN_SHULKER_BOX },
        Material.GREEN_CONCRETE,
        Material.GREEN_CONCRETE_POWDER,

        Material.MOSS_BLOCK,
        Material.GREEN_WOOL,
    ).map { it.createBlockData() to Display.Brightness(15,15)}),

    GREEN_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.GREEN_SHULKER_BOX to Display.Brightness(15,15) },
        Material.MOSS_BLOCK to Display.Brightness(15,15),
        Material.GREEN_CONCRETE to Display.Brightness(15,15),
        Material.GREEN_CONCRETE_POWDER to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),

    RED_EYES(arrayOf(
        * Array(3) { Material.RED_SHULKER_BOX },
        Material.RED_CONCRETE,
        Material.RED_CONCRETE_POWDER,

        Material.FIRE_CORAL_BLOCK,
        Material.REDSTONE_BLOCK,
    ).map { it.createBlockData() to Display.Brightness(15,15)}),

    RED_BLINKING_LIGHTS(arrayOf(
        * Array(3) { Material.BLACK_SHULKER_BOX to Display.Brightness(0,15) },
        * Array(3) { Material.PEARLESCENT_FROGLIGHT to Display.Brightness(15,15) },
        Material.RED_TERRACOTTA to Display.Brightness(15,15),
        Material.REDSTONE_BLOCK to Display.Brightness(15,15),
        Material.FIRE_CORAL_BLOCK to Display.Brightness(15,15),
    ).map { (block, brightness) -> block.createBlockData() to brightness }),

    }
