package com.daqem.irobot.fabric;

import com.daqem.irobot.client.IRobotClient;
import com.daqem.irobot.client.renderer.OutlineRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class IRobotClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        IRobotClient.init();

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> OutlineRenderer.renderOutline(context.matrixStack()));
    }
}
