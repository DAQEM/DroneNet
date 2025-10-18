package com.daqem.irobot.entity;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.entity.ai.IRobotActivities;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.entity.ai.RobotBrainPackages;
import com.daqem.irobot.entity.task.RobotTask;
import com.daqem.irobot.menu.RobotMenu;
import com.daqem.irobot.stats.IRobotStats;
import com.mojang.serialization.Dynamic;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class IRobotEntity extends TamableAnimal implements GeoEntity, InteractableRobot {

    protected static final EntityDataAccessor<Integer> DATA_ENERGY_ID = SynchedEntityData.defineId(IRobotEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    protected final RobotInventory inventory;
    private @Nullable Player interactingPlayer;
    private Vec3 lastPos = Vec3.ZERO;
    private double distanceSqAccumulator = 0.0;

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> getEntityId();
                case 1 -> getEnergy();
                case 2 -> getMaxEnergy();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 1) {
                setEnergy(value);
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    protected IRobotEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.inventory = new RobotInventory(this, this.equipment);
        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
        this.getNavigation().setRequiredPathLength(48.0F);
        this.setCanPickUpLoot(true);
        this.lastPos = this.position();
    }

    // Methods for subclasses to implement
    public abstract int getMaxEnergy();
    public abstract int getRechargeThreshold();
    protected abstract InteractionResult handleItemInteraction(ServerPlayer player, ItemStack itemInHand, InteractionHand hand);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ENERGY_ID, getMaxEnergy());
    }

    public static AttributeSupplier.Builder createRobotAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.BLOCK_BREAK_SPEED, 1.0)
                .add(Attributes.MINING_EFFICIENCY, 0.0)
                .add(Attributes.SUBMERGED_MINING_SPEED, 0.2);
    }

    @Override
    public @NotNull Brain<IRobotEntity> getBrain() {
        return (Brain<IRobotEntity>) super.getBrain();
    }

    @Override
    protected Brain.@NotNull Provider<MiniRobotEntity> brainProvider() {
        return RobotBrainPackages.createBrainProvider();
    }

    @Override
    protected @NotNull Brain<?> makeBrain(Dynamic<?> dynamic) {
        Brain<MiniRobotEntity> brain = this.brainProvider().makeBrain(dynamic);
        RobotBrainPackages.registerBrainGoals(brain);
        return brain;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.lastPos != Vec3.ZERO) {
            double distSq = this.position().distanceToSqr(this.lastPos);
            if (distSq > 0) {
                this.distanceSqAccumulator += distSq;
            }
            this.lastPos = this.position();
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push("robotBrain");
        this.getBrain().tick(level, this);
        profiler.pop();

        profiler.push("robotInventory");
        this.inventory.tick();
        profiler.pop();

        if (this.isAlive() && this.tickCount % 20 == 0) {
            // Consume energy for walking every second. 1 energy per 4 blocks (16 dist sq).
            int energyCost = (int) Math.floor(this.distanceSqAccumulator / 16.0);
            if (energyCost > 0) {
                setEnergy(getEnergy() - energyCost);
            }
            this.distanceSqAccumulator = 0.0; // Reset accumulator
        }

        Brain<IRobotEntity> brain = getBrain();
        if (brain.getMemory(IRobotMemoryModuleTypes.ASSIGNED_TASK.get()).orElse(null) == RobotTask.MINING) {
            if (brain.getActiveNonCoreActivity().orElse(Activity.IDLE) == Activity.IDLE) {
                brain.setActiveActivityIfPossible(IRobotActivities.MINE.get());
            }
        }

        super.customServerAiStep(level);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            EntityReference<LivingEntity> ownerReference = this.getOwnerReference();
            if (ownerReference == null) return InteractionResult.PASS;

            if (!ownerReference.matches(player)) {
                serverPlayer.sendSystemMessage(IRobot.translatable("error.robot.not_owner").withStyle(ChatFormatting.RED), true);
                return InteractionResult.CONSUME;
            }

            ItemStack itemInHand = player.getItemInHand(hand);
            InteractionResult itemResult = handleItemInteraction(serverPlayer, itemInHand, hand);
            if (itemResult.consumesAction()) {
                return itemResult;
            }

            if (hand == InteractionHand.MAIN_HAND) {
                player.awardStat(IRobotStats.TALKED_TO_ROBOT.get());
            }

            this.startInteracting(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void die(DamageSource damageSource) {
        if (this.level() instanceof ServerLevel serverLevel) {
            getBrain().getMemory(IRobotMemoryModuleTypes.STATION_POS.get()).ifPresent(globalPos -> {
                serverLevel.getPoiManager().release(globalPos.pos());
            });
        }
        super.die(damageSource);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.inventory.save(output.list("Inventory", ItemStackWithSlot.CODEC));
        output.putInt("Energy", this.getEnergy());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.inventory.load(input.listOrEmpty("Inventory", ItemStackWithSlot.CODEC));
        this.setEnergy(input.getIntOr("Energy", getMaxEnergy()));
    }

    //region Interaction and GUI
    private void startInteracting(ServerPlayer serverPlayer) {
        this.setInteractingPlayer(serverPlayer);
        this.openScreen(serverPlayer, this.getDisplayName());
    }

    private void openScreen(ServerPlayer serverPlayer, Component title) {
        serverPlayer.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, player) ->
                        new RobotMenu(containerId, playerInventory, this), title
        ));
    }

    @Override
    public ContainerData getContainerData() {
        return this.containerData;
    }

    @Override
    public @Nullable Player getInteractingPlayer() {
        return this.interactingPlayer;
    }

    @Override
    public void setInteractingPlayer(@Nullable Player player) {
        this.interactingPlayer = player;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.getInteractingPlayer() == player && this.isAlive() && player.canInteractWithEntity(this, 4.0);
    }

    @Override
    public int getEntityId() {
        return this.getId();
    }
    //endregion

    //region Inventory and Equipment
    @Override
    public RobotInventory getInventory() {
        return this.inventory;
    }

    @Override
    public @NotNull SlotAccess getSlot(int slot) {
        int i = slot - 300;
        return i >= 0 && i < this.inventory.getContainerSize() ? SlotAccess.forContainer(this.inventory, i) : super.getSlot(slot);
    }

    @Override
    protected void pickUpItem(ServerLevel level, ItemEntity entity) {
        this.inventory.pickUpItem(level, entity);
    }

    @Override
    public boolean wantsToPickUp(ServerLevel level, ItemStack stack) {
        return this.inventory.canAddItem(stack);
    }

    @Override
    protected void dropEquipment(ServerLevel level) {
        this.destroyVanishingCursedItems();
        this.inventory.dropAll();
    }

    protected void destroyVanishingCursedItems() {
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemStack = this.inventory.getItem(i);
            if (!itemStack.isEmpty() && EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                this.inventory.removeItemNoUpdate(i);
            }
        }
    }

    @Override
    public void hurtArmor(DamageSource damageSource, float damageAmount) {
        this.doHurtEquipment(damageSource, damageAmount, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
    }

    @Override
    public void hurtHelmet(DamageSource damageSource, float damageAmount) {
        this.doHurtEquipment(damageSource, damageAmount, EquipmentSlot.HEAD);
    }

    @Override
    protected @NotNull EntityEquipment createEquipment() {
        return new RobotEquipment(this);
    }
    //endregion

    //region Tool and Mining
    public float getDestroySpeed(BlockState state) {
        float f = this.inventory.getSelectedItem().getDestroySpeed(state);
        if (f > 1.0F) {
            f += (float) this.getAttributeValue(Attributes.MINING_EFFICIENCY);
        }

        if (MobEffectUtil.hasDigSpeed(this)) {
            f *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(this) + 1) * 0.2F;
        }

        if (this.hasEffect(MobEffects.MINING_FATIGUE)) {
            float g = switch (this.getEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };
            f *= g;
        }

        f *= (float) this.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
        if (this.isEyeInFluid(FluidTags.WATER)) {
            f *= (float) this.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getValue();
        }

        if (!this.onGround()) {
            f /= 5.0F;
        }

        return f;
    }

    public boolean hasCorrectToolForDrops(BlockState state) {
        return !state.requiresCorrectToolForDrops() || this.inventory.getSelectedItem().isCorrectToolForDrops(state);
    }

    public void setBestToolForBlock(BlockState blockState) {
        this.getInventory().setBestToolForBlock(blockState);
    }
    //endregion

    //region Energy
    public int getEnergy() {
        return this.entityData.get(DATA_ENERGY_ID);
    }

    public void setEnergy(int energy) {
        this.entityData.set(DATA_ENERGY_ID, Math.max(0, Math.min(energy, getMaxEnergy())));
    }

    public boolean needsRecharging() {
        return this.getEnergy() < getRechargeThreshold();
    }
    //endregion

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null; // Robots can't breed
    }
}