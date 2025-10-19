package com.daqem.irobot.event;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.item.TaskMarkerItem;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

public class LeftClickBlockEvent {

    public static void registerEvent() {
        InteractionEvent.LEFT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof TaskMarkerItem taskMarkerItem) {
                if (player instanceof ServerPlayer serverPlayer) {
                    if (player.isCrouching()) {
                        pos = pos.offset(face.getUnitVec3i());
                    }
                    taskMarkerItem.setFirstPos(stack, new GlobalPos(player.level().dimension(), pos));
                    serverPlayer.sendSystemMessage(IRobot.translatable("item.task_marker.first_pos_set").withStyle(ChatFormatting.AQUA), true);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }
}
