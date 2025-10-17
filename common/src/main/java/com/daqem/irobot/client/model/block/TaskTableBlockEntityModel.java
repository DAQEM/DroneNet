package com.daqem.irobot.client.model.block;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.block.entity.TaskTableBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class TaskTableBlockEntityModel extends DefaultedBlockGeoModel<TaskTableBlockEntity> {

    public TaskTableBlockEntityModel() {
        super(IRobot.getId("task_table"));
    }

    @Override
    public @Nullable RenderType getRenderType(GeoRenderState renderState, ResourceLocation texture) {
        return RenderType.entityCutout(texture);
    }
}