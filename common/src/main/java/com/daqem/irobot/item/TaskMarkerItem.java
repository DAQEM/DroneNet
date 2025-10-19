package com.daqem.irobot.item;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.item.data.IRobotDataComponents;
import com.daqem.irobot.item.data.TaskMarkerDataComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class TaskMarkerItem extends Item {

    public TaskMarkerItem(Properties properties) {
        super(properties);
    }

    public void setFirstPos(ItemStack stack, GlobalPos pos) {
        DataComponentType<TaskMarkerDataComponent> component = IRobotDataComponents.TASK_MARKER_DATA.get();
        stack.update(component, TaskMarkerDataComponent.EMPTY, pos, TaskMarkerDataComponent::withFirstPos);
    }

    public void setSecondPos(ItemStack stack, GlobalPos pos) {
        DataComponentType<TaskMarkerDataComponent> component = IRobotDataComponents.TASK_MARKER_DATA.get();
        stack.update(component, TaskMarkerDataComponent.EMPTY, pos, TaskMarkerDataComponent::withSecondPos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(IRobot.translatable("item.task_marker.description").withStyle(ChatFormatting.GRAY));
        tooltipAdder.accept(CommonComponents.EMPTY);
        tooltipAdder.accept(IRobot.translatable("item.task_marker.description.left_click").withStyle(ChatFormatting.GRAY));
        tooltipAdder.accept(IRobot.translatable("item.task_marker.description.right_click").withStyle(ChatFormatting.GRAY));
        tooltipAdder.accept(IRobot.translatable("item.task_marker.description.finalize").withStyle(ChatFormatting.GRAY));
    }
}
