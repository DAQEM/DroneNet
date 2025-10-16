package com.daqem.irobot.client.renderer.block;

import com.daqem.irobot.block.entity.RobotStationBlockEntity;
import com.daqem.irobot.client.model.block.RobotStationBlockEntityModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class RobotStationBlockEntityRenderer extends GeoBlockRenderer<RobotStationBlockEntity> {

    public RobotStationBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(new RobotStationBlockEntityModel());
    }
}
