package com.daqem.irobot.entity;

import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RobotEquipment extends EntityEquipment {
    private final IRobotEntity robot;

    public RobotEquipment(IRobotEntity robot) {
        this.robot = robot;
    }

    @Override
    public @NotNull ItemStack set(EquipmentSlot slot, ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND ? this.robot.getInventory().setSelectedItem(stack) : super.set(slot, stack);
    }

    @Override
    public @NotNull ItemStack get(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.robot.getInventory().getSelectedItem() : super.get(slot);
    }

    @Override
    public boolean isEmpty() {
        return this.robot.getInventory().getSelectedItem().isEmpty() && super.isEmpty();
    }
}
