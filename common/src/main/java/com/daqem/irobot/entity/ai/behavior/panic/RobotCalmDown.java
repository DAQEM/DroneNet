package com.daqem.irobot.entity.ai.behavior.panic;

import com.daqem.irobot.entity.MiniRobotEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

public class RobotCalmDown extends Behavior<MiniRobotEntity> {

    private static final int CALM_DOWN_DURATION = 100; // 5 seconds
    private long calmDownStartedAt;

    public RobotCalmDown() {
        super(ImmutableMap.of(
                MemoryModuleType.HURT_BY, MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected void start(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        this.calmDownStartedAt = gameTime;
    }

    @Override
    protected void tick(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        if (gameTime - this.calmDownStartedAt >= CALM_DOWN_DURATION) {
            this.stop(level, robot, gameTime);
        }
    }

    @Override
    protected void stop(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        robot.getBrain().eraseMemory(MemoryModuleType.HURT_BY);
        robot.getBrain().eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
        if (robot.getBrain().isActive(Activity.PANIC)) {
            robot.getBrain().updateActivityFromSchedule(level.getDayTime(), level.getGameTime());
        }
    }

    @Override
    protected boolean canStillUse(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        return this.checkExtraStartConditions(level, robot) && robot.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
    }
}