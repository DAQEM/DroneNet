package com.daqem.irobot.client.gui.tasktable;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.client.gui.tasktable.components.TaskTableComponent;
import com.daqem.irobot.menu.TaskTableMenu;
import com.daqem.uilib.gui.AbstractContainerScreen;
import com.daqem.uilib.gui.background.BlurredBackground;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TaskTableScreen extends AbstractContainerScreen<TaskTableMenu> {

    private final TaskTableScreenState state;

    public TaskTableScreen(TaskTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.state = new TaskTableScreenState(menu.containerId);

        this.setBackground(new BlurredBackground());
    }

    @Override
    protected void init() {
        TaskTableComponent component = new TaskTableComponent(state);
        component.center();
        this.addComponent(component);

        this.imageHeight = component.getHeight();
        this.imageWidth = component.getWidth();

        super.init();

        this.leftPos = component.getX();
        this.topPos = component.getY();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 92, 19, 0xFF63EEFB, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 92, 108, 0xFF63EEFB, false);
        guiGraphics.drawString(this.font, IRobot.translatable("gui.tasktable.modes"), 111, 68, 0xFF63EEFB, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
