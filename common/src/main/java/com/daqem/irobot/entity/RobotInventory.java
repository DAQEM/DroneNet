package com.daqem.irobot.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RobotInventory implements Container {

    public static final int INVENTORY_SIZE = 24;
    public static final Int2ObjectMap<EquipmentSlot> EQUIPMENT_SLOT_MAPPING = new Int2ObjectArrayMap<>(
            Map.of(
                    EquipmentSlot.FEET.getIndex(INVENTORY_SIZE),
                    EquipmentSlot.FEET,
                    EquipmentSlot.LEGS.getIndex(INVENTORY_SIZE),
                    EquipmentSlot.LEGS,
                    EquipmentSlot.CHEST.getIndex(INVENTORY_SIZE),
                    EquipmentSlot.CHEST,
                    EquipmentSlot.HEAD.getIndex(INVENTORY_SIZE),
                    EquipmentSlot.HEAD
            )
    );

    private final NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private int selected;
    public final IRobotEntity robot;
    private final EntityEquipment equipment;

    public RobotInventory(IRobotEntity robot, EntityEquipment equipment) {
        this.robot = robot;
        this.equipment = equipment;
    }

    public void pickUpItem(ServerLevel level, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        if (this.robot.wantsToPickUp(level, itemStack)) {
            boolean bl = this.canAddItem(itemStack);
            if (!bl) {
                return;
            }

            this.robot.onItemPickup(itemEntity);
            int i = itemStack.getCount();
            ItemStack itemStack2 = this.addItem(itemStack);
            this.robot.take(itemEntity, i - itemStack2.getCount());
            if (itemStack2.isEmpty()) {
                itemEntity.discard();
            } else {
                itemStack.setCount(itemStack2.getCount());
            }
        }
    }

    public boolean canAddItem(ItemStack stack) {
        boolean bl = false;

        for (ItemStack itemStack : this.items) {
            if (itemStack.isEmpty() || ItemStack.isSameItemSameComponents(itemStack, stack) && itemStack.getCount() < itemStack.getMaxStackSize()) {
                bl = true;
                break;
            }
        }

        return bl;
    }

    public ItemStack addItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            ItemStack itemStack = stack.copy();
            this.moveItemToOccupiedSlotsWithSameType(itemStack);
            if (itemStack.isEmpty()) {
                return ItemStack.EMPTY;
            } else {
                this.moveItemToEmptySlots(itemStack);
                return itemStack.isEmpty() ? ItemStack.EMPTY : itemStack;
            }
        }
    }

    public List<ItemStack> removeAllItems() {
        List<ItemStack> list = this.items.stream().filter(itemStack -> !itemStack.isEmpty()).collect(Collectors.toList());
        this.clearContent();
        return list;
    }

    private void moveItemToOccupiedSlotsWithSameType(ItemStack stack) {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            ItemStack itemStack = this.getItem(i);
            if (ItemStack.isSameItemSameComponents(itemStack, stack)) {
                this.moveItemsBetweenStacks(stack, itemStack);
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
    }

    private void moveItemToEmptySlots(ItemStack stack) {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            ItemStack itemStack = this.getItem(i);
            if (itemStack.isEmpty()) {
                this.setItem(i, stack.copyAndClear());
                return;
            }
        }
    }

    private void moveItemsBetweenStacks(ItemStack stack, ItemStack other) {
        int i = this.getMaxStackSize(other);
        int j = Math.min(stack.getCount(), i - other.getCount());
        if (j > 0) {
            other.grow(j);
            stack.shrink(j);
            this.setChanged();
        }
    }

    public int getSelectedSlot() {
        return this.selected;
    }

    public void setSelectedSlot(int slot) {
        if (!isHotbarSlot(slot)) {
            throw new IllegalArgumentException("Invalid selected slot");
        } else {
            this.selected = slot;
        }
    }

    public ItemStack getSelectedItem() {
        return this.items.get(this.selected);
    }

    public ItemStack setSelectedItem(ItemStack stack) {
        return this.items.set(this.selected, stack);
    }

    public static int getSelectionSize() {
        return 9;
    }

    public NonNullList<ItemStack> getNonEquipmentItems() {
        return this.items;
    }

    private boolean hasRemainingSpaceForItem(ItemStack destination, ItemStack origin) {
        return !destination.isEmpty()
                && ItemStack.isSameItemSameComponents(destination, origin)
                && destination.isStackable()
                && destination.getCount() < this.getMaxStackSize(destination);
    }

    public int getFreeSlot() {
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    public boolean isFull() {
        return getFreeSlot() == -1;
    }

    public void addAndPickItem(ItemStack stack) {
        this.setSelectedSlot(this.getSuitableHotbarSlot());
        if (!this.items.get(this.selected).isEmpty()) {
            int i = this.getFreeSlot();
            if (i != -1) {
                this.items.set(i, this.items.get(this.selected));
            }
        }

        this.items.set(this.selected, stack);
    }

    public void pickSlot(int index) {
        this.setSelectedSlot(this.getSuitableHotbarSlot());
        ItemStack itemStack = this.items.get(this.selected);
        this.items.set(this.selected, this.items.get(index));
        this.items.set(index, itemStack);
    }

    public static boolean isHotbarSlot(int index) {
        return index >= 0 && index < 9;
    }

    public int findSlotMatchingItem(ItemStack stack) {
        for (int i = 0; i < this.items.size(); i++) {
            if (!this.items.get(i).isEmpty() && ItemStack.isSameItemSameComponents(stack, this.items.get(i))) {
                return i;
            }
        }

        return -1;
    }

    public static boolean isUsableForCrafting(ItemStack stack) {
        return !stack.isDamaged() && !stack.isEnchanted() && !stack.has(DataComponents.CUSTOM_NAME);
    }

    public int findSlotMatchingCraftingIngredient(Holder<Item> item, ItemStack stack) {
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack itemStack = this.items.get(i);
            if (!itemStack.isEmpty()
                    && itemStack.is(item)
                    && isUsableForCrafting(itemStack)
                    && (stack.isEmpty() || ItemStack.isSameItemSameComponents(stack, itemStack))) {
                return i;
            }
        }

        return -1;
    }

    public int getSuitableHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            int j = (this.selected + i) % 9;
            if (this.items.get(j).isEmpty()) {
                return j;
            }
        }

        for (int ix = 0; ix < 9; ix++) {
            int j = (this.selected + ix) % 9;
            if (!this.items.get(j).isEnchanted()) {
                return j;
            }
        }

        return this.selected;
    }

    private int addResource(ItemStack stack) {
        int i = this.getSlotWithRemainingSpace(stack);
        if (i == -1) {
            i = this.getFreeSlot();
        }

        return i == -1 ? stack.getCount() : this.addResource(i, stack);
    }

    private int addResource(int slot, ItemStack stack) {
        int i = stack.getCount();
        ItemStack itemStack = this.getItem(slot);
        if (itemStack.isEmpty()) {
            itemStack = stack.copyWithCount(0);
            this.setItem(slot, itemStack);
        }

        int j = this.getMaxStackSize(itemStack) - itemStack.getCount();
        int k = Math.min(i, j);
        if (k != 0) {
            i -= k;
            itemStack.grow(k);
            itemStack.setPopTime(5);
        }
        return i;
    }

    public int getSlotWithRemainingSpace(ItemStack stack) {
        if (this.hasRemainingSpaceForItem(this.getItem(this.selected), stack)) {
            return this.selected;
        } else if (this.hasRemainingSpaceForItem(this.getItem(40), stack)) {
            return 40;
        } else {
            for (int i = 0; i < this.items.size(); i++) {
                if (this.hasRemainingSpaceForItem(this.items.get(i), stack)) {
                    return i;
                }
            }

            return -1;
        }
    }

    public void tick() {
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack itemStack = this.getItem(i);
            if (!itemStack.isEmpty()) {
                itemStack.inventoryTick(this.robot.level(), this.robot, i == this.selected ? EquipmentSlot.MAINHAND : null);
            }
        }
    }

    public boolean add(ItemStack stack) {
        return this.add(-1, stack);
    }

    public boolean add(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        } else {
            try {
                if (stack.isDamaged()) {
                    if (slot == -1) {
                        slot = this.getFreeSlot();
                    }

                    if (slot >= 0) {
                        this.items.set(slot, stack.copyAndClear());
                        this.items.get(slot).setPopTime(5);
                        return true;
                    } else if (this.robot.hasInfiniteMaterials()) {
                        stack.setCount(0);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int i;
                    do {
                        i = stack.getCount();
                        if (slot == -1) {
                            stack.setCount(this.addResource(stack));
                        } else {
                            stack.setCount(this.addResource(slot, stack));
                        }
                    } while (!stack.isEmpty() && stack.getCount() < i);

                    if (stack.getCount() == i && this.robot.hasInfiniteMaterials()) {
                        stack.setCount(0);
                        return true;
                    } else {
                        return stack.getCount() < i;
                    }
                }
            } catch (Throwable var6) {
                CrashReport crashReport = CrashReport.forThrowable(var6, "Adding item to inventory");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Item being added");
                crashReportCategory.setDetail("Item ID", Item.getId(stack.getItem()));
                crashReportCategory.setDetail("Item data", stack.getDamageValue());
                crashReportCategory.setDetail("Item name", () -> stack.getHoverName().getString());
                throw new ReportedException(crashReport);
            }
        }
    }

    public void placeItemBackInInventory(ItemStack stack) {
        this.placeItemBackInInventory(stack, true);
    }

    public void placeItemBackInInventory(ItemStack stack, boolean sendPacket) {
        while (!stack.isEmpty()) {
            int i = this.getSlotWithRemainingSpace(stack);
            if (i == -1) {
                i = this.getFreeSlot();
            }

            if (i == -1) {
                this.robot.drop(stack, false, true);
                break;
            }

            int j = stack.getMaxStackSize() - this.getItem(i).getCount();
        }
    }

    public ClientboundSetPlayerInventoryPacket createInventoryUpdatePacket(int slot) {
        return new ClientboundSetPlayerInventoryPacket(slot, this.getItem(slot).copy());
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        if (slot < this.items.size()) {
            return ContainerHelper.removeItem(this.items, slot, amount);
        } else {
            EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_MAPPING.get(slot);
            if (equipmentSlot != null) {
                ItemStack itemStack = this.equipment.get(equipmentSlot);
                if (!itemStack.isEmpty()) {
                    return itemStack.split(amount);
                }
            }

            return ItemStack.EMPTY;
        }
    }

    public void removeItem(ItemStack stack) {
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i) == stack) {
                this.items.set(i, ItemStack.EMPTY);
                return;
            }
        }

        for (EquipmentSlot equipmentSlot : EQUIPMENT_SLOT_MAPPING.values()) {
            ItemStack itemStack = this.equipment.get(equipmentSlot);
            if (itemStack == stack) {
                this.equipment.set(equipmentSlot, ItemStack.EMPTY);
                return;
            }
        }
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        if (slot < this.items.size()) {
            ItemStack itemStack = this.items.get(slot);
            this.items.set(slot, ItemStack.EMPTY);
            return itemStack;
        } else {
            EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_MAPPING.get(slot);
            return equipmentSlot != null ? this.equipment.set(equipmentSlot, ItemStack.EMPTY) : ItemStack.EMPTY;
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < this.items.size()) {
            this.items.set(slot, stack);
        }

        EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_MAPPING.get(slot);
        if (equipmentSlot != null) {
            this.equipment.set(equipmentSlot, stack);
        }
    }

    @Override
    public void setChanged() {
    }

    public void save(ValueOutput.TypedOutputList<ItemStackWithSlot> output) {
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack itemStack = this.items.get(i);
            if (!itemStack.isEmpty()) {
                output.add(new ItemStackWithSlot(i, itemStack));
            }
        }
    }

    public void load(ValueInput.TypedInputList<ItemStackWithSlot> input) {
        this.items.clear();

        for (ItemStackWithSlot itemStackWithSlot : input) {
            if (itemStackWithSlot.isValidInContainer(this.items.size())) {
                this.setItem(itemStackWithSlot.slot(), itemStackWithSlot.stack());
            }
        }
    }

    @Override
    public int getContainerSize() {
        return this.items.size() + EQUIPMENT_SLOT_MAPPING.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.items) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }

        for (EquipmentSlot equipmentSlot : EQUIPMENT_SLOT_MAPPING.values()) {
            if (!this.equipment.get(equipmentSlot).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        if (slot < this.items.size()) {
            return this.items.get(slot);
        } else {
            EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_MAPPING.get(slot);
            return equipmentSlot != null ? this.equipment.get(equipmentSlot) : ItemStack.EMPTY;
        }
    }

    public void dropAll() {
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack itemStack = this.items.get(i);
            if (!itemStack.isEmpty()) {
                this.robot.drop(itemStack, true, false);
                this.items.set(i, ItemStack.EMPTY);
            }
        }

        this.equipment.dropAll(this.robot);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public boolean contains(ItemStack stack) {
        for (ItemStack itemStack : this) {
            if (!itemStack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, stack)) {
                return true;
            }
        }

        return false;
    }

    public boolean contains(TagKey<Item> tag) {
        for (ItemStack itemStack : this) {
            if (!itemStack.isEmpty() && itemStack.is(tag)) {
                return true;
            }
        }

        return false;
    }

    public boolean contains(Predicate<ItemStack> predicate) {
        for (ItemStack itemStack : this) {
            if (predicate.test(itemStack)) {
                return true;
            }
        }

        return false;
    }

    public void replaceWith(Inventory playerInventory) {
        for (int i = 0; i < this.getContainerSize(); i++) {
            this.setItem(i, playerInventory.getItem(i));
        }

        this.setSelectedSlot(playerInventory.getSelectedSlot());
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.equipment.clear();
    }

    public void fillStackedContents(StackedItemContents contents) {
        for (ItemStack itemStack : this.items) {
            contents.accountSimpleStack(itemStack);
        }
    }

    public ItemStack removeFromSelected(boolean removeStack) {
        ItemStack itemStack = this.getSelectedItem();
        return itemStack.isEmpty() ? ItemStack.EMPTY : this.removeItem(this.selected, removeStack ? itemStack.getCount() : 1);
    }

    public void setBestToolForBlock(BlockState blockState) {
        int bestSlot = -1;
        float bestSpeed = 1.0f;

        for (int i = 0; i < this.items.size(); i++) {
            ItemStack itemStack = this.getItem(i);
            float speed = itemStack.getDestroySpeed(blockState);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        if (bestSlot != -1) {
            this.setSelectedSlot(bestSlot);
        }
    }
}