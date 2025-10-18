package com.daqem.irobot.entity.ai.behavior.farming;

import com.daqem.irobot.client.renderer.OutlineRenderer;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.entity.task.RobotTask;
import com.daqem.irobot.util.CropUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Optional;

public class FindNextCropToFarm extends Behavior<MiniRobotEntity> {

    public FindNextCropToFarm() {
        super(ImmutableMap.of(
                IRobotMemoryModuleTypes.ASSIGNED_TASK.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.TASK_AREA_START.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.TASK_AREA_END.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.FARM_TARGET_POS.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull MiniRobotEntity robot) {
        return robot.getBrain().getMemory(IRobotMemoryModuleTypes.ASSIGNED_TASK.get()).orElse(null) == RobotTask.FARMING;
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull MiniRobotEntity robot, long gameTime) {
        Optional<GlobalPos> startPosOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.TASK_AREA_START.get());
        Optional<GlobalPos> endPosOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.TASK_AREA_END.get());

        if (startPosOpt.isEmpty() || endPosOpt.isEmpty()) {
            return;
        }

        BlockPos startPos = startPosOpt.get().pos();
        BlockPos endPos = endPosOpt.get().pos();

        AABB searchArea = OutlineRenderer.createBoundingBox(startPos, endPos);

        findClosestMatureCrop(level, robot, searchArea).ifPresent(cropPos -> {
            robot.getBrain().setMemory(IRobotMemoryModuleTypes.FARM_TARGET_POS.get(), GlobalPos.of(level.dimension(), cropPos));
            robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(cropPos, 0.5f, 1));
        });
    }

    private Optional<BlockPos> findClosestMatureCrop(ServerLevel level, MiniRobotEntity robot, AABB searchArea) {
        BlockPos robotPos = robot.blockPosition();
        return BlockPos.betweenClosedStream(searchArea)
                .map(BlockPos::immutable)
                .filter(pos -> {
                    BlockPos farmlandPos = pos.below();
                    return level.getBlockState(farmlandPos).is(net.minecraft.world.level.block.Blocks.FARMLAND) &&
                            CropUtils.isFarmableCrop(level.getBlockState(pos)) &&
                            CropUtils.isMature(level.getBlockState(pos));
                })
                .min(Comparator.comparingDouble(pos -> pos.distSqr(robotPos)));
    }
}