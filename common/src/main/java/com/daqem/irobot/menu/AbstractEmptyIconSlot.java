package com.daqem.irobot.menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractEmptyIconSlot extends Slot {

    private final ResourceLocation emptyIcon;

    public AbstractEmptyIconSlot(Container container, int slot, int x, int y, ResourceLocation emptyIcon) {
        super(container, slot, x, y);
        this.emptyIcon = emptyIcon;
    }

    @Override
    public @Nullable ResourceLocation getNoItemIcon() {
        return this.emptyIcon;
    }
}
