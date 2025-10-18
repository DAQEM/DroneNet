package com.daqem.irobot.entity.ai.behavior.idle;

import com.daqem.irobot.entity.IRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.item.data.TaskDataComponent;
import com.daqem.irobot.item.data.TaskMarkerDataComponent;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GoToTaskActivity extends Behavior<IRobotEntity> {

    public GoToTaskActivity() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, IRobotEntity owner) {
        return owner.hasTaskItem();
    }

    @Override
    protected void start(ServerLevel level, IRobotEntity robot, long gameTime) {
        TaskDataComponent taskData = robot.getTaskItemData();
        TaskMarkerDataComponent markerData = robot.getTaskMarkerData();
        if (taskData != null && markerData != null) {
            robot.getBrain().setMemory(IRobotMemoryModuleTypes.ASSIGNED_TASK.get(), taskData.task());
            robot.getBrain().setMemory(IRobotMemoryModuleTypes.TASK_AREA_START.get(), markerData.getFirstPos());
            robot.getBrain().setMemory(IRobotMemoryModuleTypes.TASK_AREA_END.get(), markerData.getSecondPos());
            robot.getBrain().setActiveActivityIfPossible(taskData.task().getActivity());
        }
    }
}
