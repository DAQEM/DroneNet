package com.daqem.irobot.entity.ai.behavior;

import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.RobotInventory;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

import java.util.Comparator;
import java.util.Optional;

public class MineBlock extends Behavior<MiniRobotEntity> {

    private int miningTicks;
    private int lastBreakProgress = -1;
    private static final double MAX_REACH_DISTANCE_SQ = 4.5 * 4.5;
    private static final double PREFERRED_REACH_DISTANCE_SQ = 2.0 * 2.0;

    public MineBlock() {
        super(ImmutableMap.of(
                IRobotMemoryModuleTypes.MINE_TARGET_POS.get(), MemoryStatus.VALUE_PRESENT
        ));
    }

    private void selectBestToolForBlock(MiniRobotEntity robot, BlockState blockState) {
        RobotInventory inventory = robot.getInventory();
        int bestToolSlot = -1;
        float maxSpeed = 1.0F;

        for (int i = 0; i < RobotInventory.INVENTORY_SIZE; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (!itemStack.isEmpty()) {
                float speed = itemStack.getDestroySpeed(blockState);
                if (speed > maxSpeed) {
                    maxSpeed = speed;
                    bestToolSlot = i;
                }
            }
        }

        ItemStack currentSelectedItem = inventory.getSelectedItem();
        if (bestToolSlot == -1 && !currentSelectedItem.isEmpty() && currentSelectedItem.getDestroySpeed(blockState) < 1.0F) {
            for (int i = 0; i < RobotInventory.getSelectionSize(); i++) {
                if (inventory.getItem(i).isEmpty()) {
                    inventory.setSelectedSlot(i);
                    return;
                }
            }
            return;
        }

        if (bestToolSlot == -1) {
            return;
        }

        int selectedSlot = inventory.getSelectedSlot();

        if (bestToolSlot == selectedSlot) {
            return;
        }

        if (RobotInventory.isHotbarSlot(bestToolSlot)) {
            inventory.setSelectedSlot(bestToolSlot);
        } else {
            ItemStack bestToolStack = inventory.getItem(bestToolSlot);
            ItemStack currentSelectedStack = inventory.getItem(selectedSlot);

            inventory.setItem(bestToolSlot, currentSelectedStack);
            inventory.setItem(selectedSlot, bestToolStack);
        }
    }


    @Override
    protected void start(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        this.miningTicks = 0;
        this.lastBreakProgress = -1;
        robot.getBrain().getMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get()).ifPresent(globalPos -> {
            robot.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(globalPos.pos()));
            BlockState blockState = level.getBlockState(globalPos.pos());
            if (!blockState.isAir()) {
                selectBestToolForBlock(robot, blockState);
            }
        });
    }

    @Override
    protected void stop(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        robot.getBrain().getMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get()).ifPresent(
                globalPos -> level.destroyBlockProgress(robot.getId(), globalPos.pos(), -1)
        );
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get());
    }

    @Override
    protected void tick(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        GlobalPos targetGlobalPos = robot.getBrain().getMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get()).get();
        if (targetGlobalPos.dimension() != level.dimension()) {
            stop(level, robot, gameTime);
            return;
        }

        BlockPos targetPos = targetGlobalPos.pos();

        // Check if robot needs to move closer
        if (robot.position().distanceToSqr(targetPos.getCenter()) > MAX_REACH_DISTANCE_SQ) {
            moveToTarget(robot, targetPos);
            return;
        }

        // Attempt to get even closer if not in preferred range
        if (robot.position().distanceToSqr(targetPos.getCenter()) > PREFERRED_REACH_DISTANCE_SQ) {
            Optional<BlockPos> closerPos = findCloserReachablePosition(level, robot, targetPos);
            if (closerPos.isPresent()) {
                moveToTarget(robot, targetPos); // This will path to the best available spot
                return;
            }
        }

        // If close enough, stop moving and start mining
        robot.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);


        BlockState blockState = level.getBlockState(targetPos);
        if (blockState.isAir() || blockState.getDestroySpeed(level, targetPos) < 0) {
            stop(level, robot, gameTime);
            return;
        }

        robot.swing(InteractionHand.MAIN_HAND);

        float destroyProgress;
        float destroySpeed = blockState.getDestroySpeed(level, targetPos);
        if (destroySpeed == 0) { // Unbreakable but not negative (e.g. water)
            stop(level, robot, gameTime);
            return;
        }

        if (robot.hasCorrectToolForDrops(blockState)) {
            destroyProgress = robot.getDestroySpeed(blockState) / destroySpeed / 30F;
        } else {
            destroyProgress = robot.getDestroySpeed(blockState) / destroySpeed / 100F;
        }

        if (destroyProgress > 0) {
            miningTicks++;
            int progress = (int) ((miningTicks * destroyProgress) * 10);

            if (progress != lastBreakProgress && progress < 10) {
                level.destroyBlockProgress(robot.getId(), targetPos, progress);
                lastBreakProgress = progress;
            }

            if (miningTicks * destroyProgress >= 1.0F) {
                level.destroyBlock(targetPos, true, robot);
                stop(level, robot, gameTime);
            }
        } else {
            stop(level, robot, gameTime);
        }
    }

    private void moveToTarget(MiniRobotEntity robot, BlockPos targetPos) {
        Optional<BlockPos> bestPos = findCloserReachablePosition(robot.level(), robot, targetPos);

        if (bestPos.isPresent()) {
            robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(bestPos.get(), 0.5F, 0));
        } else {
            // Fallback: if no adjacent spot is reachable, move towards the block itself from a distance.
            robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, 0.5F, 1));
        }
    }

    private Optional<BlockPos> findCloserReachablePosition(Level level, MiniRobotEntity robot, BlockPos targetPos) {
        return Direction.Plane.HORIZONTAL.stream()
                .map(targetPos::relative)
                .filter(pos -> level.getBlockState(pos).isPathfindable(PathComputationType.LAND)
                        && level.getBlockState(pos.above()).isPathfindable(PathComputationType.LAND))
                .filter(pos -> robot.getNavigation().createPath(pos, 0) != null)
                .min(Comparator.comparingDouble(pos -> pos.distSqr(robot.blockPosition())));
    }


    @Override
    protected boolean canStillUse(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        return robot.getBrain().getMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get()).isPresent();
    }
}