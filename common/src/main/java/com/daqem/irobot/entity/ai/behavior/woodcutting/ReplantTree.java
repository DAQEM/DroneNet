package com.daqem.irobot.entity.ai.behavior.woodcutting;

import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class ReplantTree extends Behavior<MiniRobotEntity> {

    public ReplantTree() {
        super(ImmutableMap.of(
                IRobotMemoryModuleTypes.REPLANT_POS.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.SAPLING_TO_PLANT.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MiniRobotEntity robot) {
        return robot.getBrain().getMemory(IRobotMemoryModuleTypes.TREE_TARGET_POS.get()).isEmpty();
    }

    @Override
    protected void start(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        Optional<BlockPos> replantPosOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.REPLANT_POS.get());
        Optional<Item> saplingItemOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.SAPLING_TO_PLANT.get());

        if (replantPosOpt.isPresent() && saplingItemOpt.isPresent()) {
            BlockPos replantPos = replantPosOpt.get();
            Item saplingItem = saplingItemOpt.get();

            if (!robot.getInventory().contains(saplingItem.getDefaultInstance())) {
                this.doStop(level, robot, gameTime);
                return;
            }

            if (!replantPos.closerThan(robot.blockPosition(), 3.0)) {
                robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(replantPos, 0.5f, 1));
                return;
            }

            if (tryReplant(level, robot, replantPos, saplingItem)) {
                robot.swing(InteractionHand.MAIN_HAND);
            }
        }
        this.doStop(level, robot, gameTime);
    }

    private boolean tryReplant(ServerLevel level, MiniRobotEntity robot, BlockPos pos, Item saplingItem) {
        if (!(saplingItem instanceof BlockItem)) {
            return false;
        }

        int saplingSlot = -1;
        for (int i = 0; i < robot.getInventory().getContainerSize(); i++) {
            if (robot.getInventory().getItem(i).is(saplingItem)) {
                saplingSlot = i;
                break;
            }
        }

        if (saplingSlot == -1) {
            return false;
        }

        BlockState saplingState = ((BlockItem) saplingItem).getBlock().defaultBlockState();
        if (level.getBlockState(pos).canBeReplaced() && saplingState.canSurvive(level, pos)) {
            level.setBlock(pos, saplingState, 3);
            robot.getInventory().getItem(saplingSlot).shrink(1);
            return true;
        }

        return false;
    }

    @Override
    protected void stop(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.REPLANT_POS.get());
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.SAPLING_TO_PLANT.get());
    }
}