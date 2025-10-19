package com.daqem.irobot.client.config;

import com.daqem.irobot.IRobot;
import com.daqem.yamlconfig.api.config.ConfigExtension;
import com.daqem.yamlconfig.api.config.ConfigType;
import com.daqem.yamlconfig.api.config.IConfigBuilder;
import com.daqem.yamlconfig.api.config.entry.IConfigEntry;
import com.daqem.yamlconfig.impl.config.ConfigBuilder;

public class IRobotClientConfig {

    public static void init() {
    }

    public static final IConfigEntry<Boolean> RENDER_TASK_AREA_OUTLINE;
    public static final IConfigEntry<Integer> OUTLINE_COLOR_RED;
    public static final IConfigEntry<Integer> OUTLINE_COLOR_GREEN;
    public static final IConfigEntry<Integer> OUTLINE_COLOR_BLUE;
    public static final IConfigEntry<Integer> OUTLINE_COLOR_ALPHA;
    public static final IConfigEntry<Float> OUTLINE_WIDTH;

    public static final IConfigEntry<Integer> ROBOT_SCREEN_ENTITY_SCALE;
    public static final IConfigEntry<Float> ROBOT_SCREEN_ENTITY_Y_OFFSET;

    static {
        IConfigBuilder config = new ConfigBuilder(IRobot.MOD_ID, "irobot-client", ConfigExtension.YAML, ConfigType.CLIENT);

        config.push("rendering");
        RENDER_TASK_AREA_OUTLINE = config.defineBoolean("render_task_area_outline", true)
                .withComments("Set to true to render a visual outline for the area selected with the Task Marker.");

        config.push("outline_color");
        OUTLINE_COLOR_RED = config.defineInteger("red", 64, 0, 255).withComments("The red component of the outline color (0-255).");
        OUTLINE_COLOR_GREEN = config.defineInteger("green", 229, 0, 255).withComments("The green component of the outline color (0-255).");
        OUTLINE_COLOR_BLUE = config.defineInteger("blue", 242, 0, 255).withComments("The blue component of the outline color (0-255).");
        OUTLINE_COLOR_ALPHA = config.defineInteger("alpha", 255, 0, 255).withComments("The alpha (transparency) of the outline color (0-255).");
        config.pop();

        OUTLINE_WIDTH = config.defineFloat("outline_width", 0.0625F, 0.01F, 0.25F).withComments("The thickness of the rendered outline.");
        config.pop();

        config.push("gui");
        ROBOT_SCREEN_ENTITY_SCALE = config.defineInteger("robot_screen_entity_scale", 40, 10, 100).withComments("The scale of the robot entity rendered in the robot's GUI screen.");
        ROBOT_SCREEN_ENTITY_Y_OFFSET = config.defineFloat("robot_screen_entity_y_offset", 0.25F, 0.0F, 2.0F).withComments("The vertical offset of the robot entity in the GUI. Adjust if the robot is not centered vertically.");
        config.pop();


        config.build();
    }
}