package com.daqem.irobot.client.gui.tasktable;

import com.daqem.irobot.entity.task.RobotTask;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class TaskTableScreenState {

    private final int containerId;
    private @Nullable RobotTask selectedTask;

    public TaskTableScreenState(int containerId) {
        this.containerId = containerId;
    }

    public int getContainerId() {
        return containerId;
    }

    public @Nullable RobotTask getSelectedTask() {
        return selectedTask;
    }

    public void setSelectedTask(@Nullable RobotTask selectedTask) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gameMode != null && selectedTask != null) {
            minecraft.gameMode.handleInventoryButtonClick(this.containerId, selectedTask.ordinal());
        }

        this.selectedTask = selectedTask;
    }
}
