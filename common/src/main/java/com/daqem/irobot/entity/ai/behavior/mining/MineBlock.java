package com.daqem.irobot.entity.ai.behavior.mining;

import com.daqem.irobot.config.IRobotConfig;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class MineBlock extends Behavior<MiniRobotEntity> {

    private static final double MAX_REACH_DISTANCE_SQ = 4.5 * 4.5;
    private static final double PREFERRED_REACH_DISTANCE_SQ = 3.0 * 3.0;
    private float miningProgress;
    private int lastBreakProgress = -1;
    private int retryCounter = 0;
    private BlockPos targetPos;
    private int miningTicks;

    public MineBlock() {
        super(ImmutableMap.of(
                IRobotMemoryModuleTypes.MINE_TARGET_POS.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected void start(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        this.miningProgress = 0;
        this.lastBreakProgress = -1;
        this.retryCounter = 0;
        this.miningTicks = 0;
        robot.getBrain().getMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get()).ifPresent(globalPos -> {
            this.targetPos = globalPos.pos();
            robot.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.targetPos));
            BlockState blockState = level.getBlockState(this.targetPos);
            robot.setBestToolForBlock(blockState);
        });
        robot.setMining(true);
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return this.miningTicks > IRobotConfig.MINE_BLOCK_TIMEOUT_TICKS.get();
    }

    @Override
    protected void stop(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        robot.setMining(false);
        if (this.targetPos != null) {
            level.destroyBlockProgress(robot.getId(), this.targetPos, -1);
        }
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get());
        robot.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        this.targetPos = null;
    }

    @Override
    protected void tick(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        Optional<GlobalPos> minePosOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get());
        if (minePosOpt.isEmpty() || !minePosOpt.get().pos().equals(this.targetPos)) {
            doStop(level, robot, gameTime);
            return;
        }

        BlockPos currentTargetPos = minePosOpt.get().pos();
        BlockState blockState = level.getBlockState(currentTargetPos);
        if (blockState.isAir() || blockState.getDestroySpeed(level, currentTargetPos) == -1.0F) { // Check for unbreakable blocks
            doStop(level, robot, gameTime);
            return;
        }

        double distanceSq = robot.position().distanceToSqr(Vec3.atCenterOf(currentTargetPos));
        if (distanceSq > MAX_REACH_DISTANCE_SQ) {
            doStop(level, robot, gameTime);
            return;
        }

        if (distanceSq > PREFERRED_REACH_DISTANCE_SQ) {
            if (retryCounter < IRobotConfig.MINE_BLOCK_RETRY_ATTEMPTS.get()) {
                robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(currentTargetPos, 0.5f, 1));
                retryCounter++;
                return; // Let MoveToTargetSink handle moving
            } else {
                doStop(level, robot, gameTime); // Gave up trying to get closer
                return;
            }
        }

        robot.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET); // We are close enough, stop moving.
        this.miningTicks++;

        float destroySpeed = robot.getDestroySpeed(blockState);
        float blockHardness = blockState.getDestroySpeed(level, currentTargetPos);

        if (blockHardness == 0) { // Instantly breakable
            this.miningProgress = 1.0F;
        } else {
            int i = robot.hasCorrectToolForDrops(blockState) ? 30 : 100;
            float progressPerTick = destroySpeed / blockHardness / i;
            this.miningProgress += progressPerTick;
        }

        int currentBreakProgress = (int) (this.miningProgress * 10.0F);
        if (currentBreakProgress != this.lastBreakProgress) {
            level.destroyBlockProgress(robot.getId(), currentTargetPos, currentBreakProgress);
            this.lastBreakProgress = currentBreakProgress;
        }

        if (this.miningProgress >= 1.0F) {
            if (level.destroyBlock(currentTargetPos, robot.hasCorrectToolForDrops(blockState), robot)) {
                robot.setEnergy(robot.getEnergy() - (IRobotConfig.MINING_ENERGY_COST_PER_BLOCK.get() * robot.getEnergyConsumptionModifier()));
            }
            doStop(level, robot, gameTime);
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        if (this.targetPos == null) {
            return false;
        }
        Optional<GlobalPos> minePos = robot.getBrain().getMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get());
        if (minePos.isEmpty() || !minePos.get().pos().equals(this.targetPos)) {
            return false;
        }
        if (level.getBlockState(this.targetPos).isAir()) {
            return false;
        }
        return robot.position().distanceToSqr(Vec3.atCenterOf(this.targetPos)) <= MAX_REACH_DISTANCE_SQ;
    }
}