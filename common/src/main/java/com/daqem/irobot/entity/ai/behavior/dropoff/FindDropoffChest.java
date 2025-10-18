package com.daqem.irobot.entity.ai.behavior.dropoff;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.entity.IRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.level.poi.IRobotPoiTypes;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

public class FindDropoffChest extends Behavior<IRobotEntity> {

    public FindDropoffChest() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected void start(ServerLevel level, IRobotEntity robot, long gameTime) {
        level.getPoiManager().find(
                poiTypeHolder -> poiTypeHolder.is(IRobotPoiTypes.DROPOFF_CHEST.getKey()),
                blockPos -> level.getBlockEntity(blockPos) instanceof Container container && robot.getInventory().canAddItem(container),
                robot.blockPosition(),
                128,
                PoiManager.Occupancy.ANY
        ).ifPresentOrElse(poiPos -> {
            robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(poiPos, 0.5f, 1));
            robot.getBrain().setMemory(IRobotMemoryModuleTypes.DROPOFF_TARGET_POS.get(), GlobalPos.of(level.dimension(), poiPos));
        }, () -> {
            if (robot.getOwner() instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(IRobot.translatable("robot.error.cant_find_dropoff", robot.getDisplayName()));
            }
            robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.NEEDS_TO_DROPOFF.get());
            robot.getBrain().setActiveActivityIfPossible(Activity.REST);
        });
    }
}