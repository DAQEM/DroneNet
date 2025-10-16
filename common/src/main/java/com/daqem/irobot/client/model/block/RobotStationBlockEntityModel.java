package com.daqem.irobot.client.model.block;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.block.entity.RobotStationBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class RobotStationBlockEntityModel extends DefaultedBlockGeoModel<RobotStationBlockEntity> {

    public RobotStationBlockEntityModel() {
        super(IRobot.getId("robot_station"));
    }

    @Override
    public @Nullable RenderType getRenderType(GeoRenderState renderState, ResourceLocation texture) {
        return RenderType.entityCutout(texture);
    }
}
