package com.daqem.irobot.fabric;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.entity.GyroRobotEntity;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.IRobotEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class IRobotCommonFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        IRobot.init();

        createEntityAttributes();
    }

    private void createEntityAttributes() {
        FabricDefaultAttributeRegistry.register(IRobotEntities.MINI_ROBOT.get(), MiniRobotEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(IRobotEntities.GYRO_ROBOT.get(), GyroRobotEntity.createAttributes());
    }
}
