package com.heledron.spideranimation.spider.components

import com.heledron.spideranimation.PetSpiderOwner
import com.heledron.spideranimation.spider.components.body.SpiderBody
import com.heledron.spideranimation.utilities.ecs.ECS
import com.heledron.spideranimation.utilities.ecs.ECSEntity
import org.bukkit.Bukkit
import org.bukkit.util.Vector
import kotlin.math.min

class PetBehaviour {
    private val followDistance = 3.0
    private val teleportDistance = 20.0
    private val walkSpeed = 0.13 // Close to player walk speed (0.1)
    private val sprintSpeed = 0.25 // Close to player sprint speed (0.28)
    
    fun update(ecs: ECS, entity: ECSEntity) {
        val ownerComponent = entity.query<PetSpiderOwner>() ?: return
        val spider = entity.query<SpiderBody>() ?: return
        
        val owner = Bukkit.getPlayer(ownerComponent.ownerUUID) ?: return
        if (!owner.isOnline) return
        
        val ownerLocation = owner.location
        val spiderLocation = spider.location()
        
        // Check if same world
        if (ownerLocation.world != spiderLocation.world) return
        
        val distance = ownerLocation.distance(spiderLocation)
        
        // Teleport if too far
        if (distance > teleportDistance) {
            val teleportLocation = ownerLocation.clone()
            teleportLocation.y += spider.bodyPlan.scale * 2.0
            spider.position.copy(teleportLocation.toVector())
            spider.velocity.zero()
            return
        }
        
        val isSprinting = owner.isSprinting
        val moveSpeed = if (isSprinting) sprintSpeed else walkSpeed
        spider.gallop = isSprinting
        
        // Follow if beyond follow distance
        if (distance > followDistance) {
            val direction = ownerLocation.toVector()
                .subtract(spiderLocation.toVector())
                .normalize()
            
            // Calculate desired velocity
            val desiredVelocity = direction.multiply(moveSpeed)
            
            // Smoothly adjust velocity
            val acceleration = desiredVelocity.clone()
                .subtract(spider.velocity)
                .multiply(0.2)
            
            spider.velocity.add(acceleration)
            
            // Limit horizontal speed
            val horizontalVel = Vector(spider.velocity.x, 0.0, spider.velocity.z)
            val horizontalSpeed = horizontalVel.length()
            if (horizontalSpeed > moveSpeed) {
                horizontalVel.normalize().multiply(moveSpeed)
                spider.velocity.x = horizontalVel.x
                spider.velocity.z = horizontalVel.z
            }
            
            spider.isWalking = true
            
            // Face the owner
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
