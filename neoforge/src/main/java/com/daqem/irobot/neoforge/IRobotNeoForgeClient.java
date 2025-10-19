package com.daqem.irobot.neoforge;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.block.entity.IRobotBlockEntities;
import com.daqem.irobot.client.IRobotClient;
import com.daqem.irobot.client.gui.robot.RobotScreen;
import com.daqem.irobot.client.gui.tasktable.TaskTableScreen;
import com.daqem.irobot.client.renderer.OutlineRenderer;
import com.daqem.irobot.client.renderer.block.DropoffChestBlockEntityRenderer;
import com.daqem.irobot.client.renderer.block.RobotStationBlockEntityRenderer;
import com.daqem.irobot.client.renderer.block.TaskTableBlockEntityRenderer;
import com.daqem.irobot.menu.IRobotMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.culling.Frustum;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@Mod(value = IRobot.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber
public class IRobotNeoForgeClient {

    public IRobotNeoForgeClient(IEventBus modEventBus, ModContainer modContainer) {
        IRobotClient.init();
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                IRobotBlockEntities.ROBOT_STATION.get(),
                RobotStationBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                IRobotBlockEntities.TASK_TABLE.get(),
                TaskTableBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                IRobotBlockEntities.DROPOFF_CHEST.get(),
                DropoffChestBlockEntityRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(IRobotMenuTypes.ROBOT_MENU.get(), RobotScreen::new);
        event.register(IRobotMenuTypes.TASK_TABLE_MENU.get(), TaskTableScreen::new);
    }

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent.AfterParticles event) {
        OutlineRenderer.renderOutline(event.getPoseStack());
    }
}