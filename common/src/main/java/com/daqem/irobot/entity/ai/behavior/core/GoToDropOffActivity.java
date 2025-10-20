package com.daqem.irobot.entity.ai.behavior.core;

import com.daqem.irobot.entity.IRobotEntity;
import com.daqem.irobot.entity.ai.IRobotActivities;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;
import java.util.function.Predicate;

public class GoToDropOffActivity extends Behavior<IRobotEntity> {

    private final Predicate<IRobotEntity> condition;
    private long lastCheckTime = 0L;

    public GoToDropOffActivity(Predicate<IRobotEntity> condition) {
        super(Map.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT,
                IRobotMemoryModuleTypes.IS_CHARING.get(), MemoryStatus.VALUE_ABSENT
        ));
        this.condition = condition;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, IRobotEntity owner) {
        return condition.test(owner);
    }

    @Override
    protected void start(ServerLevel level, IRobotEntity entity, long gameTime) {
        if (gameTime - this.lastCheckTime < 100L) {
            return;
        }
        entity.getBrain().setActiveActivityIfPossible(IRobotActivities.DROPOFF.get());
        this.lastCheckTime = gameTime;
    }
}
