package com.daqem.irobot.entity.ai.behavior.dropoff;

import com.daqem.irobot.entity.IRobotEntity;
import com.daqem.irobot.entity.RobotInventory;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import java.util.Optional;

public class DepositItemsAtDropoff extends Behavior<IRobotEntity> {

    public DepositItemsAtDropoff() {
        super(ImmutableMap.of(
                IRobotMemoryModuleTypes.DROPOFF_TARGET_POS.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, IRobotEntity robot) {
        Optional<GlobalPos> dropoffPos = robot.getBrain().getMemory(IRobotMemoryModuleTypes.DROPOFF_TARGET_POS.get());
        return dropoffPos.isPresent() && dropoffPos.get().pos().closerThan(robot.blockPosition(), 2.0);
    }

    @Override
    protected void start(ServerLevel level, IRobotEntity robot, long gameTime) {
        robot.getBrain().getMemory(IRobotMemoryModuleTypes.DROPOFF_TARGET_POS.get()).ifPresent(globalPos -> {
            BlockEntity blockEntity = level.getBlockEntity(globalPos.pos());
            if (blockEntity instanceof Container container) {
                boolean wasTaskComplete = robot.getBrain().getMemory(IRobotMemoryModuleTypes.ASSIGNED_TASK.get()).isEmpty();

                for (int i = 0; i < RobotInventory.INVENTORY_SIZE; i++) {
                    ItemStack stackInSlot = robot.getInventory().getItem(i);
                    if (!stackInSlot.isEmpty() && !stackInSlot.isDamageableItem()) {
                        ItemStack remainder = HopperBlockEntity.addItem(robot.getInventory(), container, stackInSlot, null);
                        robot.getInventory().setItem(i, remainder);
                    }
                }

                if (wasTaskComplete) {
                    ItemStack taskStack = robot.getInventory().getTask();
                    if (!taskStack.isEmpty()) {
                        HopperBlockEntity.addItem(robot.getInventory(), container, taskStack, null);
                        robot.getInventory().removeItemNoUpdate(RobotInventory.TASK_SLOT_INDEX);
                    }
                }
            }
        });

        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.NEEDS_TO_DROPOFF.get());
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.DROPOFF_TARGET_POS.get());
        robot.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        robot.getBrain().setActiveActivityIfPossible(Activity.IDLE);
    }
}