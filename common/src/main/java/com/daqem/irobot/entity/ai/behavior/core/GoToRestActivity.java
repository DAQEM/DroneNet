package com.daqem.irobot.entity.ai.behavior.core;

import com.daqem.irobot.entity.IRobotEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Map;

public class GoToRestActivity extends Behavior<IRobotEntity> {

    public GoToRestActivity() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, IRobotEntity owner) {
        return owner.getEnergy() <= 0;
    }

    @Override
    protected void start(ServerLevel level, IRobotEntity entity, long gameTime) {
        entity.getBrain().setActiveActivityIfPossible(Activity.REST);
    }
}
