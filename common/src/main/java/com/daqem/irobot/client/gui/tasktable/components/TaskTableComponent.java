package com.daqem.irobot.client.gui.tasktable.components;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.client.gui.tasktable.TaskTableScreenState;
import com.daqem.irobot.client.gui.tasktable.widgets.TaskButtonWidget;
import com.daqem.irobot.entity.task.RobotTask;
import com.daqem.uilib.gui.component.sprite.SpriteComponent;

public class TaskTableComponent extends SpriteComponent {

    public TaskTableComponent(TaskTableScreenState state) {
        super(0, 0, 352, 222, IRobot.getId("tasktable/background"));

        for (int i = 0; i < RobotTask.values().length; i++) {
            this.addWidget(new TaskButtonWidget(109 + i * 17, 80, RobotTask.values()[i], state));
        }
    }
}
