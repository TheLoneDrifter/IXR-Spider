package com.voltaccept.spideranimation.spider.components

import com.voltaccept.spideranimation.spider.components.FleeComponent
import com.voltaccept.spideranimation.utilities.ecs.ECS
import com.voltaccept.spideranimation.utilities.ecs.ECSEntity
import com.voltaccept.spideranimation.PetSpiderOwner
import com.voltaccept.spideranimation.spider.components.body.SpiderBody
import org.bukkit.Bukkit
import org.bukkit.util.Vector

class PetBehaviour {
    private val followDistance = 3.0
    private val teleportDistance = 20.0
    private val walkSpeed = 3.75 / 20.0 // 3.75 blocks per second max walking
    private val sprintSpeed = 4.0 / 20.0 // 4.0 blocks per second max sprinting
    
    fun update(ecs: ECS, entity: ECSEntity) {
        val ownerComponent = entity.query<PetSpiderOwner>() ?: return
        val spider = entity.query<SpiderBody>() ?: return
        
        if (spider.isDisabled) {
            spider.isWalking = false
            return
        }
        
        val owner = Bukkit.getPlayer(ownerComponent.ownerUUID) ?: return
        if (!owner.isOnline) return
        
        val ownerLocation = owner.location
        val spiderLocation = spider.location()
        
        val flee = entity.query<FleeComponent>()
        val followLocation = if (flee != null && flee.fleeFrom != null && flee.fleeFrom!!.isValid) {
            // Flee from the damager
            flee.fleeTime--
            if (flee.fleeTime <= 0) {
                entity.removeComponent<FleeComponent>()
            }
            flee.fleeFrom!!.location
        } else {
            ownerLocation
        }
        
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
        val maxSpeed = if (isSprinting) sprintSpeed else walkSpeed
        val moveSpeed = maxSpeed
        
        // Scale speed by leg count (more legs = faster)
        val legCount = spider.bodyPlan.legs.size
        val legSpeedMultiplier = when {
            legCount >= 10 -> 1.4  // 10 legs: 40% faster
            legCount >= 8 -> 1.25  // 8 legs: 25% faster
            legCount >= 6 -> 1.1   // 6 legs: 10% faster
            legCount >= 4 -> 1.0   // 4 legs: base speed
            else -> 0.9            // 2 legs: 10% slower
        }
        
        // Scale speed by fuel level (keep a minimum so it can still move)
        val fuelFactor = (spider.fuel / spider.maxFuel).coerceIn(0.1, 1.0)
        val adjustedMoveSpeed = moveSpeed * fuelFactor * legSpeedMultiplier
        
        spider.gallop = isSprinting
        
        // Follow if beyond follow distance, or always if fleeing
        val shouldMove = if (flee != null) true else distanceToFollow > followDistance
        if (shouldMove) {
            val direction = if (flee != null) {
                // Move away from flee target
                followLocation.toVector().subtract(spiderLocation.toVector()).normalize().multiply(-1.0)
            } else {
                // Move towards follow target
                followLocation.toVector().subtract(spiderLocation.toVector()).normalize()
            }
            
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
