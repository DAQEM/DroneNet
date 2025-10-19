package com.daqem.irobot.entity.ai.behavior.woodcutting;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.config.IRobotConfig;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.util.TreeUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Set;

public class CutDownTree extends Behavior<MiniRobotEntity> {

    private static final double MAX_REACH_DISTANCE_SQ = 3.5 * 3.5;
    private static final double PREFERRED_REACH_DISTANCE_SQ = 2.5 * 2.5;
    private int ticksSinceStarted;
    private int totalTicksToCut;
    private BlockPos targetPos;
    private int retryCounter = 0;

    public CutDownTree() {
        super(ImmutableMap.of(
                IRobotMemoryModuleTypes.TREE_TARGET_POS.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return this.ticksSinceStarted > IRobotConfig.CUT_TREE_TIMEOUT_TICKS.get();
    }

    @Override
    protected void start(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        this.ticksSinceStarted = 0;
        this.retryCounter = 0;
        robot.getBrain().getMemory(IRobotMemoryModuleTypes.TREE_TARGET_POS.get()).ifPresent(globalPos -> {
            this.targetPos = globalPos.pos();
            robot.setMining(true);
            robot.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.targetPos));

            BlockState blockState = level.getBlockState(this.targetPos);
            robot.setBestToolForBlock(blockState);
            Set<BlockPos> treeBlocks = TreeUtils.getTreeBlocks(level, this.targetPos);

            float destroySpeed = robot.getDestroySpeed(blockState);
            float blockHardness = blockState.getDestroySpeed(level, this.targetPos);

            if (blockHardness <= 0) {
                this.totalTicksToCut = treeBlocks.isEmpty() ? 20 : treeBlocks.size();
            } else {
                int i = robot.hasCorrectToolForDrops(blockState) ? 30 : 100;
                float ticksPerBlock = (blockHardness * i) / destroySpeed;
                this.totalTicksToCut = (int) (treeBlocks.size() * ticksPerBlock);
            }
        });
    }

    @Override
    protected void stop(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        robot.setMining(false);
        if (this.targetPos != null) {
            level.destroyBlockProgress(robot.getId(), this.targetPos, -1);
        }
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.TREE_TARGET_POS.get());
        robot.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        this.targetPos = null;
    }

    @Override
    protected void tick(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        Optional<GlobalPos> treePosOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.TREE_TARGET_POS.get());
        if (treePosOpt.isEmpty() || !treePosOpt.get().pos().equals(this.targetPos)) {
            doStop(level, robot, gameTime);
            return;
        }

        double distanceSq = robot.position().distanceToSqr(Vec3.atCenterOf(this.targetPos));
        if (distanceSq > MAX_REACH_DISTANCE_SQ) {
            doStop(level, robot, gameTime);
            return;
        }

        if (distanceSq > PREFERRED_REACH_DISTANCE_SQ) {
            if (retryCounter < IRobotConfig.CUT_TREE_RETRY_ATTEMPTS.get()) {
                robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.targetPos, 0.5f, 1));
                retryCounter++;
                return; // Let MoveToTargetSink handle moving
            } else {
                doStop(level, robot, gameTime); // Gave up trying to get closer
                return;
            }
        }

        robot.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET); // We are close enough, stop moving.

        this.ticksSinceStarted++;
        if (this.totalTicksToCut > 0) {
            int progress = (int) ((this.ticksSinceStarted / (float) this.totalTicksToCut) * 10);
            level.destroyBlockProgress(robot.getId(), this.targetPos, progress);
        }

        if (this.ticksSinceStarted >= this.totalTicksToCut) {
            Set<BlockPos> treeBlocks = TreeUtils.getTreeBlocks(level, this.targetPos);
            BlockState logState = level.getBlockState(this.targetPos);
            boolean canHarvest = robot.hasCorrectToolForDrops(logState);

            for (BlockPos pos : treeBlocks) {
                level.destroyBlock(pos, canHarvest, robot);
            }
            robot.setEnergy(robot.getEnergy() - (treeBlocks.size() * IRobotConfig.WOODCUTTING_ENERGY_COST_PER_LOG.get()) * robot.getEnergyConsumptionModifier());
            doStop(level, robot, gameTime);
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        if (this.targetPos == null) {
            return false;
        }
        Optional<GlobalPos> treePos = robot.getBrain().getMemory(IRobotMemoryModuleTypes.TREE_TARGET_POS.get());
        if (treePos.isEmpty() || !treePos.get().pos().equals(this.targetPos)) {
            return false;
        }
        if (!level.getBlockState(this.targetPos).is(BlockTags.LOGS)) {
            return false;
        }
        return robot.position().distanceToSqr(Vec3.atCenterOf(this.targetPos)) <= MAX_REACH_DISTANCE_SQ;
    }
}