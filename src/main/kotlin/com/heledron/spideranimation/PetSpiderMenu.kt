package com.heledron.spideranimation

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object PetSpiderMenu {
    private const val MENU_TITLE = "§6§lPet Spider Menu"
    private const val SPAWN_SLOT = 3
    private const val DESPAWN_SLOT = 5
    
    fun openMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 9, MENU_TITLE)
        
        val hasSpider = PetSpiderManager.hasSpider(player)
        
        // Spawn button
        val spawnItem = if (!hasSpider) {
            ItemStack(Material.SPIDER_EYE).apply {
                val meta = itemMeta!!
                meta.setDisplayName("§a§lSpawn Spider")
                meta.lore = listOf(
                    "§7Click to spawn your pet spider!",
                    "§7Your spider will follow you around."
                )
                itemMeta = meta
            }
        } else {
            ItemStack(Material.BARRIER).apply {
                val meta = itemMeta!!
                meta.setDisplayName("§c§lSpider Already Active")
                meta.lore = listOf(
                    "§7You already have a spider spawned.",
                    "§7Despawn it first to spawn a new one."
                )
                itemMeta = meta
            }
        }
        
        // Despawn button
        val despawnItem = if (hasSpider) {
            ItemStack(Material.COBWEB).apply {
                val meta = itemMeta!!
                meta.setDisplayName("§c§lDespawn Spider")
                meta.lore = listOf(
                    "§7Click to despawn your pet spider."
                )
                itemMeta = meta
            }
        } else {
            ItemStack(Material.GRAY_DYE).apply {
                val meta = itemMeta!!
                meta.setDisplayName("§7§lNo Spider Active")
                meta.lore = listOf(
                    "§7You don't have a spider spawned."
                )
                itemMeta = meta
            }
        }
        
        inventory.setItem(SPAWN_SLOT, spawnItem)
        inventory.setItem(DESPAWN_SLOT, despawnItem)
        
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
                    player.sendMessage("§a§lSpider spawned! §7It will follow you around.")
                    player.closeInventory()
                } else {
                    player.sendMessage("§c§lYou already have a spider! §7Despawn it first.")
                }
            }
            DESPAWN_SLOT -> {
                if (PetSpiderManager.hasSpider(player)) {
                    PetSpiderManager.removeSpider(player)
                    player.sendMessage("§c§lSpider despawned!")
                    player.closeInventory()
                } else {
                    player.sendMessage("§7You don't have an active spider.")
                }
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
