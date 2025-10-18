package com.daqem.irobot.entity.ai;

import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.behavior.charging.FindRechargeStation;
import com.daqem.irobot.entity.ai.behavior.charging.StayOnStationAndRecharge;
import com.daqem.irobot.entity.ai.behavior.core.GoToChargingActivity;
import com.daqem.irobot.entity.ai.behavior.core.GoToDropOffActivity;
import com.daqem.irobot.entity.ai.behavior.core.GoToRestActivity;
import com.daqem.irobot.entity.ai.behavior.core.PickUpItemsAround;
import com.daqem.irobot.entity.ai.behavior.dropoff.DepositItemsAtDropoff;
import com.daqem.irobot.entity.ai.behavior.dropoff.FindDropoffChest;
import com.daqem.irobot.entity.ai.behavior.idle.GoToTaskActivity;
import com.daqem.irobot.entity.ai.behavior.mining.FindNextBlockToMine;
import com.daqem.irobot.entity.ai.behavior.mining.MineBlock;
import com.daqem.irobot.entity.ai.behavior.panic.RobotCalmDown;
import com.daqem.irobot.entity.ai.behavior.panic.RobotPanicTrigger;
import com.daqem.irobot.entity.ai.behavior.resting.GoToIdleActivity;
import com.daqem.irobot.entity.ai.behavior.woodcutting.CutDownTree;
import com.daqem.irobot.entity.ai.behavior.woodcutting.FindNextTreeToCut;
import com.daqem.irobot.entity.ai.behavior.woodcutting.ReplantTree;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

import java.util.ArrayList;
import java.util.List;

public class RobotBrainPackages {

    public static Brain.Provider<MiniRobotEntity> createBrainProvider() {
        return Brain.provider(
                MiniRobotEntity.MEMORY_TYPES,
                MiniRobotEntity.SENSOR_TYPES
        );
    }

    public static void registerBrainGoals(Brain<MiniRobotEntity> brain) {
        brain.addActivity(Activity.CORE, getCorePackage());
        brain.addActivityWithConditions(IRobotActivities.PROTECT.get(), getProtectingPackage(),
                ImmutableSet.of(Pair.of(IRobotMemoryModuleTypes.ASSIGNED_TASK.get(), MemoryStatus.VALUE_PRESENT))
        );
        brain.addActivityWithConditions(IRobotActivities.MINE.get(), getMiningPackage(),
                ImmutableSet.of(Pair.of(IRobotMemoryModuleTypes.ASSIGNED_TASK.get(), MemoryStatus.VALUE_PRESENT))
        );
        brain.addActivityWithConditions(IRobotActivities.CUT_WOOD.get(), getWoodcuttingPackage(),
                ImmutableSet.of(Pair.of(IRobotMemoryModuleTypes.ASSIGNED_TASK.get(), MemoryStatus.VALUE_PRESENT))
        );
        brain.addActivityWithConditions(IRobotActivities.FARM.get(), getFarmingPackage(),
                ImmutableSet.of(Pair.of(IRobotMemoryModuleTypes.ASSIGNED_TASK.get(), MemoryStatus.VALUE_PRESENT))
        );
        brain.addActivityWithConditions(IRobotActivities.FOLLOW.get(), getFollowingPackage(),
                ImmutableSet.of(Pair.of(IRobotMemoryModuleTypes.ASSIGNED_TASK.get(), MemoryStatus.VALUE_PRESENT))
        );
        brain.addActivity(IRobotActivities.DROPOFF.get(), getDropoffPackage());
        brain.addActivity(IRobotActivities.RECHARGE.get(), getRechargePackage());
        brain.addActivity(Activity.IDLE, getIdlePackage());
        brain.addActivity(Activity.PANIC, getPanicPackage());
        brain.addActivity(Activity.REST, getRestPackage());

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.setActiveActivityIfPossible(Activity.IDLE);
    }

    private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> getCorePackage() {
        return ImmutableList.of(
                Pair.of(0, new GoToRestActivity())
        );
    }

    private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> getIdlePackage() {
        List<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> behaviors = new ArrayList<>(getDefaultPackage());
        behaviors.addAll(List.of(
                Pair.of(0, new GoToTaskActivity()),
                Pair.of(1, new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F)),
                Pair.of(3, new GoToDropOffActivity(robot -> robot.getInventory().hasItemsToDropOff()))

        ));
        return ImmutableList.copyOf(behaviors);
    }

    private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> getProtectingPackage() {
        List<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> behaviors = new ArrayList<>(getDefaultPackage());
        behaviors.addAll(List.of());
        return ImmutableList.copyOf(behaviors);
    }

    private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> getMiningPackage() {
        List<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> behaviors = new ArrayList<>(getDefaultPackage());
        behaviors.addAll(List.of(
                Pair.of(2, new FindNextBlockToMine()),
                Pair.of(3, new MineBlock())
        ));
        return ImmutableList.copyOf(behaviors);
    }

    private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> getWoodcuttingPackage() {
        List<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> behaviors = new ArrayList<>(getDefaultPackage());
        behaviors.addAll(List.of(
                Pair.of(2, new FindNextTreeToCut()),
                Pair.of(3, new CutDownTree()),
                Pair.of(4, new ReplantTree())
        ));
        return ImmutableList.copyOf(behaviors);
    }

    private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> getFarmingPackage() {
        List<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> behaviors = new ArrayList<>(getDefaultPackage());
        behaviors.addAll(List.of());
        return ImmutableList.copyOf(behaviors);
    }

    private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> getFollowingPackage() {
        List<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> behaviors = new ArrayList<>(getDefaultPackage());
        behaviors.addAll(List.of());
        return ImmutableList.copyOf(behaviors);
    }

    private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> getRechargePackage() {
        List<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> behaviors = new ArrayList<>(getDefaultPackage());
        behaviors.addAll(List.of(
                Pair.of(0, new FindRechargeStation()),
                Pair.of(1, new StayOnStationAndRecharge())
        ));
        return ImmutableList.copyOf(behaviors);
    }

    private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> getDropoffPackage() {
        List<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> behaviors = new ArrayList<>(getDefaultPackage());
        behaviors.addAll(List.of(
                Pair.of(0, new FindDropoffChest()),
                Pair.of(1, new DepositItemsAtDropoff())
        ));
        return ImmutableList.copyOf(behaviors);
    }

    private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> getRestPackage() {
        return ImmutableList.of(
                Pair.of(0, new GoToIdleActivity())
        );
    }

    public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> getPanicPackage() {
        float speed = 0.75F;
        List<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> behaviors = new ArrayList<>(getDefaultPackage());
        behaviors.addAll(List.of(
                Pair.of(0, new RobotCalmDown()),
                Pair.of(1, SetWalkTargetAwayFrom.entity(MemoryModuleType.HURT_BY_ENTITY, speed, 6, false)),
                Pair.of(2, RandomStroll.stroll(speed, 2, 2))
        ));
        return ImmutableList.copyOf(behaviors);
    }

    private static List<Pair<Integer, ? extends BehaviorControl<? super MiniRobotEntity>>> getDefaultPackage() {
        return List.of(
                Pair.of(0, new Swim<>(0.8F)),
                Pair.of(0, InteractWithDoor.create()),
                Pair.of(0, new LookAtTargetSink(45, 90)),
                Pair.of(0, new RobotPanicTrigger()),
                Pair.of(1, new MoveToTargetSink() {
                    @Override
                    protected boolean checkExtraStartConditions(ServerLevel level, Mob owner) {
                        return super.checkExtraStartConditions(level, owner) && owner instanceof MiniRobotEntity robot && robot.getEnergy() > 0;
                    }

                    @Override
                    protected boolean canStillUse(ServerLevel level, Mob entity, long gameTime) {
                        return super.canStillUse(level, entity, gameTime) && entity instanceof MiniRobotEntity robot && robot.getEnergy() > 0;
                    }
                }),
                Pair.of(2, new GoToChargingActivity()),
                Pair.of(3, new GoToDropOffActivity(robot -> robot.getInventory().isMainInventoryFull() && robot.getInventory().hasItemsToDropOff())),
                Pair.of(4, new PickUpItemsAround())
        );
    }
}