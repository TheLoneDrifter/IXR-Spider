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
    private const val MENU_TITLE = "§6§lSP1D.3R Settings - Eye Color"
    private const val BACK_SLOT = 0
    
    // Eye color options and their slots
    private val colorOptions = mapOf(
        AnimatedPalettes.LIME_EYES to 12,
        AnimatedPalettes.CYAN_EYES to 14,
        AnimatedPalettes.RED_EYES to 16
    )
    
    fun openMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 27, MENU_TITLE)
        
        // Back button
        inventory.setItem(BACK_SLOT, ItemStack(Material.ARROW).apply {
            val meta = itemMeta!!
            meta.setDisplayName("§e§lBack")
            meta.lore = listOf("§7Return to SP1D.3R settings")
            itemMeta = meta
        })
        
        // Current selection indicator
        val currentEyeColor = com.voltaccept.spideranimation.PetSpiderSettingsManager.getSettings(player).eyeColor
        
        // Eye color options
        colorOptions.forEach { (eyeColor, slot) ->
            val material = when (eyeColor) {
                AnimatedPalettes.LIME_EYES -> Material.LIME_CONCRETE
                AnimatedPalettes.CYAN_EYES -> Material.CYAN_CONCRETE
                AnimatedPalettes.RED_EYES -> Material.RED_CONCRETE
                else -> Material.WHITE_CONCRETE
            }
            
            val isSelected = eyeColor == currentEyeColor
            
            val item = ItemStack(material).apply {
                val meta = itemMeta!!
                val displayName = when (eyeColor) {
                    AnimatedPalettes.LIME_EYES -> "§a§lLime Green Eyes"
                    AnimatedPalettes.CYAN_EYES -> "§b§lCyan Eyes"
                    AnimatedPalettes.RED_EYES -> "§c§lRed Eyes"
                    else -> "§f§lCustom Eyes"
                }
                
                meta.setDisplayName(if (isSelected) "§a✓ $displayName" else displayName)
                
                val lore = mutableListOf<String>()
                lore.add("§7Choose the eye color for your SP1D.3R")
                
                when (eyeColor) {
                    AnimatedPalettes.LIME_EYES -> lore.addAll(listOf("§7• Vibrant lime green", "§7• High visibility", "§7• Default color"))
                    AnimatedPalettes.CYAN_EYES -> lore.addAll(listOf("§7• Cool cyan blue", "§7• Calm appearance", "§7• Original color"))
                    AnimatedPalettes.RED_EYES -> lore.addAll(listOf("§7• Intense red", "§7• Menacing look", "§7• High contrast"))
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
                SpiderSettingsMenu.openMenu(player)
            }
            else -> {
                // Check if it's an eye color option
                colorOptions.forEach { (eyeColor, slot) ->
                    if (event.slot == slot) {
                        com.voltaccept.spideranimation.PetSpiderSettingsManager.setEyeColor(player, eyeColor)
                        val colorName = when (eyeColor) {
                            AnimatedPalettes.LIME_EYES -> "lime green"
                            AnimatedPalettes.CYAN_EYES -> "cyan"
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
