package com.daqem.irobot.menu;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.item.BatteryItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BatterySlot extends Slot {

    public BatterySlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof BatteryItem;
    }

    @Override
    public @Nullable ResourceLocation getNoItemIcon() {
        return IRobot.getId("robot/battery_empty");
    }
}