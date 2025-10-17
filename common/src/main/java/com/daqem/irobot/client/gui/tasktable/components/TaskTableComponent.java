package com.daqem.irobot.client.gui.tasktable.components;

import com.daqem.irobot.IRobot;
import com.daqem.uilib.gui.component.sprite.SpriteComponent;
import net.minecraft.resources.ResourceLocation;

public class TaskTableComponent extends SpriteComponent {

    public TaskTableComponent() {
        super(0, 0, 352, 222, IRobot.getId("tasktable/background"));
    }
}
