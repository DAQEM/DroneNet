package com.daqem.irobot.entity.ai.behavior.core;

import com.daqem.irobot.entity.IRobotEntity;
import com.daqem.irobot.entity.ai.IRobotActivities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public class GoToChargingActivity extends Behavior<IRobotEntity> {

    public GoToChargingActivity() {
        super(Map.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, IRobotEntity owner) {
        return owner.needsRecharging();
    }

    @Override
    protected void start(ServerLevel level, IRobotEntity entity, long gameTime) {
        entity.getBrain().setActiveActivityIfPossible(IRobotActivities.RECHARGE.get());
    }
}
