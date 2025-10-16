package com.daqem.irobot.menu;

import com.daqem.irobot.entity.IRobotEntities;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.equipment.Equippable;

public class ArmorSlot extends AbstractEmptyIconSlot {

    private final EquipmentSlot slot;

    public ArmorSlot(Container container, EquipmentSlot slot, int slotIndex, int x, int y, ResourceLocation emptyIcon) {
        super(container, slotIndex, x, y, emptyIcon);
        this.slot = slot;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && this.slot == equippable.slot() && equippable.canBeEquippedBy(IRobotEntities.MINI_ROBOT.get());
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack itemStack = this.getItem();
        return (itemStack.isEmpty() || player.isCreative() || !EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) && super.mayPickup(player);
    }
}