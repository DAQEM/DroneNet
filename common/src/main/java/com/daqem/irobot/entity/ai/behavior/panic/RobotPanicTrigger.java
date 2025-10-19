package com.daqem.irobot.entity.ai.behavior.panic;

import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotActivities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Optional;

public class RobotPanicTrigger extends Behavior<MiniRobotEntity> {

    public RobotPanicTrigger() {
        super(ImmutableMap.of());
    }

    public static boolean isHurt(LivingEntity entity) {
        return entity.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
    }

    private boolean isDoingCombatTask(MiniRobotEntity robot) {
        Optional<Activity> activity = robot.getBrain().getActiveNonCoreActivity();
        if (activity.isEmpty()) {
            return false;
        }
        Activity currentActivity = activity.get();
        return currentActivity.equals(IRobotActivities.PROTECT.get()) || currentActivity.equals(IRobotActivities.FOLLOW.get());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MiniRobotEntity owner) {
        return isHurt(owner) && !isDoingCombatTask(owner);
    }

    protected boolean canStillUse(ServerLevel level, MiniRobotEntity entity, long gameTime) {
        return isHurt(entity);
    }

    protected void start(ServerLevel level, MiniRobotEntity entity, long gameTime) {
        if (isHurt(entity)) {
            Brain<?> brain = entity.getBrain();
            if (!brain.isActive(Activity.PANIC)) {
                brain.eraseMemory(MemoryModuleType.PATH);
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
                brain.eraseMemory(MemoryModuleType.BREED_TARGET);
                brain.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
            }

            brain.setActiveActivityIfPossible(Activity.PANIC);
        }
    }
}