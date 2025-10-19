package com.daqem.irobot.menu;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.block.IRobotBlocks;
import com.daqem.irobot.entity.task.RobotTask;
import com.daqem.irobot.item.IRobotItems;
import com.daqem.irobot.item.data.IRobotDataComponents;
import com.daqem.irobot.item.data.TaskDataComponent;
import com.daqem.irobot.item.data.TaskMarkerDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TaskTableMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;
    private final Slot inputSlot;    private final Container inputContainer = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            TaskTableMenu.this.slotsChanged(this);
        }
    };
    private final ResultContainer resultContainer = new ResultContainer();
    private final Slot resultSlot;
    private final ContainerData data;
    private long lastSoundTime;
    private @Nullable RobotTask selectedTask = null;
    public TaskTableMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public TaskTableMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(IRobotMenuTypes.TASK_TABLE_MENU.get(), containerId);
        this.access = access;
        this.data = new SimpleContainerData(1);

        this.inputSlot = this.addSlot(new Slot(this.inputContainer, 0, 111, 42) {
            @Override
            public int getMaxStackSize() {
                return 64;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(IRobotItems.TASK_MARKER.get()) || stack.is(IRobotItems.TASK.get());
            }

            @Override
            public ResourceLocation getNoItemIcon() {
                return IRobot.getId("tasktable/task_marker_empty");
            }
        });

        this.resultSlot = this.addSlot(new Slot(this.resultContainer, 1, 225, 42) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                stack.onCraftedBy(player, stack.getCount());
                TaskTableMenu.this.resultContainer.awardUsedRecipes(player, this.getRelevantItems());

                TaskDataComponent taskData = stack.get(IRobotDataComponents.TASK_DATA.get());
                if (taskData != null && taskData.task().requiresArea()) {
                    TaskTableMenu.this.inputSlot.remove(1);
                }

                TaskTableMenu.this.createResult();

                access.execute((level, blockPos) -> {
                    long l = level.getGameTime();
                    if (TaskTableMenu.this.lastSoundTime != l) {
                        level.playSound(null, blockPos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                        TaskTableMenu.this.lastSoundTime = l;
                    }
                });
                super.onTake(player, stack);
            }

            private List<ItemStack> getRelevantItems() {
                return List.of(TaskTableMenu.this.inputSlot.getItem());
            }
        });

        for (int slotX = 0; slotX < 9; slotX++) {
            for (int slotY = 0; slotY < 3; slotY++) {
                this.addSlot(new Slot(playerInventory, slotX + slotY * 9 + 9, slotX * 19 + 92, slotY * 19 + 122));
            }
        }

        for (int slotX = 0; slotX < 9; slotX++) {
            this.addSlot(new Slot(playerInventory, slotX, slotX * 19 + 92, 187));
        }

        this.addDataSlots(this.data);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == this.inputContainer) {
            this.createResult();
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            Item item = itemStack2.getItem();
            itemStack = itemStack2.copy();
            if (index == 1) {
                item.onCraftedBy(itemStack2, player);
                if (!this.moveItemStackTo(itemStack2, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemStack2, itemStack);
            } else if (index == 0) {
                if (!this.moveItemStackTo(itemStack2, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemStack2.is(IRobotItems.TASK_MARKER.get()) || itemStack2.is(IRobotItems.TASK.get())) {
                if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 2 && index < 29) {
                if (!this.moveItemStackTo(itemStack2, 29, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 29 && index < 38 && !this.moveItemStackTo(itemStack2, 2, 29, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }

            slot.setChanged();
            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemStack2);
            if (index == 1) {
                player.drop(itemStack2, false);
            }

            this.broadcastChanges();
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, IRobotBlocks.TASK_TABLE.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < RobotTask.values().length) {
            this.selectedTask = RobotTask.values()[id];
            this.createResult();
            return true;
        }
        return false;
    }

    private void createResult() {
        this.resultSlot.set(ItemStack.EMPTY);
        this.data.set(0, 0); // No error

        if (this.selectedTask == null) {
            return;
        }

        ItemStack inputStack = this.inputContainer.getItem(0);

        if (this.selectedTask.requiresArea()) {
            if (inputStack.isEmpty() || !inputStack.is(IRobotItems.TASK_MARKER.get())) {
                this.data.set(0, 2); // Error: Please insert a Task Marker.
                return;
            }
            TaskMarkerDataComponent markerData = inputStack.get(IRobotDataComponents.TASK_MARKER_DATA.get());
            if (markerData == null || markerData.firstPos().pos() == BlockPos.ZERO || markerData.secondPos().pos() == BlockPos.ZERO) {
                this.data.set(0, 1); // Error: Task requires a defined area.
                return;
            }
            ItemStack resultStack = IRobotItems.TASK.get().getDefaultInstance();
            resultStack.set(IRobotDataComponents.TASK_MARKER_DATA.get(), markerData);
            resultStack.set(IRobotDataComponents.TASK_DATA.get(), new TaskDataComponent(this.selectedTask));
            this.resultSlot.set(resultStack);
        } else {
            ItemStack resultStack = IRobotItems.TASK.get().getDefaultInstance();
            resultStack.set(IRobotDataComponents.TASK_DATA.get(), new TaskDataComponent(this.selectedTask));
            this.resultSlot.set(resultStack);
        }
    }

    public int getErrorState() {
        return this.data.get(0);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.resultContainer.removeItemNoUpdate(1);
        this.access.execute((level, blockPos) -> this.clearContainer(player, this.inputContainer));
    }


}