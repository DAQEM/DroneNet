package com.daqem.irobot.client.model.entity;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.entity.MiniRobotEntity;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class MiniRobotModel extends DefaultedEntityGeoModel<MiniRobotEntity> {

    public MiniRobotModel() {
        super(IRobot.getId("mini_robot"), true);
    }
}
