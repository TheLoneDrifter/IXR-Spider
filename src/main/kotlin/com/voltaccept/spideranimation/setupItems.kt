package com.voltaccept.spideranimation

import com.voltaccept.spideranimation.AppState.ecs
import com.voltaccept.spideranimation.kinematic_chain_visualizer.KinematicChainVisualizer
import com.voltaccept.spideranimation.spider.components.body.SpiderBody
import com.voltaccept.spideranimation.spider.components.Cloak
import com.voltaccept.spideranimation.spider.components.PointDetector
import com.voltaccept.spideranimation.spider.components.rendering.SpiderRenderer
import com.voltaccept.spideranimation.laser.LaserPoint
import com.voltaccept.spideranimation.utilities.custom_items.CustomItemComponent
import com.voltaccept.spideranimation.utilities.custom_items.attach
import com.voltaccept.spideranimation.utilities.custom_items.createNamedItem
import com.voltaccept.spideranimation.utilities.custom_items.customItemRegistry
import com.voltaccept.spideranimation.utilities.ecs.ECSEntity
import com.voltaccept.spideranimation.utilities.raycastGround
import com.voltaccept.spideranimation.utilities.events.onTick
import com.voltaccept.spideranimation.utilities.overloads.direction
import com.voltaccept.spideranimation.utilities.overloads.eyePosition
import com.voltaccept.spideranimation.utilities.overloads.playSound
import com.voltaccept.spideranimation.utilities.overloads.position
import com.voltaccept.spideranimation.utilities.overloads.sendActionBar
import com.voltaccept.spideranimation.utilities.overloads.yaw
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.util.Vector
import kotlin.math.roundToInt


fun setupItems() {
    val spiderComponent = CustomItemComponent("spider")
    customItemRegistry += createNamedItem(Material.NETHERITE_INGOT, "Spider").attach(spiderComponent)
    spiderComponent.onGestureUse { player, _ ->
        val spiderEntity = AppState.ecs.query<ECSEntity, SpiderBody>().firstOrNull()?.first
        if (spiderEntity == null) {
            val yawIncrements = 45.0f
            val yaw = (player.yaw / yawIncrements).roundToInt() * yawIncrements

            val hitPosition = player.world.raycastGround(player.eyePosition, player.direction, 100.0)?.hitPosition ?: return@onGestureUse

            player.world.playSound(hitPosition, Sound.BLOCK_NETHERITE_BLOCK_PLACE, 1.0f, 1.0f)
            AppState.createSpider(hitPosition.toLocation(player.world).apply { this.yaw = yaw })
            player.sendActionBar("Spider created")
        } else {
            player.world.playSound(player.position, Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1.0f, 0.0f)
            spiderEntity.remove()
            player.sendActionBar("Spider removed")
        }
    }


    val disableLegComponent = CustomItemComponent("disableLeg")
    customItemRegistry += createNamedItem(Material.SHEARS, "Toggle Leg").attach(disableLegComponent)
    onTick {
        val players = disableLegComponent.getPlayersHoldingItem().toSet()
        for (pointDetector in AppState.ecs.query<PointDetector>()) {
            pointDetector.checkPlayers = players
        }
    }
    disableLegComponent.onGestureUse { player, _ ->
        for (pointDetector in AppState.ecs.query<PointDetector>()) {
            val selectedLeg = pointDetector.selectedLeg[player]
            if (selectedLeg == null) {
                player.world.playSound(player.position, Sound.BLOCK_DISPENSER_FAIL, 1.0f, 2.0f)
                return@onGestureUse
            }

            selectedLeg.isDisabled = !selectedLeg.isDisabled
            player.world.playSound(player.position, Sound.BLOCK_LANTERN_PLACE, 1.0f, 1.0f)
        }
    }

    val toggleDebugComponent = CustomItemComponent("toggleDebug")
    customItemRegistry += createNamedItem(Material.BLAZE_ROD, "Toggle Debug Graphics").attach(toggleDebugComponent)
    toggleDebugComponent.onGestureUse { player, _ ->
        AppState.renderDebugVisuals = !AppState.renderDebugVisuals

        AppState.ecs.query<KinematicChainVisualizer>().forEach {
            it.detailed = AppState.renderDebugVisuals
        }

        val pitch = if (AppState.renderDebugVisuals) 2.0f else 1.5f
        player.world.playSound(player.position, Sound.BLOCK_DISPENSER_FAIL, 1.0f, pitch)
    }


    val switchRendererComponent = CustomItemComponent("switchRenderer")
    customItemRegistry += createNamedItem(Material.LIGHT_BLUE_DYE, "Switch Renderer").attach(switchRendererComponent)
    switchRendererComponent.onGestureUse { player, _ ->
        AppState.ecs.query<SpiderRenderer>().forEach { renderer ->
            renderer.useParticles = !renderer.useParticles

            if (renderer.useParticles) {
                player.world.playSound(player.position, Sound.ENTITY_AXOLOTL_ATTACK, 1.0f, 1.0f)
            } else {
                player.world.playSound(player.position, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1.0f, 1.0f)
            }
        }
    }

    val toggleCloakComponent = CustomItemComponent("toggleCloak")
    customItemRegistry += createNamedItem(Material.GREEN_DYE, "Toggle Cloak").attach(toggleCloakComponent)
    toggleCloakComponent.onGestureUse { _, _ ->
        val (cloak, entity) = AppState.ecs.query<Cloak, ECSEntity>().firstOrNull() ?: return@onGestureUse
        cloak.toggleCloak(AppState.ecs, entity)
    }

    val chainVisualizerStep = CustomItemComponent("chainVisualizerStep")
    customItemRegistry += createNamedItem(Material.PURPLE_DYE, "Chain Visualizer Step").attach(chainVisualizerStep)
    chainVisualizerStep.onGestureUse { player, _ ->
        AppState.ecs.query<KinematicChainVisualizer>().forEach {
            player.world.playSound(player.position, Sound.BLOCK_DISPENSER_FAIL, 1.0f, 2.0f)
            it.step()
        }
    }

    val chainVisualizerStraighten = CustomItemComponent("chainVisualizerStraighten")
    customItemRegistry += createNamedItem(Material.MAGENTA_DYE, "Chain Visualizer Straighten").attach(chainVisualizerStraighten)
    chainVisualizerStraighten.onGestureUse { player, _ ->
        ecs.query<KinematicChainVisualizer>().forEach {
            player.world.playSound(player.position, Sound.BLOCK_DISPENSER_FAIL, 1.0f, 2.0f)
            it.straighten(it.target ?: return@onGestureUse)
        }
    }

    val switchGaitComponent = CustomItemComponent("switchGait")
    customItemRegistry += createNamedItem(Material.BREEZE_ROD, "Switch Gait").attach(switchGaitComponent)
    switchGaitComponent.onGestureUse { player, _ ->
        player.world.playSound(player.position, Sound.BLOCK_DISPENSER_FAIL, 1.0f, 2.0f)
        AppState.gallop = !AppState.gallop
        player.sendActionBar(if (!AppState.gallop) "Walk mode" else "Gallop mode")
    }

    val laserPointerComponent = CustomItemComponent("laserPointer")
    customItemRegistry += createNamedItem(Material.ARROW, "Laser Pointer").attach(laserPointerComponent)

    val comeHereComponent = CustomItemComponent("comeHere")
    customItemRegistry += createNamedItem(Material.CARROT_ON_A_STICK, "Come Here").attach(comeHereComponent)

    class LaserPointExpire(val owner: Player) {
        var expired = false
    }

    ecs.onTick {
        // mark laser for removal
        ecs.query<LaserPointExpire>().forEach { expire ->
            expire.expired = true
        }
    }

    ecs.onTick {
        val lasers = ecs.query<ECSEntity, LaserPoint, LaserPointExpire>()

        fun spawnLaser(player: Player, position: Vector, isVisible: Boolean) {
            val existing = lasers.find { it.third.owner == player }

            if (existing != null) {
                // update existing laser
                existing.second.world = player.world
                existing.second.position = position
                existing.second.isVisible = isVisible
                existing.third.expired = false
            } else {
                // create new laser
                ecs.spawn(
                    LaserPointExpire(player),
                    LaserPoint(
                        world = player.world,
                        position = position,
                        isVisible = isVisible,
                    )
                )
            }
        }

        // handle laser pointer
        for (player in laserPointerComponent.getPlayersHoldingItem()) {
            val direction = player.direction
            val result = player.world.raycastGround(player.eyePosition, direction, 100.0)

            val hitPosition = result?.hitPosition ?:
                player.eyePosition.add(direction.multiply(200))


            val isUsingFallback = result == null
            val isVisible = !isUsingFallback && AppState.miscOptions.showLaser

            spawnLaser(player, hitPosition, isVisible)
        }

        // handle carrot on a stick
        for (player in comeHereComponent.getPlayersHoldingItem()) {
            spawnLaser(player, player.eyePosition, isVisible = false)
        }
    }

    ecs.onTick {
        ecs.query<ECSEntity,LaserPointExpire>().forEach { (entity, expire) ->
            if (expire.expired) entity.remove()
        }
    }

    val petMenuBookComponent = CustomItemComponent("petMenuBook")
    val petMenuBook = createNamedItem(Material.ENCHANTED_BOOK, "§6§lPet Menu").attach(petMenuBookComponent)
    customItemRegistry += petMenuBook
    petMenuBookComponent.onGestureUse { player, _ ->
        player.world.playSound(player.position, Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f)
        PetMainMenu.openMenu(player)
    }

    // Hotbar locking functionality
    val HOTBAR_SLOT = 8 // Last slot in hotbar (0-indexed)
    
    fun givePetMenuBook(player: Player) {
        // Check if player already has the book
        val hasBook = player.inventory.contains(petMenuBook)
        if (!hasBook) {
            // Give the book and move it to the last hotbar slot
            player.inventory.addItem(petMenuBook)
            // Note: moveBookToLastSlot will be called by the listener
        }
    }
    
    fun moveBookToLastSlot(player: Player) {
        val itemInSlot = player.inventory.getItem(HOTBAR_SLOT)
        val bookSlot = (0..35).find { slot -> 
            val item = player.inventory.getItem(slot)
            item != null && petMenuBookComponent.isAttached(item)
        }
        
        if (bookSlot != null && bookSlot != HOTBAR_SLOT) {
            val book = player.inventory.getItem(bookSlot)
            // Move book to last hotbar slot
            player.inventory.setItem(HOTBAR_SLOT, book)
            // Move whatever was in the last slot to the book's previous position
            player.inventory.setItem(bookSlot, itemInSlot)
        }
    }
}

// Event listener to give book on join and lock it to slot
object PetMenuBookListener : Listener {
    private val HOTBAR_SLOT = 8 // Last slot in hotbar (0-indexed)
    
    private fun getPetMenuBookComponent() = CustomItemComponent("petMenuBook")
    
    private fun givePetMenuBook(player: Player) {
        // Get the pet menu book from registry
        val petMenuBook = customItemRegistry.find { getPetMenuBookComponent().isAttached(it) }
        if (petMenuBook == null) return
        
        // Check if player already has the book
        val hasBook = player.inventory.contains(petMenuBook)
        if (!hasBook) {
            // Give the book and move it to the last hotbar slot
            player.inventory.addItem(petMenuBook)
            moveBookToLastSlot(player)
        }
    }
    
    private fun moveBookToLastSlot(player: Player) {
        val petMenuBookComponent = getPetMenuBookComponent()
        val itemInSlot = player.inventory.getItem(HOTBAR_SLOT)
        val bookSlot = (0..35).find { slot -> 
            val item = player.inventory.getItem(slot)
            item != null && petMenuBookComponent.isAttached(item)
        }
        
        if (bookSlot != null && bookSlot != HOTBAR_SLOT) {
            val book = player.inventory.getItem(bookSlot)
            // Move book to last hotbar slot
            player.inventory.setItem(HOTBAR_SLOT, book)
            // Move whatever was in the last slot to the book's previous position
            player.inventory.setItem(bookSlot, itemInSlot)
        }
    }
    
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        givePetMenuBook(event.player)
    }
    
    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        givePetMenuBook(event.player)
    }
    
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val clickedSlot = event.slot
        val petMenuBookComponent = getPetMenuBookComponent()
        val petMenuBook = customItemRegistry.find { petMenuBookComponent.isAttached(it) } ?: return
        
        // Prevent moving the book from the last hotbar slot
        if (clickedSlot == HOTBAR_SLOT && event.currentItem != null && petMenuBookComponent.isAttached(event.currentItem!!)) {
            event.isCancelled = true
            player.sendActionBar("§cPet Menu book is locked to this slot!")
            return
        }
        
        // Prevent moving other items into the last hotbar slot if player has the book
        if (clickedSlot == HOTBAR_SLOT && event.cursor != null && event.cursor!!.type != Material.AIR) {
            val hasBook = player.inventory.contains(petMenuBook)
            if (hasBook) {
                event.isCancelled = true
                player.sendActionBar("§cThis slot is reserved for the Pet Menu book!")
                return
            }
        }
    }
    
    // Periodic check to ensure book stays in correct slot
    init {
        onTick {
            for (player in Bukkit.getOnlinePlayers()) {
                val petMenuBookComponent = getPetMenuBookComponent()
                val petMenuBook = customItemRegistry.find { petMenuBookComponent.isAttached(it) }
                if (petMenuBook != null && player.inventory.contains(petMenuBook)) {
                    moveBookToLastSlot(player)
                }
            }
        }
    }
}
