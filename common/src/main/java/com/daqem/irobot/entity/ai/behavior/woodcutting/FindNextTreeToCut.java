package com.daqem.irobot.entity.ai.behavior.woodcutting;

import com.daqem.irobot.client.renderer.OutlineRenderer;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.entity.task.RobotTask;
import com.daqem.irobot.util.TreeUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Optional;

public class FindNextTreeToCut extends Behavior<MiniRobotEntity> {

    public FindNextTreeToCut() {
        super(ImmutableMap.of(
                IRobotMemoryModuleTypes.ASSIGNED_TASK.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.TASK_AREA_START.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.TASK_AREA_END.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.TREE_TARGET_POS.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, @NotNull MiniRobotEntity robot) {
        return robot.getBrain().getMemory(IRobotMemoryModuleTypes.ASSIGNED_TASK.get()).orElse(null) == RobotTask.WOODCUTTING;
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

        findClosestTree(level, robot, searchArea).ifPresent(treePos -> {
            BlockPos northPos = treePos.north();
            BlockPos southPos = treePos.south();
            BlockPos eastPos = treePos.east();
            BlockPos westPos = treePos.west();

            Vec3 robotVec = robot.position();
            BlockPos bestPos = northPos;
            double shortestDistance = northPos.distToCenterSqr(robotVec);
            if (southPos.distToCenterSqr(robotVec) < shortestDistance) {
                shortestDistance = southPos.distToCenterSqr(robotVec);
                bestPos = southPos;
            }
            if (eastPos.distToCenterSqr(robotVec) < shortestDistance) {
                shortestDistance = eastPos.distToCenterSqr(robotVec);
                bestPos = eastPos;
            }
            if (westPos.distToCenterSqr(robotVec) < shortestDistance) {
                bestPos = westPos;
            }

            robot.getBrain().setMemory(IRobotMemoryModuleTypes.TREE_TARGET_POS.get(), GlobalPos.of(level.dimension(), treePos));
            robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(bestPos, 0.5f, 1));
            BlockState logState = level.getBlockState(treePos);
            TreeUtils.getSaplingFromLog(logState).ifPresent(sapling -> {
                robot.getBrain().setMemory(IRobotMemoryModuleTypes.SAPLING_TO_PLANT.get(), sapling);
                robot.getBrain().setMemory(IRobotMemoryModuleTypes.REPLANT_POS.get(), treePos);
            });
        });
    }

    private Optional<BlockPos> findClosestTree(ServerLevel level, MiniRobotEntity robot, AABB searchArea) {
        BlockPos robotPos = robot.blockPosition();
        return BlockPos.betweenClosedStream(searchArea)
                .map(BlockPos::immutable)
                .filter(pos -> isTreeLog(level, pos))
                .filter(pos -> TreeUtils.isTree(level, pos))
                .map(pos -> TreeUtils.getTreeBase(level, pos))
                .distinct()
                .min(Comparator.comparingDouble(pos -> pos.distSqr(robotPos)));
    }

    private boolean isTreeLog(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(BlockTags.LOGS);
    }
}