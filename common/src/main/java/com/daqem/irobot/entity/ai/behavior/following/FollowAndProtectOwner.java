package com.daqem.irobot.entity.ai.behavior.following;

import com.daqem.irobot.config.IRobotConfig;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.monster.Monster;

import java.util.Optional;

public class FollowAndProtectOwner extends Behavior<MiniRobotEntity> {

    public FollowAndProtectOwner() {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MiniRobotEntity robot) {
        LivingEntity owner = robot.getOwner();
        return owner != null && !robot.isLeashed() && !robot.unableToMoveToOwner();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MiniRobotEntity entity, long gameTime) {
        return checkExtraStartConditions(level, entity);
    }

    @Override
    protected void tick(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        LivingEntity owner = robot.getOwner();
        if (owner == null) {
            return;
        }

        Brain<?> brain = robot.getBrain();
        brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(owner, true));

        // 1. Protection Logic: Find a threat. If found, set memory. MeleeAttack behavior will take over.
        Optional<LivingEntity> nearestAttacker = findNearestAttacker(robot, owner);
        if (nearestAttacker.isPresent()) {
            brain.setMemory(MemoryModuleType.ATTACK_TARGET, nearestAttacker.get());
            return;
        }

        // 2. Teleportation Logic
        if (robot.shouldTryTeleportToOwner()) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            robot.tryToTeleportToOwner();
            return;
        }

        // 3. Follow Logic
        double distanceSq = robot.distanceToSqr(owner);
        int followStartDistance = IRobotConfig.FOLLOW_START_DISTANCE.get();
        int followStopDistance = IRobotConfig.FOLLOW_STOP_DISTANCE.get();
        if (distanceSq > followStartDistance * followStartDistance) {
            brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(owner, false), IRobotConfig.FOLLOW_OWNER_SPEED.get(), 2));
        } else if (distanceSq < followStopDistance * followStopDistance) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        }
    }

    private Optional<LivingEntity> findNearestAttacker(MiniRobotEntity robot, LivingEntity owner) {
        int protectionRange = IRobotConfig.PROTECT_OWNER_RANGE.get();
        return robot.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES)
                .flatMap(entities -> entities.stream()
                        .filter(entity -> entity instanceof Monster && entity.isAlive())
                        .map(entity -> (Monster) entity)
                        .filter(monster -> owner.equals(monster.getTarget()))
                        .filter(robot::hasLineOfSight)
                        .filter(monster -> monster.distanceToSqr(owner) < protectionRange * protectionRange)
                        .map(monster -> (LivingEntity) monster)
                        .findFirst());
    }
}