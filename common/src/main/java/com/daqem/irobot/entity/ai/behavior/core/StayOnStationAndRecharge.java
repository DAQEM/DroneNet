package com.daqem.irobot.entity.ai.behavior.core;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.block.IRobotBlocks;
import com.daqem.irobot.entity.IRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.entity.task.RobotTask;
import com.daqem.irobot.level.poi.IRobotPoiTypes;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

public class StayOnStationAndRecharge extends Behavior<IRobotEntity> {

    public StayOnStationAndRecharge() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, IRobotEntity robot) {
        return level.getBlockState(robot.blockPosition()).is(IRobotBlocks.ROBOT_STATION.get());
    }

    @Override
    protected boolean canStillUse(ServerLevel level, IRobotEntity robot, long gameTime) {
        boolean isNotFull = robot.getEnergy() < robot.getMaxEnergy();
        if (!isNotFull) {
            IRobot.LOGGER.info("StayOnStationAndRecharge is full");
        }
        boolean isAtRobotStation = level.getBlockState(robot.blockPosition()).is(IRobotBlocks.ROBOT_STATION.get());
        if (!isAtRobotStation) {
            IRobot.LOGGER.info("StayOnStationAndRecharge robot moved from station");
        }
        return isNotFull && isAtRobotStation;
    }

    @Override
    protected void start(ServerLevel level, IRobotEntity entity, long gameTime) {
        // Reserve the station
        BlockPos stationPos = entity.blockPosition();
        level.getPoiManager().take(
                poiTypeHolder -> poiTypeHolder.is(IRobotPoiTypes.ROBOT_STATION.getKey()),
                (poiTypeHolder, blockPos) -> blockPos.equals(stationPos),
                stationPos,
                0
        );

        // Remember the station position
        entity.getBrain().setMemory(IRobotMemoryModuleTypes.STATION_POS.get(), new GlobalPos(level.dimension(), stationPos));
    }

    @Override
    protected boolean timedOut(long gameTime) {
        return false;
    }

    @Override
    protected void stop(ServerLevel level, IRobotEntity robot, long gameTime) {
        // Recharging is done, release the station
        robot.getBrain().getMemory(IRobotMemoryModuleTypes.STATION_POS.get()).ifPresent(globalPos -> {
            level.getPoiManager().release(globalPos.pos());
        });
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.STATION_POS.get());

        // Resume previous task
        RobotTask previousTask = robot.getBrain().getMemory(IRobotMemoryModuleTypes.ASSIGNED_TASK.get()).orElse(null);
        if (previousTask == null) {
            robot.getBrain().setActiveActivityIfPossible(Activity.IDLE);
        } else {
            robot.getBrain().setActiveActivityIfPossible(previousTask.getActivity());
        }
    }
}