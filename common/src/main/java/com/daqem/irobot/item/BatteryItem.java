package com.daqem.irobot.item;

import com.daqem.irobot.item.data.BatteryDataComponent;
import com.daqem.irobot.item.data.IRobotDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class BatteryItem extends Item {

    private final int maxEnergy;

    public BatteryItem(Properties properties, int maxEnergy) {
        super(properties.stacksTo(1));
        this.maxEnergy = maxEnergy;
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.set(IRobotDataComponents.BATTERY_DATA.get(), new BatteryDataComponent(this.maxEnergy, this.maxEnergy));
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        BatteryDataComponent batteryData = stack.get(IRobotDataComponents.BATTERY_DATA.get());
        if (batteryData != null) {
            tooltipAdder.accept(Component.translatable("tooltip.irobot.energy", batteryData.energy(), batteryData.maxEnergy()).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        BatteryDataComponent batteryData = stack.get(IRobotDataComponents.BATTERY_DATA.get());
        if (batteryData == null) {
            return 0;
        }
        return Math.round(13.0F * (float) batteryData.energy() / (float) batteryData.maxEnergy());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00FF00; // Green
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }
}