package com.daqem.irobot.item;

import com.daqem.irobot.entity.IRobotEntities;
import com.daqem.irobot.entity.IRobotEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class GyroRobotItem extends Item implements IRobotItem {

    public GyroRobotItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        return IRobotItem.super.useOn(context);
    }

    @Override
    public IRobotEntity createRobot(ServerPlayer serverPlayer, ServerLevel level, BlockPos pos) {
        return IRobotEntities.GYRO_ROBOT.get().create(level, entity -> {
            entity.setOwner(serverPlayer);
        }, pos, EntitySpawnReason.SPAWN_ITEM_USE, true, false);
    }
}
