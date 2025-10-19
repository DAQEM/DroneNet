package com.daqem.irobot.menu;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.item.TaskItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TaskSlot extends Slot {

    public TaskSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof TaskItem;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public @Nullable ResourceLocation getNoItemIcon() {
        return IRobot.getId("robot/task_empty");
    }
}