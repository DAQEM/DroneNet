package com.daqem.irobot.menu;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.item.module.IModuleItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ModuleSlot extends AbstractEmptyIconSlot {

    public ModuleSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y, IRobot.getId("robot/module_empty"));
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof IModuleItem;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public @Nullable ResourceLocation getNoItemIcon() {
        return IRobot.getId("robot/module_empty");
    }
}