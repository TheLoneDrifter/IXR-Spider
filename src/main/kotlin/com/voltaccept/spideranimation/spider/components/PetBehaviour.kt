package com.voltaccept.spideranimation.spider.components

import com.voltaccept.spideranimation.PetSpiderOwner
import com.voltaccept.spideranimation.spider.components.body.SpiderBody
import com.voltaccept.spideranimation.utilities.ecs.ECS
import com.voltaccept.spideranimation.utilities.ecs.ECSEntity
import com.voltaccept.spideranimation.laser.LaserAttack
import org.bukkit.Bukkit
import org.bukkit.util.Vector
import kotlin.math.min

class PetBehaviour {
    private val followDistance = 3.0
    private val teleportDistance = 20.0
    private val walkSpeed = 0.13 // Close to player walk speed (0.1)
    private val sprintSpeed = 0.25 // Close to player sprint speed (0.28)
    // Remember last used move speed so the spider can continue moving when player stops
    // Remember last movement-period top speed so the spider can continue moving
    // at the peak speed from the last time the player was moving.
    private var lastTopSpeed: Double = walkSpeed * 0.8
    private var lastMoveTime: Long = 0L
    
    fun update(ecs: ECS, entity: ECSEntity) {
        val ownerComponent = entity.query<PetSpiderOwner>() ?: return
        val spider = entity.query<SpiderBody>() ?: return
        
        val owner = Bukkit.getPlayer(ownerComponent.ownerUUID) ?: return
        if (!owner.isOnline) return
        
        val ownerLocation = owner.location
        val spiderLocation = spider.location()
        
        // Check if same world
        if (ownerLocation.world != spiderLocation.world) return
        
        val attack = entity.query<LaserAttack>()
        val followLocation = if (attack != null && attack.target.isValid) attack.target.location else ownerLocation
        
        val distanceToOwner = ownerLocation.distance(spiderLocation)
        val distanceToFollow = followLocation.distance(spiderLocation)
        
        // Teleport if too far from owner
        if (distanceToOwner > teleportDistance) {
            val teleportLocation = ownerLocation.clone()
            teleportLocation.y += spider.gait.stationary.bodyHeight
            spider.position.copy(teleportLocation.toVector())
            spider.velocity.zero()
            // reset remembered speed after teleport
            lastTopSpeed = walkSpeed * 0.8
            lastMoveTime = 0L
            return
        }
        
        val isSprinting = owner.isSprinting
        // Prefer using the owner's current velocity to set spider speed at 80% of player's movement.
        // If the player has essentially zero velocity, continue using the last non-zero speed
        // until the spider reaches the player.
        val ownerSpeed = owner.velocity.length()
        val now = System.currentTimeMillis()
        val moveSpeed: Double = if (ownerSpeed > 0.01) {
            val s = ownerSpeed * 0.8
            if (s > lastTopSpeed) lastTopSpeed = s
            lastMoveTime = now
            s
        } else {
            if (distanceToFollow <= followDistance) {
                // reached the follow target â€” reset stored peak speed
                lastTopSpeed = walkSpeed * 0.8
                lastMoveTime = 0L
            }
            lastTopSpeed
        }
        // Scale speed by spider health (keep a minimum so it can still approach)
        val healthFactor = (spider.health / spider.maxHealth).coerceIn(0.2, 1.0)
        val adjustedMoveSpeed = moveSpeed * healthFactor
        
        spider.gallop = isSprinting
        
        // Follow if beyond follow distance
        if (distanceToFollow > followDistance) {
            val direction = followLocation.toVector()
                .subtract(spiderLocation.toVector())
                .normalize()
            
            // Calculate desired velocity
            val desiredVelocity = direction.multiply(adjustedMoveSpeed)
            
            // Smoothly adjust velocity
            val acceleration = desiredVelocity.clone()
                .subtract(spider.velocity)
                .multiply(0.2)
            
            spider.velocity.add(acceleration)
            
            // Limit horizontal speed
            val horizontalVel = Vector(spider.velocity.x, 0.0, spider.velocity.z)
            val horizontalSpeed = horizontalVel.length()
            if (horizontalSpeed > adjustedMoveSpeed) {
                horizontalVel.normalize().multiply(adjustedMoveSpeed)
                spider.velocity.x = horizontalVel.x
                spider.velocity.z = horizontalVel.z
            }
            
            spider.isWalking = true
            
            // Face the follow target
            val yaw = Math.atan2(direction.z, direction.x).toFloat() - Math.PI.toFloat() / 2
            spider.orientation.rotationYXZ(yaw, 0f, 0f)
        } else {
            spider.isWalking = true
        }
    }
}

fun setupPetBehaviour(ecs: ECS) {
    ecs.onTick {
        for ((entity, petBehaviour) in ecs.query<ECSEntity, PetBehaviour>()) {
            petBehaviour.update(ecs, entity)
        }
    }
}

