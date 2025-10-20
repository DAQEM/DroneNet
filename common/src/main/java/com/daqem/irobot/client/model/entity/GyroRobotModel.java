package com.daqem.irobot.client.model.entity;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.entity.GyroRobotEntity;
import com.daqem.irobot.entity.MiniRobotEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class GyroRobotModel extends DefaultedEntityGeoModel<GyroRobotEntity> {

    public GyroRobotModel() {
        super(IRobot.getId("gyro_robot"), true);
    }
}
