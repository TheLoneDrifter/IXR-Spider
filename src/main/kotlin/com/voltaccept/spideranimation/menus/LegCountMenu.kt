package com.voltaccept.spideranimation.menus

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

object LegCountMenu {
    private const val MENU_TITLE = "§6§lSP1D.3R Settings - Leg Count"
    private const val BACK_SLOT = 0
    
    // Leg count options and their slots
    private val legOptions = mapOf(
        2 to 11,  // Biped
        4 to 13,  // Quadruped  
        6 to 15,  // Hexapod (default)
        8 to 17   // Octopod
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
        val currentLegCount = com.voltaccept.spideranimation.PetSpiderSettingsManager.getSettings(player).legCount
        
        // Leg count options
        legOptions.forEach { (legCount, slot) ->
            val material = when (legCount) {
                2 -> Material.IRON_BOOTS
                4 -> Material.LEATHER_BOOTS
                6 -> Material.CHAINMAIL_BOOTS
                8 -> Material.DIAMOND_BOOTS
                else -> Material.IRON_BOOTS
            }
            
            val isSelected = legCount == currentLegCount
            
            val item = ItemStack(material).apply {
                val meta = itemMeta!!
                val displayName = when (legCount) {
                    2 -> "§f§lBiped (2 Legs)"
                    4 -> "§a§lQuadruped (4 Legs)"
                    6 -> "§b§lHexapod (6 Legs)"
                    8 -> "§d§lOctopod (8 Legs)"
                    else -> "§f§l$legCount Legs"
                }
                
                meta.setDisplayName(if (isSelected) "§a✓ $displayName" else displayName)
                
                val lore = mutableListOf<String>()
                lore.add("§7Configure your SP1D.3R to have $legCount legs")
                
                when (legCount) {
                    2 -> lore.addAll(listOf("§7• Simple and stable", "§7• Good for basic movement"))
                    4 -> lore.addAll(listOf("§7• Balanced design", "§7• Good stability and speed"))
                    6 -> lore.addAll(listOf("§7• Classic spider look", "§7• Excellent stability"))
                    8 -> lore.addAll(listOf("§7• Maximum stability", "§7• Complex movement patterns"))
                }
                
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
                // Check if it's a leg count option
                legOptions.forEach { (legCount, slot) ->
                    if (event.slot == slot) {
                        com.voltaccept.spideranimation.PetSpiderSettingsManager.setLegCount(player, legCount)
                        player.sendMessage("§a§lLeg count updated! §7Your SP1D.3R will now have $legCount legs.")
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

class LegCountMenuListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        LegCountMenu.handleClick(event)
    }
}
