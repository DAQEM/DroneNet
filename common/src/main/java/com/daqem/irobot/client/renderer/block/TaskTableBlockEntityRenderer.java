package com.daqem.irobot.client.renderer.block;

import com.daqem.irobot.block.entity.TaskTableBlockEntity;
import com.daqem.irobot.client.model.block.TaskTableBlockEntityModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class TaskTableBlockEntityRenderer extends GeoBlockRenderer<TaskTableBlockEntity> {

    public TaskTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(new TaskTableBlockEntityModel());
    }
}
