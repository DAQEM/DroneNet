package com.daqem.irobot.fabric;

import com.daqem.irobot.block.entity.IRobotBlockEntities;
import com.daqem.irobot.client.IRobotClient;
import com.daqem.irobot.client.gui.robot.RobotScreen;
import com.daqem.irobot.client.gui.tasktable.TaskTableScreen;
import com.daqem.irobot.client.renderer.OutlineRenderer;
import com.daqem.irobot.client.renderer.block.DropoffChestBlockEntityRenderer;
import com.daqem.irobot.client.renderer.block.RobotStationBlockEntityRenderer;
import com.daqem.irobot.client.renderer.block.TaskTableBlockEntityRenderer;
import com.daqem.irobot.menu.IRobotMenuTypes;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.screens.MenuScreens;

public class IRobotClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        IRobotClient.init();

        BlockEntityRendererRegistry.register(IRobotBlockEntities.ROBOT_STATION.get(), RobotStationBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(IRobotBlockEntities.TASK_TABLE.get(), TaskTableBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(IRobotBlockEntities.DROPOFF_CHEST.get(), DropoffChestBlockEntityRenderer::new);

        MenuScreens.register(IRobotMenuTypes.ROBOT_MENU.get(), RobotScreen::new);
        MenuScreens.register(IRobotMenuTypes.TASK_TABLE_MENU.get(), TaskTableScreen::new);

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> OutlineRenderer.renderOutline(context.matrixStack()));
    }
}
