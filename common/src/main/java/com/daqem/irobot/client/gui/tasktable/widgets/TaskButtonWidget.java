package com.daqem.irobot.client.gui.tasktable.widgets;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.client.gui.tasktable.TaskTableScreenState;
import com.daqem.irobot.entity.task.RobotTask;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;

public class TaskButtonWidget extends CustomButtonWidget {

    private final RobotTask task;
    private final TaskTableScreenState state;

    public TaskButtonWidget(int x, int y, RobotTask task, TaskTableScreenState state) {
        super(x, y, 14, 14, task.getName(), new WidgetSprites(
                IRobot.getId("tasktable/button"),
                IRobot.getId("tasktable/button_selected"),
                IRobot.getId("tasktable/button_hovered")
        ), button -> state.setSelectedTask(task));
        this.task = task;
        this.state = state;

        this.setTooltip(Tooltip.create(this.task.getName()));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.active = this.state.getSelectedTask() != this.task;
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderString(GuiGraphics guiGraphics, Font font, int color) {
    }
}
