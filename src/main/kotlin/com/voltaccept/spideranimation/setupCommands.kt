package com.voltaccept.spideranimation

import com.voltaccept.spideranimation.spider.components.splay
import com.voltaccept.spideranimation.spider.presets.*
import com.voltaccept.spideranimation.utilities.custom_items.setupCustomItemCommand
import com.voltaccept.spideranimation.utilities.events.runLater
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
}
