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

    // RoboFuel command - buy fuel with coins
    getCommand("robofuel").apply {
        setExecutor { sender, _, _, args ->
            val player = sender as? Player
            if (player == null) {
                sender.sendMessage("§cThis command can only be used by players!")
                return@setExecutor true
            }

            if (args.isEmpty()) {
                player.sendMessage("§c/robofuel <amount> - Costs 10 coins per fuel")
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

            // Calculate the cost
            val costPerFuel = 10.0
            val totalCost = fuelAmount * costPerFuel

            // Check if player has enough money
            if (!economy.has(player, totalCost)) {
                val balance = economy.getBalance(player)
                player.sendMessage("§cInsufficient funds! You need §b§l${totalCost - balance} more coins§c to complete this purchase.")
                player.sendMessage("§7Current balance: §b§l${balance}§7 coins")
                return@setExecutor true
            }

            // Withdraw the money
            val result = economy.withdrawPlayer(player, totalCost)
            if (!result.transactionSuccess()) {
                player.sendMessage("§cTransaction failed! Please try again.")
                return@setExecutor true
            }

            // Check if player has a spider and add fuel
            val spider = PetSpiderManager.getSpider(player)
            if (spider != null) {
                val body = spider.query<SpiderBody>()
                if (body != null) {
                    body.refuel(fuelAmount)
                    player.world.playSound(player.location, org.bukkit.Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f)
                    player.sendMessage("§b§lRobo Fuel Purchase Successful!")
                    player.sendMessage("§7Purchased: §b§l+$fuelAmount§7 fuel for §b§l${totalCost.toInt()}§7 coins")
                    player.sendMessage("§7New balance: §b§l${economy.getBalance(player).toInt()}§7 coins")
                } else {
                    // Refund if spider has no body
                    economy.depositPlayer(player, totalCost)
                    player.sendMessage("§cYour spider is missing its body component! Purchase refunded.")
                }
            } else {
                // Refund if player has no active spider
                economy.depositPlayer(player, totalCost)
                player.sendMessage("§cYou don't have an active spider! Purchase refunded. Use /pets to create one.")
            }

            return@setExecutor true
        }

        setTabCompleter { _, _, _, args ->
            if (args.size == 1) {
                listOf("10", "25", "50", "100")
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
