package com.daqem.irobot.entity;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.menu.RobotMenu;
import com.daqem.irobot.stats.IRobotStats;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
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
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final RobotInventory inventory;
    private @Nullable Player interactingPlayer;
    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> getEntityId();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public MiniRobotEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.inventory = new RobotInventory(this, this.equipment);
        this.getNavigation().setCanFloat(true);
        this.setCanPickUpLoot(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.FOLLOW_RANGE, 16)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.MAX_HEALTH, 20);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TamableAnimal.TamableAnimalPanicGoal(1.5, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0F, 8.0F, 4.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 16.0F, 1.0F) {
            // Only look at the owner
            @Override
            public boolean canUse() {
                boolean canUse = super.canUse();
                if (canUse) {
                    if (this.lookAt != MiniRobotEntity.this.getOwner()) {
                        this.stop();
                        return false;
                    }
                }
                return canUse;
            }
        });
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
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

            if (hand == InteractionHand.MAIN_HAND) {
                player.awardStat(IRobotStats.TALKED_TO_ROBOT.get());
            }

            this.startInteracting(serverPlayer);
        }

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
                })
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
        for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemStack = this.inventory.getItem(i);
            if (!itemStack.isEmpty() && EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                this.inventory.removeItemNoUpdate(i);
            }
        }

    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.inventory.save(output.list("Inventory", ItemStackWithSlot.CODEC));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.inventory.load(input.listOrEmpty("Inventory", ItemStackWithSlot.CODEC));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    protected @NotNull EntityEquipment createEquipment() {
        return new RobotEquipment(this);
    }

    @Override
    public void aiStep() {
        this.inventory.tick();
        super.aiStep();
    }

    public float getDestroySpeed(BlockState state) {
        float f = this.inventory.getSelectedItem().getDestroySpeed(state);
        if (f > 1.0F) {
            f += (float)this.getAttributeValue(Attributes.MINING_EFFICIENCY);
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

        f *= (float)this.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
        if (this.isEyeInFluid(FluidTags.WATER)) {
            f *= (float)this.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getValue();
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
}