package com.daqem.irobot.client.gui.robot.components;

import com.daqem.irobot.IRobot;
import com.daqem.uilib.gui.component.sprite.SpriteComponent;

import java.util.function.Supplier;

public class RobotComponent extends SpriteComponent {

    public RobotComponent(Supplier<Integer> entityIdSupplier) {
        super(0, 0, 352, 222, IRobot.getId("robot/background"));

        EntityComponent entityComponent = new EntityComponent(49, 17, 61, 86, entityIdSupplier);
        this.addComponent(entityComponent);
    }
}
