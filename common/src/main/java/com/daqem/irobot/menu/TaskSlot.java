package com.daqem.irobot.menu;

import com.daqem.irobot.item.TaskItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TaskSlot extends Slot {

    public TaskSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof TaskItem;
    }
}