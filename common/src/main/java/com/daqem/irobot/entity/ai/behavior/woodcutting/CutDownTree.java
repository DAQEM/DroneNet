package com.daqem.irobot.entity.ai.behavior.woodcutting;

import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.util.TreeUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;
import java.util.Set;

public class CutDownTree extends Behavior<MiniRobotEntity> {

    private int ticksSinceStarted;
    private int totalTicksToCut;
    private BlockPos targetPos;

    public CutDownTree() {
        super(ImmutableMap.of(
                IRobotMemoryModuleTypes.TREE_TARGET_POS.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MiniRobotEntity robot) {
        Optional<GlobalPos> treePos = robot.getBrain().getMemory(IRobotMemoryModuleTypes.TREE_TARGET_POS.get());
        return treePos.isPresent();
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return this.ticksSinceStarted > 600; // 30 seconds
    }

    @Override
    protected void start(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        this.ticksSinceStarted = 0;
        robot.getBrain().getMemory(IRobotMemoryModuleTypes.TREE_TARGET_POS.get()).ifPresent(globalPos -> {
            this.targetPos = globalPos.pos();
            Set<BlockPos> treeBlocks = TreeUtils.getTreeBlocks(level, this.targetPos);
            this.totalTicksToCut = treeBlocks.size() * 20; // 1 second per log
            robot.setMining(true);
        });
    }

    @Override
    protected void stop(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        robot.setMining(false);
        if (this.targetPos != null) {
            level.destroyBlockProgress(robot.getId(), this.targetPos, -1);
        }
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.TREE_TARGET_POS.get());
        this.targetPos = null;
    }

    @Override
    protected void tick(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        if (this.targetPos == null) {
            return;
        }

        this.ticksSinceStarted++;
        int progress = (int) ((this.ticksSinceStarted / (float) this.totalTicksToCut) * 10);
        level.destroyBlockProgress(robot.getId(), this.targetPos, progress);

        if (this.ticksSinceStarted >= this.totalTicksToCut) {
            Set<BlockPos> treeBlocks = TreeUtils.getTreeBlocks(level, this.targetPos);
            for (BlockPos pos : treeBlocks) {
                level.destroyBlock(pos, true, robot);
            }
            robot.setEnergy(robot.getEnergy() - (treeBlocks.size() * 2.0));
            doStop(level, robot, gameTime);
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        return this.targetPos != null && robot.getBrain().getMemory(IRobotMemoryModuleTypes.TREE_TARGET_POS.get()).isPresent();
    }
}