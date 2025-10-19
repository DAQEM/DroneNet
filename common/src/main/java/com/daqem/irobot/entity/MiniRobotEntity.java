package com.daqem.irobot.entity;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.item.TaskItem;
import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.DefaultAnimations;

import java.util.EnumMap;

public class MiniRobotEntity extends IRobotEntity {

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
    public static final ImmutableList<SensorType<? extends Sensor<? super MiniRobotEntity>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.HURT_BY
    );
    private static final float RECHARGE_THRESHOLD_PERCENTAGE = 0.2F;

    public MiniRobotEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return IRobotEntity.createRobotAttributes();
    }

    @Override
    protected InteractionResult handleItemInteraction(ServerPlayer player, ItemStack itemInHand, InteractionHand hand) {
        if (itemInHand.getItem() instanceof TaskItem) {
            ItemStack currentTask = this.inventory.getTask();
            if (currentTask.isEmpty()) {
                this.inventory.setItem(RobotInventory.TASK_SLOT_INDEX, itemInHand.split(1));
                return InteractionResult.SUCCESS;
            } else {
                player.sendSystemMessage(IRobot.translatable("robot.error.task_slot_full").withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                DefaultAnimations.genericWalkIdleController(),
                new AnimationController<>("HoldItem", 5, test -> {
                    EnumMap<EquipmentSlot, ItemStack> geckolibData = test.getData(DataTickets.EQUIPMENT_BY_SLOT);
                    ItemStack mainHandItem = geckolibData.get(EquipmentSlot.MAINHAND);

                    if (mainHandItem != null && !mainHandItem.isEmpty()) {
                        return test.setAndContinue(RawAnimation.begin().thenLoop("misc.holdItem"));
                    }
                    return PlayState.STOP;
                }),
                new AnimationController<>("Mining", 0, state -> {
                    if (this.isMining()) {
                        return state.setAndContinue(RawAnimation.begin().thenLoop("attack.swing"));
                    }
                    return PlayState.STOP;
                })
        );
    }

    @Override
    public int getRechargeThreshold() {
        return (int) (getMaxEnergy() * RECHARGE_THRESHOLD_PERCENTAGE);
    }
}