package com.daqem.irobot.neoforge;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.entity.GyroRobotEntity;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.IRobotEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(IRobot.MOD_ID)
public class IRobotNeoForge {

    public IRobotNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        IRobot.init();

        modEventBus.addListener(this::createEntityAttributes);
    }

    public void createEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(IRobotEntities.MINI_ROBOT.get(), MiniRobotEntity.createAttributes().build());
        event.put(IRobotEntities.GYRO_ROBOT.get(), GyroRobotEntity.createAttributes().build());
    }
}
