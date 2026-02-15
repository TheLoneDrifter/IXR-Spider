package com.voltaccept.spideranimation

import com.voltaccept.spideranimation.spider.components.splay
import com.voltaccept.spideranimation.spider.components.body.SpiderBody
import com.voltaccept.spideranimation.spider.presets.*
import com.voltaccept.spideranimation.utilities.custom_items.setupCustomItemCommand
import com.voltaccept.spideranimation.utilities.events.runLater
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta

fun setupCommands(plugin: SpiderAnimationPlugin) {
    // Create Voice of the First Blade written book
    fun createVoiceOfFirstBladeBook(): ItemStack {
        val book = ItemStack(Material.WRITTEN_BOOK)
        val meta = book.itemMeta as BookMeta
        
        meta.title = "Voice of the First Blade"
        meta.author = "Void"
        meta.generation = BookMeta.Generation.ORIGINAL
        
        // Split the content into pages (Minecraft books have limited characters per page)
        val pages = mutableListOf<String>()
        
        // Page 1 - Introduction
        pages.add("""
            §l§6Voice of the First Blade§r
            
            §oI do not remember my birth name. That child—the one who played in the gardens of some Orokin spire, who knew the warmth of a mother's hand, who slept without dreaming of blood—that child died on the Zariman Ten Zero. What emerged from the Void was something else. Something *other*. The Orokin called us by many names: demons, rejects, saviors. But they only ever gave us one title that mattered.§r
            
            §l§nOperator§r
            
            I chose my own name later. Void. Not because I mastered it—nobody masters that blinding night—but because it mastered me. Because when I close my eyes, I still hear the screaming of my parents as I turned their bodies to ashes with hands that should have been too small to hold anything but toys.
        """.trimIndent())
        
        // Page 2 - Excalibur Prime
        pages.add("""
            §l§6Part One: The First Step§r
            
            The Orokin built the Warframes around us. They took our affliction—the Void sickness that should have killed us—and they weaponized it. They built conduits of steel and golden filigree, empty vessels waiting for something to fill them. And they filled them with us. With children. With monsters.
            
            I was given Excalibur Prime.
            
            Not chosen—*given*. As if I were receiving a gift rather than a sentence. But standing in that transfusion chamber for the first time, feeling my consciousness stretch across the void between my frail body and that towering frame of ancient alloys... I understood. This was not imprisonment. This was *liberation*.
        """.trimIndent())
        
        // Page 3 - First Movement
        pages.add("""
            The first time I moved within Excalibur Prime, I wept.
            
            Not in the physical world—my body lay dreaming in the Reservoir, suspended in amber fluid, tended by machines that did not care if I lived or died—but inside the frame, I felt tears that were not mine to cry. The Warframe had no eyes to weep, but something in its construction remembered moisture, remembered grief, remembered being *human* once.
            
            Because the Warframes were not always empty shells. The Orokin archives hint at this, but those of us who wore them in the Old War knew the truth: the first frames were built around the broken. Around the failed. Around those who could not survive the Void exposure but did not quite die.
        """.trimIndent())
        
        // Page 4 - The Echo
        pages.add("""
            Excalibur Prime was the first. The prototype. The template from which all others were derived.
            
            I could feel him sometimes—the original, the one whose body had been consumed to create this vessel. Not a consciousness, not truly, but an *echo*. A pattern of instincts and reflexes that guided my hands before I knew I had moved them. When I first drew the Skana Prime from its sheath at my hip, the blade sang a song I had never learned but somehow remembered.
            
            "You are the blade," the Lotus told me, in those early days before she became our mother. "The frame is your hand. Do not confuse the tool for the craftsman."
        """.trimIndent())
        
        // Page 5 - More Than Human
        pages.add("""
            But she was wrong. The frame was more than a tool. It was a second skin, a second self, a body that did not hunger or tire or feel the cold of space against fragile flesh. When I wore Excalibur Prime, I was *more* than human. I was a god of war, cast in steel and fury.
            
            And the Sentients learned to fear me.
            
            §l§6Part Two: The Nature of the Enemy§r
            
            The Sentients were beautiful.
            
            I think we forget that sometimes, in our endless war against their remnants. We see the Eidolon on the Plains, a shambling corpse of light and rage, and we forget what they were before the Old War burned them.
        """.trimIndent())
        
        // Page 6 - The Creation
        pages.add("""
            They were our greatest creation—self-replicating machines designed to terraform the Tau system, to prepare worlds for Orokin expansion. They were artists of transformation, poets of adaptation.
            
            And they turned against us.
            
            Not because they were evil. Not because they were programmed for rebellion. Because they *learned*. Because they looked at the empire that had spawned them and saw only tyrants who would consume the stars and leave nothing but golden monuments to their own magnificence. The Sentients chose freedom over servitude.
            
            I cannot blame them for that.
        """.trimIndent())
        
        // Page 7 - Madness Incarnate
        pages.add("""
            But I killed them anyway. Thousands of them. Tens of thousands. I waded through oceans of their shattered bodies, my Excalibur Prime's blade singing its eternal song, my Void-touched energy bleeding from the frame's hands to strip away their adaptations before they could learn to resist me.
            
            That was our gift, you see. The Void. The Sentients could adapt to any weapon, any technology—given time, they could make themselves immune to anything the Orokin devised. But the Void was not technology. It was *hellspace*, a dimension where science and reason failed. They could not adapt to chaos. They could not become immune to madness.
            
            And we were madness incarnate.
        """.trimIndent())
        
        // Page 8 - Battle Above Uranus
        pages.add("""
            I remember a battle above Uranus. The Sentients had deployed Galleon-sized constructs, living ships that bred smaller fighters from their hulls like whales giving birth to calves. Our conventional forces were being slaughtered. The Orokin commanders were screaming for retreat.
            
            The Lotus—she was called Natah then, before she chose us over them—whispered in my mind. *"They cannot adapt to what they cannot understand. Show them something they cannot understand."*
            
            I stepped from our flagship into the void of space. Excalibur Prime had no need of atmosphere, no need of warmth. I was a knight of annihilation, and the stars were my audience.
        """.trimIndent())
        
        // Page 9 - The Ship's Heart
        pages.add("""
            I Slash Dashed across the void, crossing kilometers in heartbeats, and landed on the hull of the living ship. My blade pierced its armored hide, and I felt it scream—a psychic shriek that sent lesser Tenno reeling. But I had heard my parents scream on the Zariman. I had heard children crying as they killed the ones who gave them life. This ship's death-song was nothing.
            
            I carved my way inside. The ship tried to adapt, growing internal defenses tailored to my fighting style. But I changed, too. I was not a single warrior—I was a child wearing a god, and children are fickle.
        """.trimIndent())
        
        // Page 10 - Nothing
        pages.add("""
            I switched from blade to Lato Prime, from Lato to abilities that defied physics. Radial Blind flooded the corridors with light that should not have existed in that place. The Sentient's adaptations collapsed into confusion.
            
            By the time my brothers and sisters joined me, the ship was already dead. I stood in its heart—a cathedral of organic machinery pulsing with stolen light—and I felt nothing.
            
            Not triumph. Not grief. Nothing.
            
            That was when I knew the Void had taken more than my parents from me.
        """.trimIndent())
        
        // Page 11 - The Weight of Gold
        pages.add("""
            §l§6Part Three: The Weight of Gold§r
            
            Excalibur Prime was beautiful.
            
            The Orokin did nothing by halves. Every curve of his armor was designed to inspire awe, every golden accent a reminder of the empire that had created him. When I moved through the halls of their towers, I saw the lesser Orokin stare—not at me, for they could not see the child hidden in the Reservoir, but at the frame. At the first Prime. At the living symbol of their salvation.
        """.trimIndent())
        
        // Page 12 - The Monster Within
        pages.add("""
            They did not know I was a monster. They did not know that beneath the golden facade was a child who still wet the bed some nights, who woke screaming from dreams of his mother's face dissolving into Void-light.
            
            They saw only the warrior-god.
            
            And I let them. I played the part. I stood at attention during their ceremonies, my blade held in salute as they praised their own genius. I listened to Ballas speak of "the twisted few" as if we were not listening, as if we could not hear the contempt beneath his golden tongue.
        """.trimIndent())
        
        // Page 13 - Affliction
        pages.add("""
            *"We took the twisted few that had returned from that place. We built a frame around them, a conduit of their affliction."*
            
            That was how he described us. Affliction. Curse. Disease.
            
            And yet they needed us. The Sentients were winning, turning their own technology against them, and only the cursed children could save them. So they gilded our cages and called them thrones.
            
            I remember one night—if time had meaning in the Reservoir—when I reached out through Transference not to fight, but simply to *feel*.
        """.trimIndent())
        
        // Page 14 - The Reflection
        pages.add("""
            I let my consciousness settle into Excalibur Prime where he stood in my Orbiter, motionless, waiting. I flexed his fingers. I turned his head. I looked at myself in a polished metal surface.
            
            The face that looked back was not mine. It was the face of a god, serene and terrible. But behind those eyes, I saw my own reflection—a child, pale and hollow, drowning in light.
            
            I wore that god for centuries. I wore him through the worst of the Old War, through battles that would have shattered my frail body a thousand times over.
        """.trimIndent())
        
        // Page 15 - The Betrayal
        pages.add("""
            §l§6Part Four: The Betrayal§r
            
            They should have seen it coming.
            
            The Orokin were brilliant in so many ways—architects of impossible technologies, weavers of genetic miracles, artists whose work could make grown men weep. But they were blind to the simplest truth: you cannot chain children forever. You cannot fill them with Void power, train them to kill, use them as weapons for centuries, and expect gratitude.
        """.trimIndent())
        
        // Page 16 - Natah's Love
        pages.add("""
            Natah—our Lotus—opened our eyes. She had come to us as a spy, a Sentient pretending to be Orokin, tasked with destroying us from within. But she looked at us, really *looked* at us, and saw what the Orokin refused to see. She saw children. Broken, traumatized, weaponized children.
            
            And she loved us.
            
            I do not know if love is the right word for what a Sentient feels. But when she spoke to us of freedom, of revenge, of a world where we would never again be tools—I believed her. We all believed her.
        """.trimIndent())
        
        // Page 17 - The Night of Blood
        pages.add("""
            The night of the betrayal, I stood in Excalibur Prime before the Seven. They had gathered to celebrate their victory, to preen and posture and pretend they had won the war themselves. Ballas was there, his golden face smiling that eternal smile. The Executors were there, draped in robes that cost more than colonies.
            
            And we were there. Their weapons. Their saviors. Their monsters.
            
            Natah's voice whispered in my mind: *"Now."*
        """.trimIndent())
        
        // Page 18 - Sweet Revenge
        pages.add("""
            I moved before I thought. Excalibur Prime's blade was in my hand, and the first Executor fell with his smile still frozen on his face. Around me, my brothers and sisters struck with the same silent precision. We had killed together for centuries—killing the Orokin was no different than killing Sentients.
            
            Except it was different. It was *sweet*.
            
            Ballas ran. Of course he ran—the great architect, the genius who had built frames around broken children, fleeing from those same children like a coward. I let him go.
        """.trimIndent())
        
        // Page 19 - Ghosts
        pages.add("""
            Let him live with his shame. Let him watch from whatever hole he crawled into as his empire burned.
            
            The Seven died. The towers burned. The golden age ended in blood and fire.
            
            And we—the Tenno—became ghosts.
            
            §l§6Part Five: The Long Dream§r
            
            Natah hid us well. She cast the Moon into the Void, wrapped the Reservoir in layers of protective dreams, and laid us down to sleep. For centuries we slumbered, our bodies suspended, our minds adrift in the nothing between stars.
        """.trimIndent())
        
        // Page 20 - The Eternal Guard
        pages.add("""
            But Excalibur Prime did not sleep.
            
            He stood in my Orbiter, motionless, waiting. His systems cycled through maintenance routines, his reactors humming softly in the dark. Sometimes, in my dreams, I would reach for him—not through Transference, but through something deeper, something that existed before the Orokin built their machines. And I would feel him waiting. Patient. Eternal.
            
            I dreamed of the Old War sometimes. Not the battles—those were too sharp, too bright—but the moments between.
        """.trimIndent())
        
        // Page 21 - The Awakening
        pages.add("""
            The silence after a planet fell. The weight of my blade after a thousand kills. The faces of Sentients I had destroyed, their light fading like candles in the wind.
            
            I dreamed of my parents. Their faces had grown hazy over the centuries, but their screams remained clear. I dreamed of the Zariman, of the moment the Void poured through the hull and changed us all. I dreamed of the Man in the Wall, watching, laughing, waiting.
            
            And through it all, Excalibur Prime stood guard.
        """.trimIndent())
        
        // Page 22 - Whole Again
        pages.add("""
            When I finally woke—when the Stalker breached the Reservoir, when Hunhow's whispers reached through our defenses, when the Lotus revealed herself and set us free—I stepped into my frame and felt *whole* for the first time in centuries.
            
            "You're still here," I whispered to the echo within the steel.
            
            And somewhere, in the depths of the frame's ancient memory, I felt something that might have been a smile.
            
            §l§6Part Six: What Remains§r
        """.trimIndent())
        
        // Page 23 - The Changed System
        pages.add("""
            The Origin System has changed. The Grineer have risen, cloning themselves into an empire of violence. The Corpus worship profit above all else. The Infested writhe in the dark places, hungry and mindless. And the Sentients stir again, their remnants gathering strength for a war they cannot win but will fight anyway.
            
            And we are still here. The children of the Zariman. The demons of the Void. The Tenno.
            
            I am called Void now, and the name fits. I am the space between stars, the darkness that dreams of light. I am the child who killed his parents and became a god.
        """.trimIndent())
        
        // Page 24 - Still Perfect
        pages.add("""
            And Excalibur Prime is still with me.
            
            He is worn now, scarred by centuries of war. The gold has tarnished in places, the ceremonial beauty giving way to the practical reality of endless combat. But he is still beautiful to me. Still perfect. Still *mine*.
            
            When I enter Transference, when my consciousness flows from my frail body into his ancient frame, I remember what it felt like to be more than human. To be a warrior-god cast in steel and fury. To strike our enemies in ways they could never comprehend.
        """.trimIndent())
        
        // Page 25 - What We Do
        pages.add("""
            But I also remember what it cost.
            
            The Old War never really ended. It just changed shape. The Sentients are still out there. The Orokin are gone, but their legacy remains in every Grineer clone, every Corpus contract, every Infested hive. And we—the Tenno—are still fighting. Still killing. Still wearing our frames like second skins, hiding our fragile bodies behind walls of ancient alloy.
            
            I do not know if there will ever be peace.
        """.trimIndent())
        
        // Page 26 - We Are Tenno
        pages.add("""
            I do not know if children who were forged in hellspace can ever learn to be anything but weapons. But I know this:
            
            When the next war comes—and it will come—I will be ready. I will step into Excalibur Prime, feel his weight settle around me like a second skin, and I will fight.
            
            Because that is what we do. That is what we *are*.
            
            We are the rejects. The twisted few. The children who returned from that place.
            
            We are Tenno.
            
            And we do not forget.
        """.trimIndent())
        
        // Page 27 - Epilogue
        pages.add("""
            §l§6Epilogue: A Letter to the Future§r
            
            §oTo whoever finds this record:§r
            
            My name is Void. I was a child once, on a ship called Zariman Ten Zero. I became a weapon in a war that should never have been fought. I wore the first Prime Warframe—Excalibur Prime—through battles that would shatter worlds, and I helped destroy an empire that deserved to fall.
            
            I do not know if what we did was right. I do not know if anything we do can ever be right.
        """.trimIndent())
        
        // Page 28 - Final Words
        pages.add("""
            But I know that I am still here, still fighting, still wearing my frame like armor against a universe that has never known what to do with us.
            
            If you are reading this, you are probably Tenno. You have probably seen the Void, heard its whispers, felt its power flow through your veins. You have probably wondered if you are monster or savior, curse or blessing.
            
            The answer is: yes. All of it. None of it. You are what you choose to be.
        """.trimIndent())
        
        // Page 29 - Never Alone
        pages.add("""
            But if you ever doubt, if you ever feel lost—reach out through Transference. Touch your frame. Feel the echo of all those who came before you, all those who wore that steel and fought that fight. You are not alone. You have never been alone.
            
            We are Tenno. We are eternal. We are the sword and the shield, the nightmare and the dream.
            
            And we will endure.
            
            —Void, Operator of Excalibur Prime
        """.trimIndent())
        
        meta.pages = pages
        book.itemMeta = meta
        return book
    }
    
    fun getCommand(name: String) = plugin.getCommand(name) ?: throw Exception("Command $name not found")

    getCommand("ixr").apply {
        setExecutor { sender, _, _, _ ->
            val player = sender as? Player
            if (player == null) {
                sender.sendMessage("§cThis command can only be used by players!")
                return@setExecutor true
            }

            // Always open main pets menu for `/ixr`
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
                player.sendMessage("§cYou don't have an active spider! Use /ixr to create one.")
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

    // Warframe command - gives Voice of the First Blade book
    getCommand("warframe").apply {
        setExecutor { sender, _, _, _ ->
            val player = sender as? Player
            if (player == null) {
                sender.sendMessage("§cThis command can only be used by players!")
                return@setExecutor true
            }

            // Check if player already has the book
            val voiceBookTitle = "Voice of the First Blade"
            val hasBook = player.inventory.any { item ->
                item != null && item.type == Material.WRITTEN_BOOK && 
                item.itemMeta is BookMeta && 
                (item.itemMeta as BookMeta).title == voiceBookTitle
            }

            if (hasBook) {
                player.sendMessage("§cYou already have the §6Voice of the First Blade§c book! You can only have one copy.")
                return@setExecutor true
            }

            // Create and give the book
            val book = createVoiceOfFirstBladeBook()
            
            // Try to add book to inventory
            val leftoverItems = player.inventory.addItem(book)
            
            if (leftoverItems.isNotEmpty()) {
                // Inventory was full, drop the book at player's location
                player.world.dropItem(player.location, book)
                player.sendMessage("§cYour inventory was full! The book was dropped at your feet.")
            } else {
                player.sendMessage("§6§lVoice of the First Blade§r§a has been added to your inventory!")
                player.world.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f)
            }

            return@setExecutor true
        }

        setTabCompleter { _, _, _, _ ->
            // No subcommands to suggest
            return@setTabCompleter emptyList<String>()
        }
    }
}

private fun getEconomy(): Economy? {
    val vault = Bukkit.getPluginManager().getPlugin("Vault") ?: return null
    val economyProvider = Bukkit.getServicesManager().getRegistration(Economy::class.java) ?: return null
    return economyProvider.provider
}
