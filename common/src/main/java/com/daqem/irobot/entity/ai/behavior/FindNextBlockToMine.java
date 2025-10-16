package com.daqem.irobot.entity.ai.behavior;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.entity.task.RobotTask;
import com.google.common.collect.ImmutableMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class FindNextBlockToMine extends Behavior<MiniRobotEntity> {

    public FindNextBlockToMine() {
        super(ImmutableMap.of(
                IRobotMemoryModuleTypes.ASSIGNED_TASK.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.TASK_AREA_START.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.TASK_AREA_END.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.MINE_TARGET_POS.get(), MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MiniRobotEntity robot) {
        return robot.getBrain().getMemory(IRobotMemoryModuleTypes.ASSIGNED_TASK.get()).orElse(RobotTask.NONE) == RobotTask.MINING;
    }

    @Override
    protected void start(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        Optional<GlobalPos> startPosOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.TASK_AREA_START.get());
        Optional<GlobalPos> endPosOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.TASK_AREA_END.get());

        if (startPosOpt.isEmpty() || endPosOpt.isEmpty()) {
            return;
        }

        BlockPos startPos = startPosOpt.get().pos();
        BlockPos endPos = endPosOpt.get().pos();

        boolean hasMineableBlocks = false;

        // Iterate from bottom to top to find the next block
        for (BlockPos pos : BlockPos.betweenClosed(startPos, endPos)) {
            BlockState blockState = level.getBlockState(pos);
            if (!blockState.isAir() && blockState.getDestroySpeed(level, pos) >= 0) { // Check if mineable
                hasMineableBlocks = true;
                if (robot.getNavigation().createPath(pos, 1) != null) {
                    robot.getBrain().setMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get(), GlobalPos.of(level.dimension(), pos));
                    return; // Found a reachable block
                }
            }
        }

        if (!hasMineableBlocks) {
            finishMining(robot, "robot.task.mining_complete", ChatFormatting.GREEN);
        } else {
            // No reachable blocks found, but there are mineable blocks left.
            finishMining(robot, "robot.task.mining_stuck", ChatFormatting.YELLOW);
        }
    }

    private void finishMining(MiniRobotEntity robot, String message, ChatFormatting color) {
        if (robot.getOwner() instanceof ServerPlayer owner) {
            owner.sendSystemMessage(IRobot.translatable(message).withStyle(color), true);
        }

        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.ASSIGNED_TASK.get());
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.TASK_AREA_START.get());
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.TASK_AREA_END.get());
    }
}