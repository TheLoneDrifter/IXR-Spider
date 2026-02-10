package com.voltaccept.spideranimation.menus

import com.voltaccept.spideranimation.spider.presets.AnimatedPalettes
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

object EyeColorMenu {
    private const val MENU_TITLE = "§6§lSettings - Eye Color"
    private const val BACK_SLOT = 45
    
    // Eye color options organized in a grid (excluding black)
    private val colorOptions = mapOf(
        AnimatedPalettes.WHITE_EYES to 10,
        AnimatedPalettes.ORANGE_EYES to 11,
        AnimatedPalettes.MAGENTA_EYES to 12,
        AnimatedPalettes.LIGHT_BLUE_EYES to 13,
        AnimatedPalettes.YELLOW_EYES to 14,
        AnimatedPalettes.LIME_EYES to 15,
        AnimatedPalettes.PINK_EYES to 16,
        AnimatedPalettes.GRAY_EYES to 19,
        AnimatedPalettes.LIGHT_GRAY_EYES to 20,
        AnimatedPalettes.CYAN_EYES to 21,
        AnimatedPalettes.PURPLE_EYES to 22,
        AnimatedPalettes.BLUE_EYES to 23,
        AnimatedPalettes.BROWN_EYES to 24,
        AnimatedPalettes.GREEN_EYES to 25,
        AnimatedPalettes.RED_EYES to 31
    )
    
    fun openMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 54, MENU_TITLE)
        
        // Back button
        inventory.setItem(BACK_SLOT, ItemStack(Material.ARROW).apply {
            val meta = itemMeta!!
            meta.setDisplayName("§e§lBack")
            meta.lore = listOf("§7Return to Settings")
            itemMeta = meta
        })
        
        // Current selection indicator
        val currentEyeColor = com.voltaccept.spideranimation.PetSpiderSettingsManager.getSettings(player).eyeColor
        
        // Eye color options
        colorOptions.forEach { (eyeColor, slot) ->
            val material = when (eyeColor) {
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
            
            val isSelected = eyeColor == currentEyeColor
            
            val item = ItemStack(material).apply {
                val meta = itemMeta!!
                val displayName = when (eyeColor) {
                    AnimatedPalettes.WHITE_EYES -> "§f§lWhite Eyes"
                    AnimatedPalettes.ORANGE_EYES -> "§6§lOrange Eyes"
                    AnimatedPalettes.MAGENTA_EYES -> "§d§lMagenta Eyes"
                    AnimatedPalettes.LIGHT_BLUE_EYES -> "§b§lLight Blue Eyes"
                    AnimatedPalettes.YELLOW_EYES -> "§e§lYellow Eyes"
                    AnimatedPalettes.LIME_EYES -> "§a§lLime Green Eyes"
                    AnimatedPalettes.PINK_EYES -> "§d§lPink Eyes"
                    AnimatedPalettes.GRAY_EYES -> "§8§lGray Eyes"
                    AnimatedPalettes.LIGHT_GRAY_EYES -> "§7§lLight Gray Eyes"
                    AnimatedPalettes.CYAN_EYES -> "§3§lCyan Eyes"
                    AnimatedPalettes.PURPLE_EYES -> "§5§lPurple Eyes"
                    AnimatedPalettes.BLUE_EYES -> "§9§lBlue Eyes"
                    AnimatedPalettes.BROWN_EYES -> "§6§lBrown Eyes"
                    AnimatedPalettes.GREEN_EYES -> "§2§lGreen Eyes"
                    AnimatedPalettes.RED_EYES -> "§c§lRed Eyes"
                    else -> "§f§lCustom Eyes"
                }
                
                meta.setDisplayName(if (isSelected) "§a✓ $displayName" else displayName)
                
                val lore = mutableListOf<String>()
                lore.add("§7Choose eye color for your SP1D.3R")
                
                when (eyeColor) {
                    AnimatedPalettes.WHITE_EYES -> lore.addAll(listOf("§7• Pure white", "§7• Clean appearance"))
                    AnimatedPalettes.ORANGE_EYES -> lore.addAll(listOf("§7• Warm orange", "§7• Vibrant energy"))
                    AnimatedPalettes.MAGENTA_EYES -> lore.addAll(listOf("§7• Bold magenta", "§7• Unique style"))
                    AnimatedPalettes.LIGHT_BLUE_EYES -> lore.addAll(listOf("§7• Sky blue", "§7• Calm and gentle"))
                    AnimatedPalettes.YELLOW_EYES -> lore.addAll(listOf("§7• Bright yellow", "§7• Sunny disposition"))
                    AnimatedPalettes.LIME_EYES -> lore.addAll(listOf("§7• Vibrant lime green", "§7• High visibility", "§7• Default color"))
                    AnimatedPalettes.PINK_EYES -> lore.addAll(listOf("§7• Playful pink", "§7• Friendly appearance"))
                    AnimatedPalettes.GRAY_EYES -> lore.addAll(listOf("§7• Neutral gray", "§7• Subtle look"))
                    AnimatedPalettes.LIGHT_GRAY_EYES -> lore.addAll(listOf("§7• Soft gray", "§7• Gentle appearance"))
                    AnimatedPalettes.CYAN_EYES -> lore.addAll(listOf("§7• Cool cyan blue", "§7• Calm appearance", "§7• Original color"))
                    AnimatedPalettes.PURPLE_EYES -> lore.addAll(listOf("§7• Royal purple", "§7• Mystical aura"))
                    AnimatedPalettes.BLUE_EYES -> lore.addAll(listOf("§7• Deep blue", "§7• Ocean depths"))
                    AnimatedPalettes.BROWN_EYES -> lore.addAll(listOf("§7• Earthy brown", "§7• Natural look"))
                    AnimatedPalettes.GREEN_EYES -> lore.addAll(listOf("§7• Forest green", "§7• Natural camouflage"))
                    AnimatedPalettes.RED_EYES -> lore.addAll(listOf("§7• Intense red", "§7• Menacing look", "§7• High contrast"))
                    else -> lore.addAll(listOf("§7• Custom eye color"))
                }
                
                lore.add("§7§oIncludes matching blinking light colors")
                
                if (isSelected) {
                    lore.add("§a§lCurrently Selected")
                }
                
                meta.lore = lore
                itemMeta = meta
            }
            
            inventory.setItem(slot, item)
        }
        
        // Fill empty slots with glass panes
        for (i in 0 until 54) {
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
                SpiderSettingsMenu.openMenu(player)
            }
            else -> {
                // Check if it's an eye color option
                colorOptions.forEach { (eyeColor, slot) ->
                    if (event.slot == slot) {
                        com.voltaccept.spideranimation.PetSpiderSettingsManager.setEyeColor(player, eyeColor)
                        val colorName = when (eyeColor) {
                            AnimatedPalettes.WHITE_EYES -> "white"
                            AnimatedPalettes.ORANGE_EYES -> "orange"
                            AnimatedPalettes.MAGENTA_EYES -> "magenta"
                            AnimatedPalettes.LIGHT_BLUE_EYES -> "light blue"
                            AnimatedPalettes.YELLOW_EYES -> "yellow"
                            AnimatedPalettes.LIME_EYES -> "lime green"
                            AnimatedPalettes.PINK_EYES -> "pink"
                            AnimatedPalettes.GRAY_EYES -> "gray"
                            AnimatedPalettes.LIGHT_GRAY_EYES -> "light gray"
                            AnimatedPalettes.CYAN_EYES -> "cyan"
                            AnimatedPalettes.PURPLE_EYES -> "purple"
                            AnimatedPalettes.BLUE_EYES -> "blue"
                            AnimatedPalettes.BROWN_EYES -> "brown"
                            AnimatedPalettes.GREEN_EYES -> "green"
                            AnimatedPalettes.RED_EYES -> "red"
                            else -> "custom"
                        }
                        player.sendMessage("§a§lEye color updated! §7Your SP1D.3R will now have $colorName eyes.")
                        player.sendMessage("§7§oNote: Changes will apply when you spawn a new SP1D.3R.")
                        
                        // Refresh menu to show selection
                        openMenu(player)
                        return@forEach
                    }
                }
            }
        }
    }
}

class EyeColorMenuListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        EyeColorMenu.handleClick(event)
    }
}
