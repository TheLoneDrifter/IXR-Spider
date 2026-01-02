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
    private const val SPIDER_HEALTH_SLOT = 1

    fun openMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 9, MENU_TITLE)

        val spiderItem = ItemStack(Material.SPIDER_EYE).apply {
            val meta = itemMeta!!
            meta.setDisplayName("§a§lSP1D3.R")
            meta.lore = listOf("§7Open SP1D3.R menu")
            itemMeta = meta
        }

        // Health display for player's currently spawned spider (or placeholder)
        val healthItem = if (PetSpiderManager.hasSpider(player)) {
            val ecsEntity = PetSpiderManager.getSpider(player)!!
            val body = ecsEntity.query<SpiderBody>()
            if (body != null) {
                ItemStack(Material.REDSTONE).apply {
                    val meta = itemMeta!!
                    meta.setDisplayName("§c§lSP1D3.R's Health")
                    meta.lore = listOf("§7${body.health.toInt()} / ${body.maxHealth.toInt()} HP")
                    itemMeta = meta
                }
            } else {
                // spider registered but no body component yet
                ItemStack(Material.GRAY_DYE).apply {
                    val meta = itemMeta!!
                    meta.setDisplayName("§7§lSP1D3.R's Health")
                    meta.lore = listOf("§7Activated but not initialized")
                    itemMeta = meta
                }
            }
        } else {
            ItemStack(Material.GRAY_DYE).apply {
                val meta = itemMeta!!
                meta.setDisplayName("§7§lNo SP1D3.R Active")
                meta.lore = listOf("§7You don't have an active SP1D3.R.")
                itemMeta = meta
            }
        }

        inventory.setItem(SPIDER_HEALTH_SLOT, healthItem)
        inventory.setItem(SPIDER_SLOT, spiderItem)

        player.openInventory(inventory)
    }

    fun handleClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (event.view.title != MENU_TITLE) return
        event.isCancelled = true

        when (event.slot) {
            SPIDER_SLOT, SPIDER_HEALTH_SLOT -> {
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
