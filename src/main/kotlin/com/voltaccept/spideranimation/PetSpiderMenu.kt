package com.voltaccept.spideranimation

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object PetSpiderMenu {
    private const val MENU_TITLE = "§6§lPets Menu - SP1D3.R"
    private const val SPAWN_SLOT = 3
    private const val DESPAWN_SLOT = 5
    private const val BACK_SLOT = 8
    
    fun openMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 9, MENU_TITLE)
        
        val hasSpider = PetSpiderManager.hasSpider(player)
        
        // Spawn button
        val spawnItem = if (!hasSpider) {
            ItemStack(Material.SPIDER_EYE).apply {
                val meta = itemMeta!!
                meta.setDisplayName("§a§lActivate SP1D3.R")
                meta.lore = listOf(
                    "§7Click to activate your SP1D3.R!",
                    "§7Your SP1D3.R will follow you around."
                )
                itemMeta = meta
            }
        } else {
            ItemStack(Material.BARRIER).apply {
                val meta = itemMeta!!
                meta.setDisplayName("§c§lSP1D3.R Already Active")
                meta.lore = listOf(
                    "§7You already have an active SP1D3.R.",
                    "§7Deactivate it first to activate a new one."
                )
                itemMeta = meta
            }
        }
        
        // Despawn button
        val despawnItem = if (hasSpider) {
            ItemStack(Material.COBWEB).apply {
                val meta = itemMeta!!
                meta.setDisplayName("§c§lDeactivate SP1D3.R")
                meta.lore = listOf(
                    "§7Click to deactivate your SP1D3.R."
                )
                itemMeta = meta
            }
        } else {
            ItemStack(Material.GRAY_DYE).apply {
                val meta = itemMeta!!
                meta.setDisplayName("§7§lNo SP1D3.R Active")
                meta.lore = listOf(
                    "§7You don't have an active SP1D3.R.",
                    "§7Use /pets to activate a new one."
                )
                itemMeta = meta
            }
        }
        
        inventory.setItem(SPAWN_SLOT, spawnItem)
        inventory.setItem(DESPAWN_SLOT, despawnItem)
        inventory.setItem(BACK_SLOT, ItemStack(Material.ARROW).apply {
            val meta = itemMeta!!
            meta.setDisplayName("§e§lBack")
            meta.lore = listOf("§7Return to pets list")
            itemMeta = meta
        })
        
        player.openInventory(inventory)
    }
    
    fun handleClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val inventory = event.inventory
        
        if (event.view.title != MENU_TITLE) return
        
        event.isCancelled = true
        
        when (event.slot) {
            SPAWN_SLOT -> {
                if (!PetSpiderManager.hasSpider(player)) {
                    val location = player.location.clone()
                    location.y += 1.0
                    AppState.createSpider(location, player)
                    player.sendMessage("§a§lSP1D3.R activated! §7It will follow you around.")
                    player.closeInventory()
                } else {
                    player.sendMessage("§c§lSP1D3.R is already active! §7Deactivate it first.")
                }
            }
            DESPAWN_SLOT -> {
                if (PetSpiderManager.hasSpider(player)) {
                    PetSpiderManager.removeSpider(player)
                    player.sendMessage("§c§lSP1D3.R deactivated!")
                    player.closeInventory()
                } else {
                    player.sendMessage("§7You don't have an active SP1D3.R.")
                }
            }
            BACK_SLOT -> {
                PetMainMenu.openMenu(player)
            }
        }
    }
}

class PetSpiderMenuListener : Listener {
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        PetSpiderMenu.handleClick(event)
    }
}

class PetSpiderPlayerListener : Listener {
    @EventHandler
    fun onPlayerQuit(event: org.bukkit.event.player.PlayerQuitEvent) {
        PetSpiderManager.removeSpider(event.player)
    }

    @EventHandler
    fun onPlayerKick(event: org.bukkit.event.player.PlayerKickEvent) {
        PetSpiderManager.removeSpider(event.player)
    }
}

