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
    private val walkSpeed = 3.75 / 20.0 // 3.75 blocks per second max walking
    private val sprintSpeed = 4.0 / 20.0 // 4.0 blocks per second max sprinting
    
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
            return
        }
        
        val isSprinting = owner.isSprinting
        val maxSpeed = if (attack != null || isSprinting) sprintSpeed else walkSpeed
        // Calculate speed based on distance to follow target, capped at max speed
        val distanceFactor = maxSpeed / 20.0 // reach max speed at 20 blocks distance
        val moveSpeed = min(maxSpeed, distanceToFollow * distanceFactor)
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

