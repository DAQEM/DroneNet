package com.daqem.irobot.entity.ai.behavior.farming;

import com.daqem.irobot.client.renderer.OutlineRenderer;
import com.daqem.irobot.entity.IRobotEntity;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.entity.task.RobotTask;
import com.daqem.irobot.item.module.ModuleItem;
import com.daqem.irobot.util.CropUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Optional;

public class FindFarmableBlock extends Behavior<IRobotEntity> {

    public FindFarmableBlock() {
        super(ImmutableMap.of(
                IRobotMemoryModuleTypes.ASSIGNED_TASK.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.TASK_AREA_START.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.TASK_AREA_END.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.FARM_TARGET_POS.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull IRobotEntity robot) {
        return robot.getBrain().getMemory(IRobotMemoryModuleTypes.ASSIGNED_TASK.get()).orElse(null) == RobotTask.FARMING;
    }

    @Override
    protected void start(@NotNull ServerLevel level, @NotNull IRobotEntity robot, long gameTime) {
        Optional<GlobalPos> startPosOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.TASK_AREA_START.get());
        Optional<GlobalPos> endPosOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.TASK_AREA_END.get());

        if (startPosOpt.isEmpty() || endPosOpt.isEmpty()) {
            return;
        }

        AABB searchArea = OutlineRenderer.createBoundingBox(startPosOpt.get().pos(), endPosOpt.get().pos());

        Optional<BlockPos> matureCrop = findClosestMatureCrop(level, robot, searchArea);
        if (matureCrop.isPresent()) {
            robot.getBrain().setMemory(IRobotMemoryModuleTypes.FARM_TARGET_POS.get(), GlobalPos.of(level.dimension(), matureCrop.get()));
            robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(matureCrop.get(), 0.5f, 1));
            return;
        }

        if (robot.hasModule(ModuleItem.ModuleType.CROP_REPLANT)) {
            findClosestEmptyFarmland(level, robot, searchArea).ifPresent(emptySpot -> {
                robot.getBrain().setMemory(IRobotMemoryModuleTypes.FARM_TARGET_POS.get(), GlobalPos.of(level.dimension(), emptySpot));
                robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(emptySpot, 0.5f, 1));
            });
        }
    }

    private Optional<BlockPos> findClosestMatureCrop(ServerLevel level, IRobotEntity robot, AABB searchArea) {
        return BlockPos.betweenClosedStream(searchArea)
                .map(BlockPos::immutable)
                .filter(pos -> CropUtils.isFarmableCrop(level.getBlockState(pos)) && CropUtils.isMature(level.getBlockState(pos)))
                .min(Comparator.comparingDouble(pos -> pos.distSqr(robot.blockPosition())));
    }

    private Optional<BlockPos> findClosestEmptyFarmland(ServerLevel level, IRobotEntity robot, AABB searchArea) {
        return BlockPos.betweenClosedStream(searchArea)
                .map(BlockPos::immutable)
                .filter(pos -> level.getBlockState(pos).isAir() && isPlantableSpot(level, pos, robot))
                .min(Comparator.comparingDouble(pos -> pos.distSqr(robot.blockPosition())));
    }

    private boolean isPlantableSpot(ServerLevel level, BlockPos pos, IRobotEntity robot) {
        BlockState ground = level.getBlockState(pos.below());
        if (!ground.is(Blocks.FARMLAND) && !ground.is(Blocks.SOUL_SAND)) {
            return false;
        }

        for (int i = 0; i < robot.getInventory().getContainerSize(); i++) {
            ItemStack stack = robot.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
                if (CropUtils.isPlantable(stack.getItem())) {
                    if (blockItem.getBlock().defaultBlockState().canSurvive(level, pos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}