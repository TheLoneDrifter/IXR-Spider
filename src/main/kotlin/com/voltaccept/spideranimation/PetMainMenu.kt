package com.voltaccept.spideranimation

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import com.voltaccept.spideranimation.PetSpiderManager
import com.voltaccept.spideranimation.spider.components.body.SpiderBody
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.inventory.ItemStack

object PetMainMenu {
    private const val MENU_TITLE = "§6§lPets Menu - Main"
    private const val SPIDER_SLOT = 4
    private const val SPIDER_FOOD_SLOT = 1

    fun openMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 9, MENU_TITLE)

        val spiderItem = ItemStack(Material.SPIDER_EYE).apply {
            val meta = itemMeta!!
            meta.setDisplayName("§a§lSP1D.3R")
            meta.lore = listOf("§7Open SP1D.3R menu")
            itemMeta = meta
        }

        // Fuel display for player's currently spawned spider (or placeholder)
        val fuelItem = if (PetSpiderManager.hasSpider(player)) {
            val ecsEntity = PetSpiderManager.getSpider(player)!!
            val body = ecsEntity.query<SpiderBody>()
            if (body != null) {
                val fuelPercentage = (body.fuel.toDouble() / body.maxFuel * 100).toInt()
                val fuelMaterial = when {
                    fuelPercentage > 66 -> Material.REDSTONE_BLOCK
                    fuelPercentage > 33 -> Material.REDSTONE
                    fuelPercentage > 0 -> Material.COAL
                    else -> Material.GUNPOWDER
                }
                ItemStack(fuelMaterial).apply {
                    val meta = itemMeta!!
                    meta.setDisplayName("§b§lSP1D.3R's Fuel")
                    meta.lore = listOf("§7${body.fuel} / ${body.maxFuel} Fuel", "§7${fuelPercentage}% Charged")
                    itemMeta = meta
                }
            } else {
                // spider registered but no body component yet
                ItemStack(Material.GRAY_DYE).apply {
                    val meta = itemMeta!!
                    meta.setDisplayName("§7§lSP1D.3R's Fuel")
                    meta.lore = listOf("§7Activated but not initialized")
                    itemMeta = meta
                }
            }
        } else {
            ItemStack(Material.GRAY_DYE).apply {
                val meta = itemMeta!!
                meta.setDisplayName("§7§lNo SP1D.3R Active")
                meta.lore = listOf("§7You don't have an active SP1D.3R.")
                itemMeta = meta
            }
        }

        inventory.setItem(SPIDER_FOOD_SLOT, fuelItem)
        inventory.setItem(SPIDER_SLOT, spiderItem)

        player.openInventory(inventory)
    }

    fun handleClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (event.view.title != MENU_TITLE) return
        event.isCancelled = true

        when (event.slot) {
            SPIDER_SLOT, SPIDER_FOOD_SLOT -> {
                PetSpiderMenu.openMenu(player)
            }
        }
    }
}

class PetMainMenuListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        PetMainMenu.handleClick(event)
    }
}
