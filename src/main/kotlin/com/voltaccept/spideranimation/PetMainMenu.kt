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
    private const val MENU_TITLE = "§6§lSP1D.3R Menu"
    private const val SPAWN_SLOT = 11
    private const val DESPAWN_SLOT = 13
    private const val SETTINGS_SLOT = 15
    private const val FUEL_SLOT = 22
    private const val BACK_SLOT = 26

    fun openMenu(player: Player) {
        val inventory = Bukkit.createInventory(null, 27, MENU_TITLE)
        
        val hasSpider = PetSpiderManager.hasSpider(player)
        
        // Spawn button
        val spawnItem = if (!hasSpider) {
            ItemStack(Material.SPIDER_EYE).apply {
                val meta = itemMeta!!
                meta.setDisplayName("§a§lActivate SP1D.3R")
                meta.lore = listOf(
                    "§7Click to activate your SP1D.3R!",
                    "§7Your SP1D.3R will follow you around."
                )
                itemMeta = meta
            }
        } else {
            ItemStack(Material.BARRIER).apply {
                val meta = itemMeta!!
                meta.setDisplayName("§c§lSP1D.3R Already Active")
                meta.lore = listOf(
                    "§7You already have an active SP1D.3R.",
                    "§7Deactivate it first to activate a new one."
                )
                itemMeta = meta
            }
        }
        
        // Despawn button
        val despawnItem = if (hasSpider) {
            ItemStack(Material.COBWEB).apply {
                val meta = itemMeta!!
                meta.setDisplayName("§c§lDeactivate SP1D.3R")
                meta.lore = listOf(
                    "§7Click to deactivate your SP1D.3R."
                )
                itemMeta = meta
            }
        } else {
            ItemStack(Material.GRAY_DYE).apply {
                val meta = itemMeta!!
                meta.setDisplayName("§7§lNo SP1D.3R Active")
                meta.lore = listOf(
                    "§7You don't have an active SP1D.3R.",
                    "§7Use /ixr to activate a new one."
                )
                itemMeta = meta
            }
        }
        
        // Settings button
        val settingsItem = ItemStack(Material.WRITABLE_BOOK).apply {
            val meta = itemMeta!!
            meta.setDisplayName("§6§lSettings")
            meta.lore = listOf(
                "§7Configure your SP1D.3R appearance",
                "§7• Leg count",
                "§7• Body color",
                "§7• Eye color"
            )
            itemMeta = meta
        }
        
        // Fuel display - always show from saved settings
        val currentFuel = PetSpiderSettingsManager.getSpiderFuel(player)
        val fuelPercentage = currentFuel
        val fuelMaterial = when {
            fuelPercentage > 66 -> Material.REDSTONE_BLOCK
            fuelPercentage > 33 -> Material.REDSTONE
            fuelPercentage > 0 -> Material.COAL
            else -> Material.GUNPOWDER
        }
        
        val fuelItem = if (hasSpider) {
            val ecsEntity = PetSpiderManager.getSpider(player)!!
            val body = ecsEntity.query<SpiderBody>()
            if (body != null) {
                // Show real-time fuel from active spider
                val realFuelPercentage = (body.fuel.toDouble() / body.maxFuel * 100).toInt()
                val realFuelMaterial = when {
                    realFuelPercentage > 66 -> Material.REDSTONE_BLOCK
                    realFuelPercentage > 33 -> Material.REDSTONE
                    realFuelPercentage > 0 -> Material.COAL
                    else -> Material.GUNPOWDER
                }
                ItemStack(realFuelMaterial).apply {
                    val meta = itemMeta!!
                    meta.setDisplayName("§b§lSP1D.3R's Fuel (Active)")
                    meta.lore = listOf(
                        "§7${body.fuel} / ${body.maxFuel} Fuel", 
                        "§7${realFuelPercentage}% Charged",
                        "",
                        "§7§oReal-time fuel from active SP1D.3R"
                    )
                    itemMeta = meta
                }
            } else {
                // spider registered but no body component yet
                ItemStack(fuelMaterial).apply {
                    val meta = itemMeta!!
                    meta.setDisplayName("§b§lSP1D.3R's Fuel (Initializing)")
                    meta.lore = listOf(
                        "§7$currentFuel / 100 Fuel", 
                        "§7$fuelPercentage% Charged",
                        "",
                        "§7§oSP1D.3R is initializing..."
                    )
                    itemMeta = meta
                }
            }
        } else {
            // No active spider - show saved fuel
            ItemStack(fuelMaterial).apply {
                val meta = itemMeta!!
                meta.setDisplayName("§b§lSP1D.3R's Fuel (Inactive)")
                meta.lore = listOf(
                    "§7$currentFuel / 100 Fuel", 
                    "§7$fuelPercentage% Charged",
                    "",
                    "§7§oNo active SP1D.3R - showing saved fuel"
                )
                itemMeta = meta
            }
        }
        
        inventory.setItem(SPAWN_SLOT, spawnItem)
        inventory.setItem(DESPAWN_SLOT, despawnItem)
        inventory.setItem(SETTINGS_SLOT, settingsItem)
        inventory.setItem(FUEL_SLOT, fuelItem)
        inventory.setItem(BACK_SLOT, ItemStack(Material.ARROW).apply {
            val meta = itemMeta!!
            meta.setDisplayName("§e§lClose")
            meta.lore = listOf("§7Close this menu")
            itemMeta = meta
        })
        
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
        val inventory = event.inventory
        
        if (event.view.title != MENU_TITLE) return
        
        event.isCancelled = true
        
        when (event.slot) {
            SPAWN_SLOT -> {
                if (!PetSpiderManager.hasSpider(player)) {
                    val location = player.location.clone()
                    location.y += 1.0
                    AppState.createSpider(location, player)
                    player.sendMessage("§a§lSP1D.3R activated! §7It will follow you around.")
                    player.closeInventory()
                } else {
                    player.sendMessage("§c§lSP1D.3R is already active! §7Deactivate it first.")
                }
            }
            DESPAWN_SLOT -> {
                if (PetSpiderManager.hasSpider(player)) {
                    // Save spider fuel before despawning
                    val spider = PetSpiderManager.getSpider(player)
                    if (spider != null) {
                        val spiderBody = spider.query<com.voltaccept.spideranimation.spider.components.body.SpiderBody>()
                        if (spiderBody != null) {
                            PetSpiderSettingsManager.saveSpiderFuel(player, spiderBody.fuel)
                        }
                    }
                    PetSpiderManager.removeSpider(player)
                    player.sendMessage("§c§lSP1D.3R deactivated!")
                    player.closeInventory()
                } else {
                    player.sendMessage("§7You don't have an active SP1D.3R.")
                }
            }
            SETTINGS_SLOT -> {
                com.voltaccept.spideranimation.menus.SpiderSettingsMenu.openMenu(player)
            }
            BACK_SLOT -> {
                player.closeInventory()
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
