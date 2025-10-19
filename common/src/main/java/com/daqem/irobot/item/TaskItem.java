package com.daqem.irobot.item;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.item.data.IRobotDataComponents;
import com.daqem.irobot.item.data.TaskDataComponent;
import com.daqem.irobot.item.data.TaskMarkerDataComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class TaskItem extends Item {

    public TaskItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        TaskMarkerDataComponent markerData = stack.get(IRobotDataComponents.TASK_MARKER_DATA.get());
        TaskDataComponent taskData = stack.get(IRobotDataComponents.TASK_DATA.get());
        if (taskData != null && taskData.task().requiresArea()) {
            if (markerData == null) {
                tooltipAdder.accept(IRobot.translatable("tooltip.task_item.no_marker_data").withStyle(ChatFormatting.RED));
            } else {
                GlobalPos firstPos = markerData.firstPos();
                if (firstPos != null && firstPos.pos() != BlockPos.ZERO) {
                    tooltipAdder.accept(IRobot.translatable("tooltip.task_item.first_position", firstPos.pos().getX(), firstPos.pos().getY(), firstPos.pos().getZ()).withStyle(ChatFormatting.GRAY));
                } else {
                    tooltipAdder.accept(IRobot.translatable("tooltip.task_item.first_position_none").withStyle(ChatFormatting.RED));
                }
                GlobalPos secondPos = markerData.secondPos();
                if (secondPos != null && secondPos.pos() != BlockPos.ZERO) {
                    tooltipAdder.accept(IRobot.translatable("tooltip.task_item.second_position", secondPos.pos().getX(), secondPos.pos().getY(), secondPos.pos().getZ()).withStyle(ChatFormatting.GRAY));
                } else {
                    tooltipAdder.accept(IRobot.translatable("tooltip.task_item.second_position_none").withStyle(ChatFormatting.RED));
                }
            }
        }

        if (taskData != null) {
            tooltipAdder.accept(IRobot.translatable("tooltip.task_item.task_name", taskData.task().getName()).withStyle(ChatFormatting.GRAY));
        } else {
            tooltipAdder.accept(IRobot.translatable("tooltip.task_item.no_task_data").withStyle(ChatFormatting.RED));
        }
    }
}
