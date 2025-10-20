package com.daqem.irobot.entity;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.client.renderer.OutlineRenderer;
import com.daqem.irobot.config.IRobotConfig;
import com.daqem.irobot.entity.ai.IRobotActivities;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.entity.ai.RobotBrainPackages;
import com.daqem.irobot.item.data.BatteryDataComponent;
import com.daqem.irobot.item.data.IRobotDataComponents;
import com.daqem.irobot.item.data.TaskDataComponent;
import com.daqem.irobot.item.data.TaskMarkerDataComponent;
import com.daqem.irobot.item.module.ModuleItem;
import com.daqem.irobot.menu.RobotMenu;
import com.daqem.irobot.stats.IRobotStats;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.Dynamic;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

public abstract class IRobotEntity extends TamableAnimal implements GeoEntity, InteractableRobot {

    public static final DataTicket<Integer> OVERLAY_COLOR_TICKET = DataTicket.create("overlay_color", Integer.class);
    private static final EntityDataAccessor<Boolean> IS_MINING = SynchedEntityData.defineId(IRobotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_FARMING = SynchedEntityData.defineId(IRobotEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(IRobotEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_OVERLAY_COLOR = SynchedEntityData.defineId(IRobotEntity.class, EntityDataSerializers.INT);
    private static final EnumMap<ModuleItem.ModuleType, ResourceLocation> ATTRIBUTE_MODIFIER_LOCATIONS = new EnumMap<>(ModuleItem.ModuleType.class);
    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.DOORS_TO_CLOSE,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.INTERACTION_TARGET,
            MemoryModuleType.HURT_BY,
            IRobotMemoryModuleTypes.ASSIGNED_TASK.get(),
            IRobotMemoryModuleTypes.TASK_AREA_START.get(),
            IRobotMemoryModuleTypes.TASK_AREA_END.get(),
            IRobotMemoryModuleTypes.MINE_TARGET_POS.get(),
            IRobotMemoryModuleTypes.TREE_TARGET_POS.get(),
            IRobotMemoryModuleTypes.STATION_POS.get(),
            IRobotMemoryModuleTypes.MINING_DIRECTION.get(),
            IRobotMemoryModuleTypes.LANE_DIRECTION.get(),
            IRobotMemoryModuleTypes.DROPOFF_TARGET_POS.get(),
            IRobotMemoryModuleTypes.IS_CHARING.get(),
            IRobotMemoryModuleTypes.SAPLING_TO_PLANT.get(),
            IRobotMemoryModuleTypes.REPLANT_POS.get(),
            IRobotMemoryModuleTypes.FARM_TARGET_POS.get()
    );
    public static final ImmutableList<SensorType<? extends Sensor<? super IRobotEntity>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.HURT_BY
    );

    static {
        ATTRIBUTE_MODIFIER_LOCATIONS.put(ModuleItem.ModuleType.SPEED_BOOST, IRobot.getId("speed_boost_module"));
        ATTRIBUTE_MODIFIER_LOCATIONS.put(ModuleItem.ModuleType.MINING_SPEED, IRobot.getId("mining_speed_module"));
        ATTRIBUTE_MODIFIER_LOCATIONS.put(ModuleItem.ModuleType.ATTACK_DAMAGE, IRobot.getId("attack_damage_module"));
        ATTRIBUTE_MODIFIER_LOCATIONS.put(ModuleItem.ModuleType.DURABILITY, IRobot.getId("durability_module"));
    }

    protected final RobotInventory inventory;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> getEntityId();
                case 1 -> Mth.floor(getEnergy());
                case 2 -> Mth.floor(getMaxEnergy());
                case 3 -> getActiveActivityIndex();
                case 4 -> Mth.floor(getHealth());
                case 5 -> Mth.floor(getMaxHealth());
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // No-op: This container data is read-only from the client side
        }

        @Override
        public int getCount() {
            return 6;
        }
    };
    private int regenerationCooldown = 0;
    private @Nullable Player interactingPlayer;
    private Vec3 lastPos;
    private double distanceSqAccumulator;

    protected IRobotEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.inventory = new RobotInventory(this, this.equipment);
        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
        this.getNavigation().setRequiredPathLength(48.0F);
        this.setCanPickUpLoot(true);
        this.lastPos = this.position();
    }

    public static Activity getActivityByIndex(int index) {
        return switch (index) {
            case 1 -> Activity.REST;
            case 2 -> IRobotActivities.PROTECT.get();
            case 3 -> IRobotActivities.MINE.get();
            case 4 -> IRobotActivities.CUT_WOOD.get();
            case 5 -> IRobotActivities.FARM.get();
            case 6 -> IRobotActivities.FOLLOW.get();
            case 7 -> IRobotActivities.RECHARGE.get();
            case 8 -> IRobotActivities.DROPOFF.get();
            default -> Activity.IDLE;
        };
    }

    public static int getIndexByActivity(Activity activity) {
        if (activity == Activity.IDLE) return 0;
        if (activity == Activity.REST) return 1;
        if (activity == IRobotActivities.PROTECT.get()) return 2;
        if (activity == IRobotActivities.MINE.get()) return 3;
        if (activity == IRobotActivities.CUT_WOOD.get()) return 4;
        if (activity == IRobotActivities.FARM.get()) return 5;
        if (activity == IRobotActivities.FOLLOW.get()) return 6;
        if (activity == IRobotActivities.RECHARGE.get()) return 7;
        if (activity == IRobotActivities.DROPOFF.get()) return 8;
        return 0;
    }

    public static AttributeSupplier.Builder createRobotAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, IRobotConfig.FOLLOW_RANGE.get())
                .add(Attributes.MOVEMENT_SPEED, IRobotConfig.MOVEMENT_SPEED.get())
                .add(Attributes.MAX_HEALTH, IRobotConfig.MAX_HEALTH.get())
                .add(Attributes.BLOCK_BREAK_SPEED, IRobotConfig.BLOCK_BREAK_SPEED.get())
                .add(Attributes.MINING_EFFICIENCY, IRobotConfig.MINING_EFFICIENCY.get())
                .add(Attributes.SUBMERGED_MINING_SPEED, IRobotConfig.SUBMERGED_MINING_SPEED.get())
                .add(Attributes.ATTACK_DAMAGE, IRobotConfig.ATTACK_DAMAGE.get());
    }

    /**
     * Blends a list of integer colors by averaging their RGB components.
     *
     * @param colors A list of integer colors to blend.
     * @return The resulting blended integer color.
     */
    public int mixColors(List<Integer> colors) {
        if (colors == null || colors.isEmpty()) {
            return getDefaultColor();
        }
        if (colors.size() == 1) {
            return colors.getFirst();
        }

        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;
        int colorCount = colors.size();

        for (int color : colors) {
            totalRed += (color >> 16) & 0xFF;
            totalGreen += (color >> 8) & 0xFF;
            totalBlue += color & 0xFF;
        }

        int avgRed = totalRed / colorCount;
        int avgGreen = totalGreen / colorCount;
        int avgBlue = totalBlue / colorCount;

        return (avgRed << 16) | (avgGreen << 8) | avgBlue;
    }

    public abstract int getDefaultColor();

    public abstract int getDefaultOverlayColor();

    public int getActiveActivityIndex() {
        return this.getBrain().getActiveNonCoreActivity().map(IRobotEntity::getIndexByActivity).orElse(0);
    }

    // Methods for subclasses to implement
    public abstract int getRechargeThreshold();

    protected abstract InteractionResult handleItemInteraction(ServerPlayer player, ItemStack itemInHand, InteractionHand hand);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_MINING, false);
        builder.define(IS_FARMING, false);
        builder.define(DATA_COLOR, getDefaultColor());
        builder.define(DATA_OVERLAY_COLOR, getDefaultOverlayColor());
    }

    public boolean isMining() {
        return this.entityData.get(IS_MINING);
    }

    public void setMining(boolean mining) {
        this.entityData.set(IS_MINING, mining);
    }

    public boolean isFarming() {
        return this.entityData.get(IS_FARMING);
    }

    public void setFarming(boolean farming) {
        this.entityData.set(IS_FARMING, farming);
    }

    public int getColor() {
        return this.entityData.get(DATA_COLOR);
    }

    public void setColor(int color) {
        this.entityData.set(DATA_COLOR, color);
    }

    public int getOverlayColor() {
        return this.entityData.get(DATA_OVERLAY_COLOR);
    }

    public void setOverlayColor(int color) {
        this.entityData.set(DATA_OVERLAY_COLOR, color);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull Brain<IRobotEntity> getBrain() {
        return (Brain<IRobotEntity>) super.getBrain();
    }

    @Override
    protected Brain.@NotNull Provider<IRobotEntity> brainProvider() {
        return RobotBrainPackages.createBrainProvider();
    }

    @Override
    protected @NotNull Brain<?> makeBrain(Dynamic<?> dynamic) {
        Brain<IRobotEntity> brain = this.brainProvider().makeBrain(dynamic);
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
        }
        this.lastPos = this.position();
        recalculateAttributes();
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

        if (this.isAlive()) {
            if (this.tickCount % 20 == 0) {
                double distanceTraveled = Math.sqrt(this.distanceSqAccumulator);
                double energyCost = distanceTraveled * IRobotConfig.MOVEMENT_ENERGY_COST_PER_METER.get();

                if (energyCost > 0) {
                    setEnergy(getEnergy() - (energyCost * getEnergyConsumptionModifier()));
                }

                this.distanceSqAccumulator = 0.0;
            }

            handleHealthRegeneration();
            handleSolarCharging();
        }

        super.customServerAiStep(level);
    }

    private void handleHealthRegeneration() {
        if (this.getHealth() < this.getMaxHealth() && this.getEnergy() >= IRobotConfig.REGENERATION_ENERGY_COST.get()) {
            if (this.regenerationCooldown > 0) {
                this.regenerationCooldown--;
            } else {
                this.heal(IRobotConfig.REGENERATION_AMOUNT.get());
                this.setEnergy(this.getEnergy() - (IRobotConfig.REGENERATION_ENERGY_COST.get() * getEnergyConsumptionModifier()));
                this.regenerationCooldown = IRobotConfig.REGENERATION_COOLDOWN_TICKS.get();
            }
        }
    }

    private void handleSolarCharging() {
        if (hasModule(ModuleItem.ModuleType.SOLAR_PANEL) && this.isDay() && this.level().canSeeSky(this.blockPosition())) {
            if (this.tickCount % 20 == 0) {
                double energyToGen = IRobotConfig.SOLAR_PANEL_ENERGY_PER_SECOND.get();
                setEnergy(getEnergy() + energyToGen);
            }
        }
    }

    private boolean isDay() {
        int i = level().getBrightness(LightLayer.SKY, blockPosition()) - level().getSkyDarken();
        float f = level().getSunAngle(1.0F);
        float g = f < (float) Math.PI ? 0.0F : (float) (Math.PI * 2);
        f += (g - f) * 0.2F;
        i = Math.round(i * Mth.cos(f));

        i = Mth.clamp(i, 0, 15);
        return i >= 8;
    }


    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        boolean wasHurt = super.hurtServer(level, damageSource, amount);
        if (wasHurt && !this.level().isClientSide()) {
            Entity attacker = damageSource.getEntity();
            if (attacker instanceof LivingEntity livingAttacker) {
                // Don't attack players who damage the robot
                if (attacker instanceof Player) {
                    return true;
                }

                // Set the attacker as the target, prompting the robot to fight back
                this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, livingAttacker);
            }
        }
        return wasHurt;
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            EntityReference<LivingEntity> ownerReference = this.getOwnerReference();
            if (ownerReference == null) return InteractionResult.PASS;

            if (!ownerReference.matches(player) && !player.isCreative()) {
                serverPlayer.sendSystemMessage(IRobot.translatable("error.robot.not_owner").withStyle(ChatFormatting.RED), true);
                return InteractionResult.CONSUME;
            }

            ItemStack itemInHand = player.getItemInHand(hand);

            if (itemInHand.getItem() instanceof DyeItem dyeItem) {
                if (player.isCrouching()) {
                    int currentColor = this.getOverlayColor();
                    int dyeColor = dyeItem.getDyeColor().getTextureDiffuseColor();
                    List<Integer> colorsToBlend = new ArrayList<>();
                    if (currentColor != getDefaultOverlayColor()) {
                        colorsToBlend.add(currentColor);
                    }
                    colorsToBlend.add(dyeColor);
                    this.setOverlayColor(mixColors(colorsToBlend));
                } else {
                    int currentColor = this.getColor();
                    int dyeColor = dyeItem.getDyeColor().getTextureDiffuseColor();
                    List<Integer> colorsToBlend = new ArrayList<>();
                    if (currentColor != getDefaultColor()) {
                        colorsToBlend.add(currentColor);
                    }
                    colorsToBlend.add(dyeColor);
                    this.setColor(mixColors(colorsToBlend));
                }
                if (!player.getAbilities().instabuild) {
                    itemInHand.shrink(1);
                }
                this.playSound(SoundEvents.DYE_USE, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }

            if (itemInHand.is(Items.WATER_BUCKET)) {
                if (player.isCrouching()) {
                    if (this.getOverlayColor() != getDefaultOverlayColor()) {
                        this.setOverlayColor(getDefaultOverlayColor());
                        if (!player.getAbilities().instabuild) {
                            player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                        }
                        this.playSound(SoundEvents.BUCKET_EMPTY, 1.0F, 1.0F);
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    if (this.getColor() != getDefaultColor()) {
                        this.setColor(getDefaultColor());
                        if (!player.getAbilities().instabuild) {
                            player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                        }
                        this.playSound(SoundEvents.BUCKET_EMPTY, 1.0F, 1.0F);
                        return InteractionResult.SUCCESS;
                    }
                }
            }

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
                if (serverLevel.getPoiManager().exists(globalPos.pos(), poiTypeHolder -> true)) {
                    serverLevel.getPoiManager().release(globalPos.pos());
                }
            });
        }
        super.die(damageSource);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.inventory.save(output.list("Inventory", ItemStackWithSlot.CODEC));
        if (this.getColor() != getDefaultColor()) {
            output.putInt("Color", this.getColor());
        }
        if (this.getOverlayColor() != getDefaultOverlayColor()) {
            output.putInt("OverlayColor", this.getOverlayColor());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.inventory.load(input.listOrEmpty("Inventory", ItemStackWithSlot.CODEC));
        if (this.level() instanceof ServerLevel) {
            recalculateAttributes();
        }
        this.setColor(input.getIntOr("Color", getDefaultColor()));
        this.setOverlayColor(input.getIntOr("OverlayColor", getDefaultOverlayColor()));
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
        return this.getInteractingPlayer() == player && this.isAlive() && this.distanceToSqr(player) < 64.0;
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
            float g = switch (Objects.requireNonNull(this.getEffect(MobEffects.MINING_FATIGUE)).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };
            f *= g;
        }

        f *= (float) this.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
        if (this.isEyeInFluid(FluidTags.WATER)) {
            f *= (float) Objects.requireNonNull(this.getAttribute(Attributes.SUBMERGED_MINING_SPEED)).getValue();
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
    public double getEnergy() {
        ItemStack battery = this.inventory.getBattery();
        if (battery.isEmpty()) {
            return 0;
        }
        BatteryDataComponent data = battery.get(IRobotDataComponents.BATTERY_DATA.get());
        return data != null ? data.energy() : 0;
    }

    public void setEnergy(double energy) {
        ItemStack battery = this.inventory.getBattery();
        if (!battery.isEmpty()) {
            BatteryDataComponent data = battery.get(IRobotDataComponents.BATTERY_DATA.get());
            if (data != null) {
                battery.set(IRobotDataComponents.BATTERY_DATA.get(), data.withEnergy(energy));
            }
        }
    }

    public double getMaxEnergy() {
        ItemStack battery = this.inventory.getBattery();
        if (battery.isEmpty()) {
            return 0;
        }
        BatteryDataComponent data = battery.get(IRobotDataComponents.BATTERY_DATA.get());
        return data != null ? data.maxEnergy() : 0;
    }

    //endregion

    //region Modules
    public void recalculateAttributes() {
        // Build a map of ALL possible modifiers to remove them all.
        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> allPossibleModifiersBuilder = ImmutableMultimap.builder();
        for (ModuleItem.ModuleType type : ATTRIBUTE_MODIFIER_LOCATIONS.keySet()) {
            double amount = getModuleValue(type);
            AttributeModifier.Operation operation = getModuleOperation(type);
            ResourceLocation id = ATTRIBUTE_MODIFIER_LOCATIONS.get(type);
            AttributeModifier modifier = new AttributeModifier(id, amount, operation);

            switch (type) {
                case SPEED_BOOST -> allPossibleModifiersBuilder.put(Attributes.MOVEMENT_SPEED, modifier);
                case MINING_SPEED -> allPossibleModifiersBuilder.put(Attributes.MINING_EFFICIENCY, modifier);
                case ATTACK_DAMAGE -> allPossibleModifiersBuilder.put(Attributes.ATTACK_DAMAGE, modifier);
                case DURABILITY -> allPossibleModifiersBuilder.put(Attributes.MAX_HEALTH, modifier);
            }
        }

        // Remove all possible module modifiers.
        this.getAttributes().removeAttributeModifiers(allPossibleModifiersBuilder.build());

        // Add back the modifiers for the currently equipped modules.
        this.getAttributes().addTransientAttributeModifiers(createCurrentAttributeMap());

        // Clamp health.
        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    private Multimap<Holder<Attribute>, AttributeModifier> createCurrentAttributeMap() {
        ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
        for (ModuleItem module : this.inventory.getEquippedModules()) {
            double amount = getModuleValue(module.getType());
            AttributeModifier.Operation operation = getModuleOperation(module.getType());
            ResourceLocation id = ATTRIBUTE_MODIFIER_LOCATIONS.get(module.getType());
            if (id != null) {
                AttributeModifier modifier = new AttributeModifier(id, amount, operation);
                switch (module.getType()) {
                    case SPEED_BOOST -> builder.put(Attributes.MOVEMENT_SPEED, modifier);
                    case MINING_SPEED -> builder.put(Attributes.MINING_EFFICIENCY, modifier);
                    case ATTACK_DAMAGE -> builder.put(Attributes.ATTACK_DAMAGE, modifier);
                    case DURABILITY -> builder.put(Attributes.MAX_HEALTH, modifier);
                }
            }
        }
        return builder.build();
    }


    private double getModuleValue(ModuleItem.ModuleType type) {
        return switch (type) {
            case SPEED_BOOST -> IRobotConfig.SPEED_BOOST_MULTIPLIER.get();
            case MINING_SPEED -> IRobotConfig.MINING_SPEED_BONUS.get();
            case ATTACK_DAMAGE -> IRobotConfig.ATTACK_DAMAGE_BONUS.get();
            case DURABILITY -> IRobotConfig.DURABILITY_HEALTH_BONUS.get();
            default -> 0.0;
        };
    }

    private AttributeModifier.Operation getModuleOperation(ModuleItem.ModuleType type) {
        if (type == ModuleItem.ModuleType.SPEED_BOOST) {
            return AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
        }
        return AttributeModifier.Operation.ADD_VALUE;
    }

    public boolean hasModule(ModuleItem.ModuleType type) {
        return this.inventory.getEquippedModules().stream().anyMatch(m -> m.getType() == type);
    }

    public double getEnergyConsumptionModifier() {
        if (hasModule(ModuleItem.ModuleType.BATTERY_EFFICIENCY)) {
            return IRobotConfig.BATTERY_EFFICIENCY_MULTIPLIER.get();
        }
        return 1.0;
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

    public boolean needsToDropOff() {
        return this.inventory.isMainInventoryFull();
    }

    public boolean hasTaskItem() {
        ItemStack task = this.getInventory().getTask();
        return task != null && !task.isEmpty();
    }

    public @Nullable TaskDataComponent getTaskItemData() {
        ItemStack task = this.getInventory().getTask();
        if (task != null && !task.isEmpty() && task.has(IRobotDataComponents.TASK_DATA.get())) {
            return task.get(IRobotDataComponents.TASK_DATA.get());
        }
        return null;
    }

    public TaskMarkerDataComponent getTaskMarkerData() {
        ItemStack task = this.getInventory().getTask();
        if (task != null && !task.isEmpty() && task.has(IRobotDataComponents.TASK_MARKER_DATA.get())) {
            return task.get(IRobotDataComponents.TASK_MARKER_DATA.get());
        }
        return null;
    }

    public List<ItemEntity> getItemEntitiesAround() {
        if (this.level() instanceof ServerLevel serverLevel) {
            AABB searchArea = this.getBoundingBox().inflate(16.0, 3.0, 16.0);
            if (this.hasTaskItem()) {
                TaskMarkerDataComponent taskMarkerData = this.getTaskMarkerData();
                if (taskMarkerData != null && this.level().dimension().equals(taskMarkerData.firstPos().dimension())) {
                    searchArea = OutlineRenderer.createBoundingBox(
                            taskMarkerData.firstPos().pos(),
                            taskMarkerData.secondPos().pos()
                    ).inflate(2);
                }
            }
            return serverLevel.getEntitiesOfClass(ItemEntity.class, searchArea);
        }
        return List.of();
    }

    public boolean hasItemsAround() {
        return this.getItemEntitiesAround().stream().anyMatch(ItemEntity::isAlive);
    }

    public ItemEntity getNearestItemEntity() {
        List<ItemEntity> items = this.getItemEntitiesAround();
        ItemEntity nearestItem = null;
        double nearestDistanceSq = Double.MAX_VALUE;
        for (ItemEntity item : items) {
            if (item.isAlive()) {
                if (level().getBlockState(item.blockPosition().below()).isAir()) continue;
                double distanceSq = this.distanceToSqr(item);
                if (distanceSq < nearestDistanceSq) {
                    nearestDistanceSq = distanceSq;
                    nearestItem = item;
                }
            }
        }
        return nearestItem;
    }

    public boolean isCharging() {
        return this.getBrain().getActiveNonCoreActivity().orElse(Activity.IDLE).equals(IRobotActivities.RECHARGE.get());
    }

    public boolean isDroppingOffItems() {
        return this.getBrain().getActiveNonCoreActivity().orElse(Activity.IDLE).equals(IRobotActivities.DROPOFF.get());
    }

    public boolean isFollowing() {
        return this.getBrain().getActiveNonCoreActivity().orElse(Activity.IDLE).equals(IRobotActivities.FOLLOW.get());
    }
}