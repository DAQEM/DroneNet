package com.daqem.irobot.client.gui.robot;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.client.gui.robot.components.RobotComponent;
import com.daqem.irobot.menu.RobotMenu;
import com.daqem.uilib.gui.AbstractContainerScreen;
import com.daqem.uilib.gui.background.BlurredBackground;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.schedule.Activity;

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

        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();
        Activity currentActivity = this.menu.getActiveActivity() == null ? Activity.IDLE : this.menu.getActiveActivity();

        Component statsComponent = IRobot.translatable("gui.robot.stats").withStyle(ChatFormatting.BOLD);
        Component energyComponent = IRobot.translatable("gui.robot.energy", energy, maxEnergy);
        Component activityComponent = IRobot.translatable("gui.robot.current_activity", IRobot.translatable("gui.robot.activity." + currentActivity.getName()));

        guiGraphics.drawString(this.font, statsComponent, 160, 20, 0xFF63EEFB, false);
        guiGraphics.drawString(this.font, energyComponent, 160, 30, 0xFF63EEFB, false);
        guiGraphics.drawString(this.font, activityComponent, 160, 40, 0xFF63EEFB, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}