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

    // RoboFuel command - buy fuel with coins (5 fuel per purchase)
    getCommand("robofuel").apply {
        setExecutor { sender, _, _, args ->
            val player = sender as? Player
            if (player == null) {
                sender.sendMessage("§cThis command can only be used by players!")
                return@setExecutor true
            }

            if (args.isEmpty()) {
                player.sendMessage("§c/robofuel <amount> - Buy robo fuel (5 fuel per purchase for 10 coins)")
                return@setExecutor true
            }

            val purchaseCount = args[0].toIntOrNull()
            if (purchaseCount == null || purchaseCount <= 0) {
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

            // Calculate fuel and cost (5 fuel per purchase, costs 10 coins)
            val fuelPerPurchase = 5
            val costPerPurchase = 10.0
            val totalFuel = fuelPerPurchase * purchaseCount
            val totalCost = costPerPurchase * purchaseCount
            
            // Limit to max fuel
            val actualFuel = (body.fuel + totalFuel).coerceAtMost(body.maxFuel) - body.fuel

            // Check if player has enough money
            if (!economy.has(player, totalCost)) {
                val balance = economy.getBalance(player)
                val needed = totalCost - balance
                player.sendMessage("§cInsufficient funds! You need §b§l$needed more coins§c to complete this purchase.")
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

        setTabCompleter { _, _, _, args ->
            if (args.size == 1) {
                listOf("1", "2", "5", "10")
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
