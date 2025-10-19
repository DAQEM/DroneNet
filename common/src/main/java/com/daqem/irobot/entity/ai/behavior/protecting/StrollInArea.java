package com.daqem.irobot.entity.ai.behavior.protecting;

import com.daqem.irobot.client.renderer.OutlineRenderer;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class StrollInArea extends Behavior<MiniRobotEntity> {

    private static final int STROLL_COOLDOWN_TICKS = 100; // 5 seconds
    private long lastStrollTime;

    public StrollInArea() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
                IRobotMemoryModuleTypes.TASK_AREA_START.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.TASK_AREA_END.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MiniRobotEntity owner) {
        // Only stroll if enough time has passed since the last stroll
        return owner.level().getGameTime() - this.lastStrollTime >= STROLL_COOLDOWN_TICKS;
    }

    @Override
    protected void start(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        Optional<GlobalPos> startPosOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.TASK_AREA_START.get());
        Optional<GlobalPos> endPosOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.TASK_AREA_END.get());

        if (startPosOpt.isEmpty() || endPosOpt.isEmpty()) {
            return;
        }

        AABB protectionArea = OutlineRenderer.createBoundingBox(startPosOpt.get().pos(), endPosOpt.get().pos());
        Optional<Vec3> targetPos = findRandomPosInAABB(robot, protectionArea);

        targetPos.ifPresent(pos -> {
            robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, 0.5f, 1));
            this.lastStrollTime = gameTime;
        });
    }

    private Optional<Vec3> findRandomPosInAABB(MiniRobotEntity robot, AABB aabb) {
        RandomSource random = robot.getRandom();
        // Try a few times to find a valid position
        for (int i = 0; i < 10; i++) {
            double x = aabb.minX + (aabb.getXsize() * random.nextDouble());
            double y = aabb.minY + (aabb.getYsize() * random.nextDouble());
            double z = aabb.minZ + (aabb.getZsize() * random.nextDouble());
            BlockPos targetPos = BlockPos.containing(x, y, z);

            // Find a safe spot near the random point
            Vec3 safePos = DefaultRandomPos.getPosTowards(robot, 10, 7, Vec3.atBottomCenterOf(targetPos), (float) (Math.PI / 2.0));
            if (safePos != null && aabb.contains(safePos)) {
                return Optional.of(safePos);
            }
        }
        return Optional.empty();
    }
}