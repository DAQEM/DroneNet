package com.daqem.irobot.menu;

import com.daqem.irobot.block.IRobotBlocks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TaskTableMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;
    private final Player player;

    public TaskTableMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public TaskTableMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(IRobotMenuTypes.TASK_TABLE_MENU.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;

        for (int slotX = 0; slotX < 9; slotX++) {
            this.addSlot(new Slot(playerInventory, slotX, slotX * 19 + 92, 187));
        }

        for (int slotX = 0; slotX < 9; slotX++) {
            for (int slotY = 0; slotY < 3; slotY++) {
                this.addSlot(new Slot(playerInventory, slotX + slotY * 9 + 9, slotX * 19 + 92, slotY * 19 + 122));
            }
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, IRobotBlocks.TASK_TABLE.get());
    }
}
