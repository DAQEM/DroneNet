package com.daqem.irobot.menu;

import com.daqem.irobot.client.entity.ClientSideInteractableRobot;
import com.daqem.irobot.entity.IRobotEntity;
import com.daqem.irobot.entity.InteractableRobot;
import com.daqem.irobot.entity.RobotInventory;
import com.daqem.irobot.item.BatteryItem;
import com.daqem.irobot.item.TaskItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RobotMenu extends AbstractContainerMenu {
    public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = ResourceLocation.withDefaultNamespace("container/slot/helmet");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = ResourceLocation.withDefaultNamespace("container/slot/chestplate");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = ResourceLocation.withDefaultNamespace("container/slot/leggings");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = ResourceLocation.withDefaultNamespace("container/slot/boots");
    private static final Map<EquipmentSlot, ResourceLocation> TEXTURE_EMPTY_SLOTS = Map.of(
            EquipmentSlot.FEET,
            EMPTY_ARMOR_SLOT_BOOTS,
            EquipmentSlot.LEGS,
            EMPTY_ARMOR_SLOT_LEGGINGS,
            EquipmentSlot.CHEST,
            EMPTY_ARMOR_SLOT_CHESTPLATE,
            EquipmentSlot.HEAD,
            EMPTY_ARMOR_SLOT_HELMET
    );
    private static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private final InteractableRobot robot;

    public RobotMenu(int windowId, Inventory playerInventory, InteractableRobot robot) {
        super(IRobotMenuTypes.ROBOT_MENU.get(), windowId);
        this.robot = robot;
        int baseArmorSlotIndex = RobotInventory.INVENTORY_SIZE + 2;

        for (int i = 0; i < 4; i++) {
            EquipmentSlot equipmentSlot = SLOT_IDS[i];
            ResourceLocation resourceLocation = TEXTURE_EMPTY_SLOTS.get(equipmentSlot);
            this.addSlot(new ArmorSlot(robot.getInventory(), equipmentSlot, baseArmorSlotIndex + (3 - i), 24, 19 + i * 22, resourceLocation));
        }

        this.addSlot(new BatterySlot(robot.getInventory(), RobotInventory.BATTERY_SLOT_INDEX, 119, 19));
        this.addSlot(new TaskSlot(robot.getInventory(), RobotInventory.TASK_SLOT_INDEX, 119, 41));

        for (int slotX = 0; slotX < 6; slotX++) {
            this.addSlot(new Slot(robot.getInventory(), slotX, 24 + slotX * 19, 187));
        }


        for (int slotY = 0; slotY < 3; slotY++) {
            for (int slotX = 0; slotX < 6; slotX++) {
                this.addSlot(new Slot(robot.getInventory(), slotX + slotY * 6 + 6, 24 + slotX * 19, slotY * 19 + 122));
            }
        }

        for (int slotX = 0; slotX < 9; slotX++) {
            this.addSlot(new Slot(playerInventory, slotX, slotX * 19 + 160, 187));
        }

        for (int slotX = 0; slotX < 9; slotX++) {
            for (int slotY = 0; slotY < 3; slotY++) {
                this.addSlot(new Slot(playerInventory, slotX + slotY * 9 + 9, slotX * 19 + 160, slotY * 19 + 122));
            }
        }

        this.addDataSlots(robot.getContainerData());
    }

    public RobotMenu(int i, Inventory inventory) {
        this(i, inventory, new ClientSideInteractableRobot(inventory.player));
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack sourceStack = slot.getItem();
            itemStack = sourceStack.copy();
            int containerSize = this.robot.getInventory().getContainerSize();
            if (index < containerSize) {
                if (!this.moveItemStackTo(sourceStack, containerSize, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (sourceStack.getItem() instanceof BatteryItem) {
                if (!this.moveItemStackTo(sourceStack, 4, 5, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (sourceStack.getItem() instanceof TaskItem) {
                if (!this.moveItemStackTo(sourceStack, 5, 6, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (sourceStack.get(DataComponents.EQUIPPABLE) != null) {
                EquipmentSlot equipmentSlot = sourceStack.get(DataComponents.EQUIPPABLE).slot();
                int armorSlotIndex = 3 - equipmentSlot.getIndex();
                if (!this.moveItemStackTo(sourceStack, armorSlotIndex, armorSlotIndex + 1, false)) {
                    if (!this.moveItemStackTo(sourceStack, 6, containerSize, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (index >= containerSize && index < containerSize + 27) {
                if (!this.moveItemStackTo(sourceStack, containerSize + 27, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= containerSize + 27 && index < this.slots.size()) {
                if (!this.moveItemStackTo(sourceStack, containerSize, containerSize + 27, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(sourceStack, 6, containerSize, false)) {
                return ItemStack.EMPTY;
            }

            if (sourceStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }


    @Override
    public boolean stillValid(Player player) {
        return this.robot.stillValid(player);
    }

    public int getRobotEntityId() {
        return this.robot.getEntityId();
    }

    public int getEnergy() {
        return this.robot.getContainerData().get(1);
    }

    public int getMaxEnergy() {
        return this.robot.getContainerData().get(2);
    }

    public int getActiveActivityIndex() {
        return this.robot.getContainerData().get(3);
    }

    public Activity getActiveActivity() {
        int index = getActiveActivityIndex();
        return IRobotEntity.getActivityByIndex(index);
    }

    public int getHealth() {
        return this.robot.getContainerData().get(4);
    }

    public int getMaxHealth() {
        return this.robot.getContainerData().get(5);
    }
}