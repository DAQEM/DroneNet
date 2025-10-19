package com.daqem.irobot.entity.ai.behavior.protecting;

import com.daqem.irobot.client.renderer.OutlineRenderer;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FindHostileMobInArea extends Behavior<MiniRobotEntity> {

    public FindHostileMobInArea() {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
                IRobotMemoryModuleTypes.TASK_AREA_START.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.TASK_AREA_END.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected void start(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        Optional<GlobalPos> startPosOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.TASK_AREA_START.get());
        Optional<GlobalPos> endPosOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.TASK_AREA_END.get());

        if (startPosOpt.isEmpty() || endPosOpt.isEmpty()) {
            return;
        }

        AABB protectionArea = OutlineRenderer.createBoundingBox(startPosOpt.get().pos(), endPosOpt.get().pos());

        findNearestHostileMob(level, robot, protectionArea).ifPresent(target -> {
            Brain<?> brain = robot.getBrain();
            brain.setMemory(MemoryModuleType.ATTACK_TARGET, target);
        });
    }

    private Optional<LivingEntity> findNearestHostileMob(ServerLevel level, MiniRobotEntity robot, AABB protectionArea) {
        List<Monster> hostilesInArea = level.getEntitiesOfClass(Monster.class, protectionArea, entity -> entity.isAlive() && robot.canAttack(entity));

        if (hostilesInArea.isEmpty()) {
            return Optional.empty();
        }

        return hostilesInArea.stream()
                .min(Comparator.comparingDouble(hostile -> hostile.distanceToSqr(robot)))
                .map(monster -> monster);
    }
}