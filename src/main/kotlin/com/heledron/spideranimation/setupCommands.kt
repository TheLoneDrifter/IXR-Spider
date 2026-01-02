package com.heledron.spideranimation

import com.heledron.spideranimation.spider.components.splay
import com.heledron.spideranimation.spider.presets.*
import com.heledron.spideranimation.utilities.custom_items.setupCustomItemCommand
import com.heledron.spideranimation.utilities.events.runLater
import org.bukkit.entity.Player

fun setupCommands(plugin: SpiderAnimationPlugin) {
    fun getCommand(name: String) = plugin.getCommand(name) ?: throw Exception("Command $name not found")

    getCommand("pets").apply {
        setExecutor { sender, _, _, args ->
            val player = sender as? Player
            if (player == null) {
                sender.sendMessage("§cThis command can only be used by players!")
                return@setExecutor true
            }
            
            val subCommand = args.getOrNull(0)?.lowercase()
            
            if (subCommand != "spider") {
                player.sendMessage("§cUsage: /pets spider")
                return@setExecutor true
            }
            
            PetSpiderMenu.openMenu(player)
            
            return@setExecutor true
        }
        
        setTabCompleter { _, _, _, args ->
            if (args.size == 1) {
                return@setTabCompleter listOf("spider").filter { it.contains(args.last(), true) }
            }
            return@setTabCompleter emptyList()
        }
    }
}
