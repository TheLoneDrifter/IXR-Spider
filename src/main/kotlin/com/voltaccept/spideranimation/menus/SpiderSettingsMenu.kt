package com.voltaccept.spideranimation.menus

import com.voltaccept.spideranimation.PetSpiderMenu
import com.voltaccept.spideranimation.PetSpiderSettingsManager
import com.voltaccept.spideranimation.ConcreteColor
import com.voltaccept.spideranimation.spider.presets.AnimatedPalettes
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

object SpiderSettingsMenu {
    private const val MENU_TITLE = "§6§lSettings"
    private const val BACK_SLOT = 0
    private const val LEG_COUNT_SLOT = 11
    private const val BODY_COLOR_SLOT = 13
    private const val EYE_COLOR_SLOT = 15
    
    fun openMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 27, MENU_TITLE)
        
        // Back button
        inventory.setItem(BACK_SLOT, ItemStack(Material.ARROW).apply {
            val meta = itemMeta!!
            meta.setDisplayName("§e§lBack")
            meta.lore = listOf("§7Return to SP1D.3R menu")
            itemMeta = meta
        })
        
        // Get current settings
        val settings = PetSpiderSettingsManager.getSettings(player)
        
        // Leg count setting
        val legCountItem = ItemStack(Material.CHAINMAIL_BOOTS).apply {
            val meta = itemMeta!!
            meta.setDisplayName("§b§lLeg Count")
            meta.lore = listOf(
                "§7Current: §f${settings.legCount} legs",
                "§7Click to configure leg count",
                "",
                "§7Available options:",
                "§7• 4 legs (Quadruped)",
                "§7• 6 legs (Hexapod)",
                "§7• 8 legs (Octopod)"
            )
            itemMeta = meta
        }
        
        // Eye color setting
        val eyeColorItem = when (settings.eyeColor) {
            AnimatedPalettes.WHITE_EYES -> Material.WHITE_CONCRETE
            AnimatedPalettes.ORANGE_EYES -> Material.ORANGE_CONCRETE
            AnimatedPalettes.MAGENTA_EYES -> Material.MAGENTA_CONCRETE
            AnimatedPalettes.LIGHT_BLUE_EYES -> Material.LIGHT_BLUE_CONCRETE
            AnimatedPalettes.YELLOW_EYES -> Material.YELLOW_CONCRETE
            AnimatedPalettes.LIME_EYES -> Material.LIME_CONCRETE
            AnimatedPalettes.PINK_EYES -> Material.PINK_CONCRETE
            AnimatedPalettes.GRAY_EYES -> Material.GRAY_CONCRETE
            AnimatedPalettes.LIGHT_GRAY_EYES -> Material.LIGHT_GRAY_CONCRETE
            AnimatedPalettes.CYAN_EYES -> Material.CYAN_CONCRETE
            AnimatedPalettes.PURPLE_EYES -> Material.PURPLE_CONCRETE
            AnimatedPalettes.BLUE_EYES -> Material.BLUE_CONCRETE
            AnimatedPalettes.BROWN_EYES -> Material.BROWN_CONCRETE
            AnimatedPalettes.GREEN_EYES -> Material.GREEN_CONCRETE
            AnimatedPalettes.RED_EYES -> Material.RED_CONCRETE
            else -> Material.WHITE_CONCRETE
        }
        
        val eyeColorItemStack = ItemStack(eyeColorItem).apply {
            val meta = itemMeta!!
            val colorName = when (settings.eyeColor) {
                AnimatedPalettes.WHITE_EYES -> "White"
                AnimatedPalettes.ORANGE_EYES -> "Orange"
                AnimatedPalettes.MAGENTA_EYES -> "Magenta"
                AnimatedPalettes.LIGHT_BLUE_EYES -> "Light Blue"
                AnimatedPalettes.YELLOW_EYES -> "Yellow"
                AnimatedPalettes.LIME_EYES -> "Lime Green"
                AnimatedPalettes.PINK_EYES -> "Pink"
                AnimatedPalettes.GRAY_EYES -> "Gray"
                AnimatedPalettes.LIGHT_GRAY_EYES -> "Light Gray"
                AnimatedPalettes.CYAN_EYES -> "Cyan"
                AnimatedPalettes.PURPLE_EYES -> "Purple"
                AnimatedPalettes.BLUE_EYES -> "Blue"
                AnimatedPalettes.BROWN_EYES -> "Brown"
                AnimatedPalettes.GREEN_EYES -> "Green"
                AnimatedPalettes.RED_EYES -> "Red"
                else -> "Custom"
            }
            meta.setDisplayName("§a§lEye Color")
            meta.lore = listOf(
                "§7Current: §f$colorName",
                "§7Click to configure eye color",
                "",
                "§7Includes 15+ eye color options"
            )
            itemMeta = meta
        }
        
        // Concrete color setting
        val concreteColorItem = when (settings.concreteColor) {
            ConcreteColor.BLACK -> Material.BLACK_CONCRETE
            ConcreteColor.WHITE -> Material.WHITE_CONCRETE
        }
        
        val concreteColorItemStack = ItemStack(concreteColorItem).apply {
            val meta = itemMeta!!
            val colorName = when (settings.concreteColor) {
                ConcreteColor.BLACK -> "Black"
                ConcreteColor.WHITE -> "White"
            }
            meta.setDisplayName("§f§lBody Color")
            meta.lore = listOf(
                "§7Current: §f$colorName Concrete",
                "§7Click to configure body and legs color",
                "",
                "§7Available options:",
                "§7• Black (default)",
                "§7• White"
            )
            itemMeta = meta
        }
        
        // Reset settings button
        val resetItem = ItemStack(Material.BARRIER).apply {
            val meta = itemMeta!!
            meta.setDisplayName("§c§lReset to Defaults")
            meta.lore = listOf(
                "§7Reset all settings to default values",
                "§7• 6 legs",
                "§7• Lime green eyes"
            )
            itemMeta = meta
        }
        
        inventory.setItem(LEG_COUNT_SLOT, legCountItem)
        inventory.setItem(EYE_COLOR_SLOT, eyeColorItemStack)
        inventory.setItem(BODY_COLOR_SLOT, concreteColorItemStack)
        inventory.setItem(22, resetItem)
        
        // Fill empty slots with glass panes
        for (i in 0 until 27) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
                    val meta = itemMeta!!
                    meta.setDisplayName("§f")
                    itemMeta = meta
                })
            }
        }
        
        player.openInventory(inventory)
    }
    
    fun handleClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        
        if (event.view.title != MENU_TITLE) return
        event.isCancelled = true
        
        when (event.slot) {
            BACK_SLOT -> {
                PetSpiderMenu.openMenu(player)
            }
            LEG_COUNT_SLOT -> {
                LegCountMenu.openMenu(player)
            }
            EYE_COLOR_SLOT -> {
                EyeColorMenu.openMenu(player)
            }
            BODY_COLOR_SLOT -> {
                ConcreteColorMenu.openMenu(player)
            }
            22 -> { // Reset button
                PetSpiderSettingsManager.resetSettings(player)
                player.sendMessage("§a§lSettings reset! §7All settings have been reset to defaults.")
                openMenu(player) // Refresh menu
            }
        }
    }
}

class SpiderSettingsMenuListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        SpiderSettingsMenu.handleClick(event)
    }
}
