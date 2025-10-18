package com.daqem.irobot.entity.ai.behavior.core;

import com.daqem.irobot.entity.IRobotEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.pathfinder.Path;

public class PickUpItemsAround extends Behavior<IRobotEntity> {

    public PickUpItemsAround() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, IRobotEntity owner) {
        return !owner.getInventory().isMainInventoryFull() && owner.hasItemsAround() && !owner.isMining() && !owner.isCharging() && !owner.isDroppingOffItems() && !owner.isFarming();
    }

    @Override
    protected void start(ServerLevel level, IRobotEntity entity, long gameTime) {
        ItemEntity itemEntity = entity.getNearestItemEntity();
        if (itemEntity != null && itemEntity.isAlive()) {
            Path path = entity.getNavigation().createPath(itemEntity, 0);
            if (path != null && path.canReach()) {
                entity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(itemEntity, 0.5F, 0));
            }
        }
    }
}
