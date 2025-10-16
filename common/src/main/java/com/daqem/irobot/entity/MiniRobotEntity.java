package com.daqem.irobot.entity;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.entity.ai.IRobotActivities;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.entity.ai.RobotBrainPackages;
import com.daqem.irobot.entity.task.RobotTask;
import com.daqem.irobot.item.AreaMarkerItem;
import com.daqem.irobot.item.data.AreaMarkerDataComponent;
import com.daqem.irobot.item.data.IRobotDataComponents;
import com.daqem.irobot.menu.RobotMenu;
import com.daqem.irobot.stats.IRobotStats;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumMap;

public class MiniRobotEntity extends IRobotEntity {

    private static final EntityDataAccessor<Integer> DATA_ENERGY_ID = SynchedEntityData.defineId(MiniRobotEntity.class, EntityDataSerializers.INT);
    private static final int MAX_ENERGY = 10000;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final RobotInventory inventory;
    private @Nullable Player interactingPlayer;

    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.INTERACTION_TARGET,
            MemoryModuleType.HURT_BY,
            IRobotMemoryModuleTypes.ASSIGNED_TASK.get(),
            IRobotMemoryModuleTypes.TASK_AREA_START.get(),
            IRobotMemoryModuleTypes.TASK_AREA_END.get(),
            IRobotMemoryModuleTypes.MINE_TARGET_POS.get(),
            IRobotMemoryModuleTypes.STATION_POS.get()
    );

    public static final ImmutableList<SensorType<? extends Sensor<? super MiniRobotEntity>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.HURT_BY
    );

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

    public MiniRobotEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.inventory = new RobotInventory(this, this.equipment);
        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
        this.getNavigation().setRequiredPathLength(48.0F);
        this.setCanPickUpLoot(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ENERGY_ID, MAX_ENERGY);
    }

    @Override
    public @NotNull Brain<MiniRobotEntity> getBrain() {
        return (Brain<MiniRobotEntity>) super.getBrain();
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
    protected void customServerAiStep(ServerLevel level) {
        IRobot.LOGGER.info(getBrain().getActiveNonCoreActivity().orElse(Activity.CELEBRATE).toString());

        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("villagerBrain");
        this.getBrain().tick(level, this);
        profilerFiller.pop();

        this.inventory.tick();

        if (this.isAlive() && this.tickCount % 20 == 0) {
            Activity activity = this.getBrain().getActiveNonCoreActivity().orElse(null);
            if (activity == IRobotActivities.WORK.get()) {
                setEnergy(getEnergy() - 1); // Consume 1 energy per second
            }
        }

        super.customServerAiStep(level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.FOLLOW_RANGE, 16)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.BLOCK_BREAK_SPEED, 1.0)
                .add(Attributes.MINING_EFFICIENCY, 0.0)
                .add(Attributes.SUBMERGED_MINING_SPEED, 0.2);
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
            if (itemInHand.getItem() instanceof AreaMarkerItem) {
                return assignTaskFromMarker(serverPlayer, itemInHand);
            }

            if (hand == InteractionHand.MAIN_HAND) {
                player.awardStat(IRobotStats.TALKED_TO_ROBOT.get());
            }

            this.startInteracting(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult assignTaskFromMarker(ServerPlayer player, ItemStack markerStack) {
        DataComponentType<AreaMarkerDataComponent> componentType = IRobotDataComponents.AREA_MARKER_DATA.get();
        if (!markerStack.has(componentType)) {
            player.sendSystemMessage(IRobot.translatable("error.robot.marker_not_set").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        AreaMarkerDataComponent data = markerStack.get(componentType);
        BlockPos firstPos = data.getFirstPos();
        BlockPos secondPos = data.getSecondPos();

        if (firstPos.equals(BlockPos.ZERO) || secondPos.equals(BlockPos.ZERO)) {
            player.sendSystemMessage(IRobot.translatable("error.robot.marker_not_set").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        getBrain().setMemory(IRobotMemoryModuleTypes.ASSIGNED_TASK.get(), RobotTask.MINING);
        getBrain().setMemory(IRobotMemoryModuleTypes.TASK_AREA_START.get(), GlobalPos.of(player.level().dimension(), new BlockPos(Math.min(firstPos.getX(), secondPos.getX()), Math.min(firstPos.getY(), secondPos.getY()), Math.min(firstPos.getZ(), secondPos.getZ()))));
        getBrain().setMemory(IRobotMemoryModuleTypes.TASK_AREA_END.get(), GlobalPos.of(player.level().dimension(), new BlockPos(Math.max(firstPos.getX(), secondPos.getX()), Math.max(firstPos.getY(), secondPos.getY()), Math.max(firstPos.getZ(), secondPos.getZ()))));

        getBrain().setActiveActivityIfPossible(IRobotActivities.WORK.get());

        player.sendSystemMessage(IRobot.translatable("robot.task.assigned_mining").withStyle(ChatFormatting.GREEN), true);

        return InteractionResult.SUCCESS;
    }

    private void startInteracting(ServerPlayer serverPlayer) {
        this.setInteractingPlayer(serverPlayer);
        this.openScreen(serverPlayer, this.getDisplayName());
    }

    @Override
    public ContainerData getContainerData() {
        return this.containerData;
    }

    @Override
    public RobotInventory getInventory() {
        return this.inventory;
    }

    public @Nullable Player getInteractingPlayer() {
        return this.interactingPlayer;
    }

    @Override
    public void setInteractingPlayer(@Nullable Player player) {
        this.interactingPlayer = player;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.getInteractingPlayer() == player && this.isAlive() && player.canInteractWithEntity(this, 4F);
    }

    @Override
    public int getEntityId() {
        return this.getId();
    }

    private void openScreen(ServerPlayer serverPlayer, Component title) {
        serverPlayer.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, player) ->
                        new RobotMenu(containerId, playerInventory, this), title
        ));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                DefaultAnimations.genericWalkIdleController(),
                new AnimationController<>("HoldItem", test -> {
                    EnumMap<?, ?> geckolibData = test.renderState().getGeckolibData(DataTickets.EQUIPMENT_BY_SLOT);
                    if (geckolibData == null) return PlayState.STOP;
                    if (geckolibData.get(EquipmentSlot.MAINHAND) instanceof ItemStack itemStack && !itemStack.isEmpty()) {
                        return test.setAndContinue(RawAnimation.begin().thenLoop("misc.holdItem"));
                    }
                    return PlayState.STOP;
                }),
                DefaultAnimations.genericAttackAnimation(DefaultAnimations.ATTACK_SWING)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
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
    public void die(DamageSource damageSource) {
        if (this.level() instanceof ServerLevel serverLevel) {
            getBrain().getMemory(IRobotMemoryModuleTypes.STATION_POS.get()).ifPresent(globalPos -> {
                serverLevel.getPoiManager().release(globalPos.pos());
            });
        }
        super.die(damageSource);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
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
        this.setEnergy(input.getIntOr("Energy", MAX_ENERGY));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    protected @NotNull EntityEquipment createEquipment() {
        return new RobotEquipment(this);
    }


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

    @Override
    public void hurtArmor(DamageSource damageSource, float damageAmount) {
        this.doHurtEquipment(damageSource, damageAmount, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
    }

    @Override
    public void hurtHelmet(DamageSource damageSource, float damageAmount) {
        this.doHurtEquipment(damageSource, damageAmount, EquipmentSlot.HEAD);
    }

    // Energy Methods
    public int getEnergy() {
        return this.entityData.get(DATA_ENERGY_ID);
    }

    public void setEnergy(int energy) {
        this.entityData.set(DATA_ENERGY_ID, Math.max(0, Math.min(energy, MAX_ENERGY)));
    }

    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    public void setBestToolForBlock(BlockState blockState) {
        this.getInventory().setBestToolForBlock(blockState);
    }
}