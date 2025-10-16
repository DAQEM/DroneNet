package com.daqem.irobot.client.gui.robot;

import com.daqem.irobot.client.gui.robot.components.RobotComponent;
import com.daqem.irobot.menu.RobotMenu;
import com.daqem.uilib.gui.AbstractContainerScreen;
import com.daqem.uilib.gui.background.BlurredBackground;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class RobotScreen extends AbstractContainerScreen<RobotMenu> {

    public RobotScreen(RobotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.setBackground(new BlurredBackground());
    }

    @Override
    protected void init() {
        RobotComponent component = new RobotComponent(this.menu::getRobotEntityId);
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
        guiGraphics.drawString(this.font, this.title, 24, 108, 0xFF63EEFB, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 160, 108, 0xFF63EEFB, false);
    }
}
