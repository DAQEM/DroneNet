package com.daqem.irobot.item;

import com.daqem.irobot.entity.IRobotEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

public interface IRobotItem {

    default InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            IRobotEntity robot = createRobot(serverPlayer, serverPlayer.level(), context.getClickedPos());
            serverPlayer.level().addFreshEntity(robot);
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.PASS;
    }

    IRobotEntity createRobot(ServerPlayer serverPlayer, ServerLevel level, BlockPos pos);
}
