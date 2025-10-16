package com.daqem.irobot.neoforge;

import com.daqem.irobot.IRobot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = IRobot.MOD_ID, dist = Dist.CLIENT)
public class IRobotNeoForgeClient {

    public IRobotNeoForgeClient(IEventBus modEventBus, ModContainer modContainer) {
        IRobot.init();
    }
}