package com.daqem.irobot.item;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.block.entity.RobotStationBlockEntity;
import com.daqem.irobot.entity.IRobotEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

public interface IRobotItem {

    default InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof RobotStationBlockEntity robotStation) {
                if (robotStation.hasMiniRobot()) {
                    serverPlayer.sendSystemMessage(IRobot.translatable("error.robot.already_assigned").withStyle(ChatFormatting.RED), true);
                    return InteractionResult.PASS;
                } else {
                    robotStation.deployMiniRobot(serverPlayer, this);
                    context.getItemInHand().shrink(1);
                }
            } else {
                serverPlayer.sendSystemMessage(IRobot.translatable("error.robot.not_on_robot_station").withStyle(ChatFormatting.RED), true);
            }
        }
        return InteractionResult.PASS;
    }

    IRobotEntity createRobot(ServerPlayer serverPlayer, ServerLevel level, BlockPos pos);
}
