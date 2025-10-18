package com.daqem.irobot.client;

import com.daqem.irobot.block.entity.IRobotBlockEntities;
import com.daqem.irobot.client.gui.robot.RobotScreen;
import com.daqem.irobot.client.gui.tasktable.TaskTableScreen;
import com.daqem.irobot.client.renderer.block.DropoffChestBlockEntityRenderer;
import com.daqem.irobot.client.renderer.block.RobotStationBlockEntityRenderer;
import com.daqem.irobot.client.renderer.block.TaskTableBlockEntityRenderer;
import com.daqem.irobot.client.renderer.entity.MiniRobotEntityRenderer;
import com.daqem.irobot.entity.IRobotEntities;
import com.daqem.irobot.menu.IRobotMenuTypes;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.ChestRenderer;

public class IRobotClient {
    public static void init() {
        EntityRendererRegistry.register(IRobotEntities.MINI_ROBOT, MiniRobotEntityRenderer::new);

        BlockEntityRendererRegistry.register(IRobotBlockEntities.ROBOT_STATION.get(), RobotStationBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(IRobotBlockEntities.TASK_TABLE.get(), TaskTableBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(IRobotBlockEntities.DROPOFF_CHEST.get(), DropoffChestBlockEntityRenderer::new);

        MenuScreens.register(IRobotMenuTypes.ROBOT_MENU.get(), RobotScreen::new);
        MenuScreens.register(IRobotMenuTypes.TASK_TABLE_MENU.get(), TaskTableScreen::new);
    }
}
