package com.daqem.irobot.entity.ai.behavior;

import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.google.common.collect.ImmutableMap;
import io.netty.handler.codec.compression.ZstdOptions;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class MineBlock extends Behavior<MiniRobotEntity> {

    private float miningProgress;
    private int lastBreakProgress = -1;
    private int retryCounter = 0;
    private static final double MAX_REACH_DISTANCE_SQ = 4.5 * 4.5;
    private static final double PREFERRED_REACH_DISTANCE_SQ = 3.0 * 3.0;

    public MineBlock() {
        super(ImmutableMap.of(
                IRobotMemoryModuleTypes.MINE_TARGET_POS.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected void start(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        this.miningProgress = 0;
        this.lastBreakProgress = -1;
        this.retryCounter = 0;
        robot.getBrain().getMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get()).ifPresent(globalPos -> {
            robot.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(globalPos.pos()));
            BlockState blockState = level.getBlockState(globalPos.pos());
            robot.setBestToolForBlock(blockState);
        });
    }

    @Override
    protected void stop(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get());
    }

    @Override
    protected void tick(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        Optional<GlobalPos> minePos = robot.getBrain().getMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get());
        if (minePos.isEmpty()) {
            return;
        }
        BlockState blockState = level.getBlockState(minePos.get().pos());
        if (blockState.isAir()) {
            doStop(level, robot, gameTime);
            return;
        }
        double distanceSq = robot.distanceToSqr(minePos.get().pos().getX() + 0.5, minePos.get().pos().getY() + 0.5, minePos.get().pos().getZ() + 0.5);
        if (distanceSq > MAX_REACH_DISTANCE_SQ) {
            doStop(level, robot, gameTime);
            return;
        }
        if (distanceSq > PREFERRED_REACH_DISTANCE_SQ) {
            if (retryCounter < 5) {
                robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(minePos.get().pos(), 0.5f, 1));
                retryCounter++;
                return;
            }
        }
        robot.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        level.destroyBlock(minePos.get().pos(), false, robot);
        doStop(level, robot, gameTime);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        return robot.getBrain().getMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get())
                .map(globalPos -> !level.getBlockState(globalPos.pos()).isAir())
                .orElse(false);
    }
}