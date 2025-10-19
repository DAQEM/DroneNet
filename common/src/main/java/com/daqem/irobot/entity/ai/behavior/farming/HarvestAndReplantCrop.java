package com.daqem.irobot.entity.ai.behavior.farming;

import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.item.module.ModuleItem;
import com.daqem.irobot.util.CropUtils;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class HarvestAndReplantCrop extends Behavior<MiniRobotEntity> {

    private int ticksSinceStarted;
    private BlockPos targetPos;

    public HarvestAndReplantCrop() {
        super(ImmutableMap.of(
                IRobotMemoryModuleTypes.FARM_TARGET_POS.get(), MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MiniRobotEntity robot) {
        Optional<GlobalPos> farmPos = robot.getBrain().getMemory(IRobotMemoryModuleTypes.FARM_TARGET_POS.get());
        boolean hasPos = farmPos.isPresent();
        if (hasPos) {
            boolean isCloseEnough = farmPos.get().pos().closerThan(robot.blockPosition(), 2.0);
            if (!isCloseEnough) {
                robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.FARM_TARGET_POS.get());
            }
            return isCloseEnough;
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        this.ticksSinceStarted = 0;
        robot.getBrain().getMemory(IRobotMemoryModuleTypes.FARM_TARGET_POS.get()).ifPresent(globalPos -> {
            this.targetPos = globalPos.pos();
            robot.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.targetPos));
            robot.setFarming(true);
        });
    }

    @Override
    protected void stop(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.FARM_TARGET_POS.get());
        robot.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        this.targetPos = null;
        robot.setFarming(false);
    }

    @Override
    protected void tick(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        if (this.targetPos == null) {
            this.doStop(level, robot, gameTime);
            return;
        }

        this.ticksSinceStarted++;

        // Give it a moment to look at the block
        if (this.ticksSinceStarted < 20) {
            return;
        }

        BlockState cropState = level.getBlockState(this.targetPos);

        // Case 1: Harvest a mature crop
        if (CropUtils.isMature(cropState)) {
            Optional<Item> seedItemOpt = CropUtils.getSeedFromCrop(cropState);

            robot.swing(InteractionHand.MAIN_HAND);
            level.destroyBlock(this.targetPos, true, robot);
            robot.setEnergy(robot.getEnergy() - (1 * robot.getEnergyConsumptionModifier())); // Small energy cost for harvesting

            if (robot.hasModule(ModuleItem.ModuleType.CROP_REPLANT)) {
                seedItemOpt.ifPresent(seedItem -> {
                    if (tryReplant(level, robot, this.targetPos, seedItem)) {
                        robot.swing(InteractionHand.MAIN_HAND);
                        robot.setEnergy(robot.getEnergy() - (1 * robot.getEnergyConsumptionModifier())); // Small energy cost for replanting
                    }
                });
            }

            // Case 2: Plant on empty farmland
        } else if (cropState.isAir() && (level.getBlockState(this.targetPos.below()).is(Blocks.FARMLAND) || level.getBlockState(this.targetPos.below()).is(Blocks.SOUL_SAND))) {
            if (robot.hasModule(ModuleItem.ModuleType.CROP_REPLANT)) {
                Optional<Item> seedToPlant = findSeedInInventory(robot, level, this.targetPos);
                seedToPlant.ifPresent(seedItem -> {
                    if (tryReplant(level, robot, this.targetPos, seedItem)) {
                        robot.swing(InteractionHand.MAIN_HAND);
                        robot.setEnergy(robot.getEnergy() - (1 * robot.getEnergyConsumptionModifier())); // Small energy cost for planting
                    }
                });
            }
        }

        // Done with this crop
        this.doStop(level, robot, gameTime);
    }

    private Optional<Item> findSeedInInventory(MiniRobotEntity robot, ServerLevel level, BlockPos plantPos) {
        for (int i = 0; i < robot.getInventory().getContainerSize(); i++) {
            ItemStack stack = robot.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem && CropUtils.isPlantable(stack.getItem())) {
                if (blockItem.getBlock().defaultBlockState().canSurvive(level, plantPos)) {
                    return Optional.of(stack.getItem());
                }
            }
        }
        return Optional.empty();
    }

    private boolean tryReplant(ServerLevel level, MiniRobotEntity robot, BlockPos pos, Item seedItem) {
        if (!(seedItem instanceof BlockItem blockItem)) {
            return false;
        }

        int seedSlot = -1;
        for (int i = 0; i < robot.getInventory().getContainerSize(); i++) {
            if (robot.getInventory().getItem(i).is(seedItem)) {
                seedSlot = i;
                break;
            }
        }

        if (seedSlot == -1) {
            // No seeds to replant
            return false;
        }

        if (level.getBlockState(pos).isAir()) {
            BlockState seedState = blockItem.getBlock().defaultBlockState();
            if (seedState.canSurvive(level, pos)) {
                level.setBlock(pos, seedState, 3);
                robot.getInventory().getItem(seedSlot).shrink(1);
                return true;
            }
        }

        return false;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        if (this.targetPos == null) {
            return false;
        }
        Optional<GlobalPos> farmPos = robot.getBrain().getMemory(IRobotMemoryModuleTypes.FARM_TARGET_POS.get());
        if (farmPos.isEmpty() || !farmPos.get().pos().equals(this.targetPos)) {
            return false;
        }

        BlockState targetState = level.getBlockState(this.targetPos);
        BlockState groundState = level.getBlockState(this.targetPos.below());

        boolean isMatureCrop = CropUtils.isMature(targetState);
        boolean isEmptyFarmland = targetState.isAir() && (groundState.is(Blocks.FARMLAND) || groundState.is(Blocks.SOUL_SAND));

        return isMatureCrop || isEmptyFarmland;
    }
}