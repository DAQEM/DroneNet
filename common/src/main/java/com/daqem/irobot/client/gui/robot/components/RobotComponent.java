package com.daqem.irobot.client.gui.robot.components;

import com.daqem.irobot.IRobot;
import com.daqem.uilib.gui.component.sprite.SpriteComponent;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Supplier;

public class RobotComponent extends SpriteComponent {

    public RobotComponent(Supplier<Integer> entityIdSupplier) {
        super(0, 0, 352, 222, IRobot.getId("robot/background"));

        EntityComponent entityComponent = new EntityComponent(49, 17, 61, 86, entityIdSupplier);
        this.addComponent(entityComponent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth, int parentHeight) {
//        guiGraphics.fill(getTotalX() + 3, getTotalY() + 3, getTotalX() + getWidth() - 6, getTotalY() + getHeight() - 6, 0x9944E5E5);
        super.render(guiGraphics, mouseX, mouseY, partialTick, parentWidth, parentHeight);
    }
}
