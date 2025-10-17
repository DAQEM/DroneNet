package com.daqem.irobot.entity.ai.behavior.core;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.level.poi.IRobotPoiTypes;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.schedule.Activity;

public class FindRechargeStation extends Behavior<MiniRobotEntity> {

    public FindRechargeStation() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, MiniRobotEntity robot) {
        return robot.needsRecharging();
    }

    @Override
    protected void start(ServerLevel level, MiniRobotEntity robot, long gameTime) {
        level.getPoiManager().findClosestWithType(
                poiTypeHolder -> poiTypeHolder.is(IRobotPoiTypes.ROBOT_STATION.getKey()),
                robot.blockPosition(),
                128,
                PoiManager.Occupancy.HAS_SPACE
        ).ifPresentOrElse(pair ->
                        robot.getBrain().setMemory(
                                MemoryModuleType.WALK_TARGET,
                                new WalkTarget(
                                        pair.getSecond(),
                                        1.0f,
                                        0
                                )),
                () -> {
                    robot.getBrain().setActiveActivityIfPossible(Activity.REST);
                    if (robot.getOwner() instanceof ServerPlayer serverPlayer) {
                        serverPlayer.sendSystemMessage(IRobot.translatable("robot.error.cant_find_station", robot.getDisplayName(), robot.getX(), robot.getY(), robot.getZ()));
                    }
                });
    }
}
