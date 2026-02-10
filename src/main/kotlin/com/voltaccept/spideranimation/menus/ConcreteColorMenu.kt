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
        ConcreteColor.WHITE to 14
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
            }
            
            val isSelected = concreteColor == currentConcreteColor
            val itemStack = ItemStack(material).apply {
                val meta = itemMeta!!
                val colorName = when (concreteColor) {
                    ConcreteColor.BLACK -> "§f§lBlack"
                    ConcreteColor.WHITE -> "§f§lWhite"
                }
                meta.setDisplayName(colorName)
                
                val lore = mutableListOf<String>()
                if (isSelected) {
                    lore.add("§a§l✓ Selected")
                    lore.add("")
                }
                lore.add("§7Click to select")
                
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
        }
    }
}

class ConcreteColorMenuListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        ConcreteColorMenu.handleClick(event)
    }
}
