package com.daqem.irobot.client;

import com.daqem.irobot.client.renderer.entity.MiniRobotEntityRenderer;
import com.daqem.irobot.client.config.IRobotClientConfig;
import com.daqem.irobot.entity.IRobotEntities;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;

public class IRobotClient {
    public static void init() {
        IRobotClientConfig.init();
        EntityRendererRegistry.register(IRobotEntities.MINI_ROBOT, MiniRobotEntityRenderer::new);
    }
}