package com.voltaccept.spideranimation.menus

import com.voltaccept.spideranimation.ConcreteColor
import com.voltaccept.spideranimation.PetSpiderSettingsManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

object ConcreteColorMenu {
    private const val MENU_TITLE = "§6§lSettings - Body Color"
    private const val BACK_SLOT = 10
    
    // Concrete color options
    private val colorOptions = mapOf(
        ConcreteColor.BLACK to 12,
        ConcreteColor.WHITE to 14,
        ConcreteColor.HONEYCOMB to 16,
        ConcreteColor.DIAMOND to 20
    )
    
    fun openMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 27, MENU_TITLE)
        
        // Back button
        inventory.setItem(BACK_SLOT, ItemStack(Material.ARROW).apply {
            val meta = itemMeta!!
            meta.setDisplayName("§e§lBack")
            meta.lore = listOf("§7Return to Settings")
            itemMeta = meta
        })
        
        // Current selection indicator
        val currentConcreteColor = PetSpiderSettingsManager.getSettings(player).concreteColor
        
        // Concrete color options
        colorOptions.forEach { (concreteColor, slot) ->
            val material = when (concreteColor) {
                ConcreteColor.BLACK -> Material.BLACK_CONCRETE
                ConcreteColor.WHITE -> Material.WHITE_CONCRETE
                ConcreteColor.HONEYCOMB -> Material.HONEYCOMB_BLOCK
                ConcreteColor.DIAMOND -> Material.DIAMOND_BLOCK
            }
            
            val isSelected = concreteColor == currentConcreteColor
            val isUnlocked = PetSpiderSettingsManager.isSkinUnlocked(player, concreteColor)
            
            val itemStack = ItemStack(material).apply {
                val meta = itemMeta!!
                val colorName = when (concreteColor) {
                    ConcreteColor.BLACK -> "§f§lBlack"
                    ConcreteColor.WHITE -> "§f§lWhite"
                    ConcreteColor.HONEYCOMB -> "§6§lHoneycomb"
                    ConcreteColor.DIAMOND -> "§b§lDiamond"
                }
                meta.setDisplayName(colorName)
                
                val lore = mutableListOf<String>()
                if (isSelected) {
                    lore.add("§a§l✓ Selected")
                    lore.add("")
                }
                
                if (!isUnlocked && (concreteColor == ConcreteColor.HONEYCOMB || concreteColor == ConcreteColor.DIAMOND)) {
                    lore.add("§c§lLOCKED")
                    val requirement = when (concreteColor) {
                        ConcreteColor.HONEYCOMB -> "64 Honeycomb blocks"
                        ConcreteColor.DIAMOND -> "64 Diamond blocks"
                        else -> ""
                    }
                    lore.add("§7Requires $requirement")
                    lore.add("§7in your inventory to unlock")
                    lore.add("")
                    lore.add("§7Click to attempt unlock")
                } else {
                    lore.add("§7Click to select")
                }
                
                meta.lore = lore
                itemMeta = meta
            }
            
            inventory.setItem(slot, itemStack)
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
            12 -> { // Black concrete
                PetSpiderSettingsManager.setConcreteColor(player, ConcreteColor.BLACK)
                player.sendMessage("§a§lBody color set to §0Black Concrete")
                openMenu(player) // Refresh menu
            }
            14 -> { // White concrete
                PetSpiderSettingsManager.setConcreteColor(player, ConcreteColor.WHITE)
                player.sendMessage("§a§lBody color set to §fWhite Concrete")
                openMenu(player) // Refresh menu
            }
            16 -> { // Honeycomb
                if (PetSpiderSettingsManager.isSkinUnlocked(player, ConcreteColor.HONEYCOMB)) {
                    PetSpiderSettingsManager.setConcreteColor(player, ConcreteColor.HONEYCOMB)
                    player.sendMessage("§a§lBody color set to §6Honeycomb")
                    openMenu(player) // Refresh menu
                } else {
                    // Check if player has 64 honeycomb blocks
                    val honeycombCount = player.inventory.contents?.filterNotNull()
                        ?.sumOf { if (it.type == Material.HONEYCOMB_BLOCK) it.amount else 0 } ?: 0
                    
                    if (honeycombCount >= 64) {
                        // Remove 64 honeycomb blocks and unlock the skin
                        var remainingToRemove = 64
                        player.inventory.contents?.filterNotNull()?.forEach { item ->
                            if (remainingToRemove > 0 && item.type == Material.HONEYCOMB_BLOCK) {
                                val toRemove = minOf(item.amount, remainingToRemove)
                                item.amount -= toRemove
                                remainingToRemove -= toRemove
                            }
                        }
                        
                        if (PetSpiderSettingsManager.unlockSkin(player, ConcreteColor.HONEYCOMB)) {
                            player.sendMessage("§a§lHoneycomb skin unlocked!")
                            player.sendMessage("§7Removed 64 Honeycomb blocks from your inventory")
                            player.sendMessage("§6§lBody color set to §6Honeycomb")
                            PetSpiderSettingsManager.setConcreteColor(player, ConcreteColor.HONEYCOMB)
                        }
                        openMenu(player) // Refresh menu
                    } else {
                        player.sendMessage("§c§lYou need 64 Honeycomb blocks to unlock this skin!")
                        player.sendMessage("§7You currently have: $honeycombCount/64 Honeycomb blocks")
                    }
                }
            }
            20 -> { // Diamond
                if (PetSpiderSettingsManager.isSkinUnlocked(player, ConcreteColor.DIAMOND)) {
                    PetSpiderSettingsManager.setConcreteColor(player, ConcreteColor.DIAMOND)
                    player.sendMessage("§a§lBody color set to §bDiamond")
                    openMenu(player) // Refresh menu
                } else {
                    // Check if player has 64 diamond blocks
                    val diamondCount = player.inventory.contents?.filterNotNull()
                        ?.sumOf { if (it.type == Material.DIAMOND_BLOCK) it.amount else 0 } ?: 0
                    
                    if (diamondCount >= 64) {
                        // Remove 64 diamond blocks and unlock the skin
                        var remainingToRemove = 64
                        player.inventory.contents?.filterNotNull()?.forEach { item ->
                            if (remainingToRemove > 0 && item.type == Material.DIAMOND_BLOCK) {
                                val toRemove = minOf(item.amount, remainingToRemove)
                                item.amount -= toRemove
                                remainingToRemove -= toRemove
                            }
                        }
                        
                        if (PetSpiderSettingsManager.unlockSkin(player, ConcreteColor.DIAMOND)) {
                            player.sendMessage("§a§lDiamond skin unlocked!")
                            player.sendMessage("§7Removed 64 Diamond blocks from your inventory")
                            player.sendMessage("§b§lBody color set to §bDiamond")
                            PetSpiderSettingsManager.setConcreteColor(player, ConcreteColor.DIAMOND)
                        }
                        openMenu(player) // Refresh menu
                    } else {
                        player.sendMessage("§c§lYou need 64 Diamond blocks to unlock this skin!")
                        player.sendMessage("§7You currently have: $diamondCount/64 Diamond blocks")
                    }
                }
            }
        }
    }
}

class ConcreteColorMenuListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        ConcreteColorMenu.handleClick(event)
    }
}
