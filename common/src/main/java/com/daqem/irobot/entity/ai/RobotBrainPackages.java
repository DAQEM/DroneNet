package com.daqem.irobot.entity.ai;

import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.behavior.core.FindRechargeStation;
import com.daqem.irobot.entity.ai.behavior.mining.FindNextBlockToMine;
import com.daqem.irobot.entity.ai.behavior.mining.MineBlock;
import com.daqem.irobot.entity.ai.behavior.panic.RobotCalmDown;
import com.daqem.irobot.entity.ai.behavior.panic.RobotPanicTrigger;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

public class RobotBrainPackages {

    public static Brain.Provider<MiniRobotEntity> createBrainProvider() {
        return Brain.provider(
                MiniRobotEntity.MEMORY_TYPES,
                MiniRobotEntity.SENSOR_TYPES
        );
    }

    public static void registerBrainGoals(Brain<MiniRobotEntity> brain) {
        // Not using schedule because a robot doesn't need to sleep
        brain.addActivity(Activity.CORE, getCorePackage());
        brain.addActivityWithConditions(IRobotActivities.MINE.get(), getMiningPackage(0.5F),
                ImmutableSet.of(Pair.of(IRobotMemoryModuleTypes.ASSIGNED_TASK.get(), MemoryStatus.VALUE_PRESENT))
        );
        brain.addActivity(Activity.IDLE, getIdlePackage());
        brain.addActivity(Activity.PANIC, getPanicPackage(0.5F));

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.setActiveActivityIfPossible(Activity.IDLE);
    }

    private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> getCorePackage() {
        return ImmutableList.of(
                Pair.of(0, new Swim<>(0.8F)),
                Pair.of(0, InteractWithDoor.create()),
                Pair.of(0, new LookAtTargetSink(45, 90)),
                Pair.of(0, new RobotPanicTrigger()),
                Pair.of(1, new MoveToTargetSink()),
                Pair.of(2, new FindRechargeStation())
        );
    }

    private static ImmutableList<Pair<Integer, ? extends Behavior<? super MiniRobotEntity>>> getIdlePackage() {
        return ImmutableList.of(
                Pair.of(1, new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F))
        );
    }

    private static ImmutableList<Pair<Integer, ? extends Behavior<? super MiniRobotEntity>>> getMiningPackage(float speedModifier) {
        return ImmutableList.of(
                Pair.of(2, new FindNextBlockToMine()),
                Pair.of(3, new MineBlock())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> getPanicPackage(float speedModifier) {
        float f = speedModifier * 1.5F;
        return ImmutableList.of(
                Pair.of(0, new RobotCalmDown()),
                Pair.of(1, SetWalkTargetAwayFrom.entity(MemoryModuleType.HURT_BY_ENTITY, f, 6, false)),
                Pair.of(2, RandomStroll.stroll(f, 2, 2)),
                getMinimalLookBehavior()
        );
    }

    private static Pair<Integer, BehaviorControl<LivingEntity>> getMinimalLookBehavior() {
        return Pair.of(
                5,
                new RunOne<>(
                        ImmutableList.of(
                                Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 2),
                                Pair.of(new DoNothing(30, 60), 8)
                        )
                )
        );
    }
}