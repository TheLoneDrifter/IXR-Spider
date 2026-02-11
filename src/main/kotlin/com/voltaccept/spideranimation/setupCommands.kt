package com.voltaccept.spideranimation

import com.voltaccept.spideranimation.spider.components.splay
import com.voltaccept.spideranimation.spider.components.body.SpiderBody
import com.voltaccept.spideranimation.spider.presets.*
import com.voltaccept.spideranimation.utilities.custom_items.setupCustomItemCommand
import com.voltaccept.spideranimation.utilities.events.runLater
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

fun setupCommands(plugin: SpiderAnimationPlugin) {
    fun getCommand(name: String) = plugin.getCommand(name) ?: throw Exception("Command $name not found")

    getCommand("pets").apply {
        setExecutor { sender, _, _, _ ->
            val player = sender as? Player
            if (player == null) {
                sender.sendMessage("§cThis command can only be used by players!")
                return@setExecutor true
            }

            // Always open the main pets menu for `/pets`
            PetMainMenu.openMenu(player)
            return@setExecutor true
        }

        setTabCompleter { _, _, _, _ ->
            // No subcommands to suggest
            return@setTabCompleter emptyList<String>()
        }
    }

    // RoboFuel command - buy fuel with coins (1 fuel for 1 coin)
    getCommand("robofuel").apply {
        setExecutor { sender, _, _, args ->
            val player = sender as? Player
            if (player == null) {
                sender.sendMessage("§cThis command can only be used by players!")
                return@setExecutor true
            }

            if (args.isEmpty()) {
                player.sendMessage("§c/robofuel <amount> - Buy robo fuel (2.55 coins per fuel)")
                return@setExecutor true
            }

            val fuelAmount = args[0].toIntOrNull()
            if (fuelAmount == null || fuelAmount <= 0) {
                player.sendMessage("§cPlease enter a valid positive number!")
                return@setExecutor true
            }

            // Get Vault economy
            val economy = getEconomy()
            if (economy == null) {
                player.sendMessage("§cEconomy system is not available!")
                return@setExecutor true
            }

            // Check if player has a spider first
            val spider = PetSpiderManager.getSpider(player)
            val body = spider?.query<SpiderBody>()
            
            if (spider == null || body == null) {
                player.sendMessage("§cYou don't have an active spider! Use /pets to create one.")
                return@setExecutor true
            }

            // Check if fuel is full
            if (body.fuel >= body.maxFuel) {
                player.sendMessage("§c§lYour spider's fuel is already full! (${body.fuel}/${body.maxFuel})")
                return@setExecutor true
            }

            // Calculate missing fuel
            val missingFuel = body.maxFuel - body.fuel
            
            // Limit to missing fuel
            val actualFuel = fuelAmount.coerceAtMost(missingFuel)
            
            // If requested more than missing fuel, tell them
            if (fuelAmount > missingFuel) {
                player.sendMessage("§c§lYou can only buy up to §b${missingFuel}§c fuel to fill your tank!")
            }
            
            // Cost is 2.55 coins per fuel
            val totalCost = actualFuel * 2.55

            // Check if player has enough money
            if (!economy.has(player, totalCost)) {
                val balance = economy.getBalance(player)
                val needed = totalCost - balance
                player.sendMessage("§cInsufficient funds! You need §b§l${needed.toInt()} more coins§c to complete this purchase.")
                player.sendMessage("§7Current balance: §b§l${balance.toInt()}§7 coins")
                return@setExecutor true
            }

            // Withdraw the money
            val result = economy.withdrawPlayer(player, totalCost)
            if (!result.transactionSuccess()) {
                player.sendMessage("§cTransaction failed! Please try again.")
                return@setExecutor true
            }

            // Add fuel to spider
            body.refuel(actualFuel)
            
            // Save fuel to YAML file
            com.voltaccept.spideranimation.utilities.FuelDataManager.savePlayerFuel(player, body.fuel)

            player.world.playSound(player.location, org.bukkit.Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f)
            player.sendMessage("§b§l⚡ Robo Fuel Purchase Successful!")
            player.sendMessage("§7Purchased: §b§l+$actualFuel§7 fuel (${body.fuel}/${body.maxFuel}) for §b§l${totalCost.toInt()}§7 coins")
            player.sendMessage("§7New balance: §b§l${economy.getBalance(player).toInt()}§7 coins")

            return@setExecutor true
        }

        setTabCompleter { sender, _, _, args ->
            if (args.size == 1) {
                val player = sender as? Player
                if (player != null) {
                    val spider = PetSpiderManager.getSpider(player)
                    val body = spider?.query<SpiderBody>()
                    if (body != null) {
                        val missingFuel = (body.maxFuel - body.fuel).coerceAtLeast(0)
                        (1..missingFuel).map { it.toString() }
                    } else {
                        emptyList<String>()
                    }
                } else {
                    emptyList<String>()
                }
            } else {
                emptyList<String>()
            }
        }
    }
}

private fun getEconomy(): Economy? {
    val vault = Bukkit.getPluginManager().getPlugin("Vault") ?: return null
    val economyProvider = Bukkit.getServicesManager().getRegistration(Economy::class.java) ?: return null
    return economyProvider.provider
}
