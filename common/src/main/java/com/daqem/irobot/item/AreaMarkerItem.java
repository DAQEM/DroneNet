package com.daqem.irobot.item;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.item.data.AreaMarkerDataComponent;
import com.daqem.irobot.item.data.IRobotDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class AreaMarkerItem extends Item {

    public AreaMarkerItem(Properties properties) {
        super(properties);
    }

    public void setFirstPos(ItemStack stack, BlockPos pos) {
        DataComponentType<AreaMarkerDataComponent> component = IRobotDataComponents.AREA_MARKER_DATA.get();
        stack.update(component, new AreaMarkerDataComponent(), pos, AreaMarkerDataComponent::withFirstPos);
    }

    public void setSecondPos(ItemStack stack, BlockPos pos) {
        DataComponentType<AreaMarkerDataComponent> component = IRobotDataComponents.AREA_MARKER_DATA.get();
        stack.update(component, new AreaMarkerDataComponent(), pos, AreaMarkerDataComponent::withSecondPos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(IRobot.translatable("item.area_marker.description").withStyle(ChatFormatting.GRAY));
        tooltipAdder.accept(CommonComponents.EMPTY);
        tooltipAdder.accept(IRobot.translatable("item.area_marker.description.left_click").withStyle(ChatFormatting.GRAY));
        tooltipAdder.accept(IRobot.translatable("item.area_marker.description.right_click").withStyle(ChatFormatting.GRAY));
    }
}
