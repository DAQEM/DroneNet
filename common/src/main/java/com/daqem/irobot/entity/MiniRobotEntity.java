package com.daqem.irobot.entity;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.entity.ai.IRobotActivities;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.entity.task.RobotTask;
import com.daqem.irobot.item.TaskMarkerItem;
import com.daqem.irobot.item.data.TaskMarkerDataComponent;
import com.daqem.irobot.item.data.IRobotDataComponents;
import com.google.common.collect.ImmutableList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
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

    private static final float RECHARGE_THRESHOLD_PERCENTAGE = 0.2F;

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
            IRobotMemoryModuleTypes.STATION_POS.get(),
            IRobotMemoryModuleTypes.MINING_DIRECTION.get(),
            IRobotMemoryModuleTypes.LANE_DIRECTION.get()
    );

    public static final ImmutableList<SensorType<? extends Sensor<? super MiniRobotEntity>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.HURT_BY
    );

    public MiniRobotEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return IRobotEntity.createRobotAttributes();
    }

    @Override
    protected InteractionResult handleItemInteraction(ServerPlayer player, ItemStack itemInHand, InteractionHand hand) {
        if (itemInHand.getItem() instanceof TaskMarkerItem) {
            return assignTaskFromMarker(player, itemInHand);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult assignTaskFromMarker(ServerPlayer player, ItemStack markerStack) {
        DataComponentType<TaskMarkerDataComponent> componentType = IRobotDataComponents.TASK_MARKER_DATA.get();
        if (!markerStack.has(componentType)) {
            player.sendSystemMessage(IRobot.translatable("error.robot.marker_not_set").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        TaskMarkerDataComponent data = markerStack.get(componentType);
        if (data == null || data.getFirstPos().equals(BlockPos.ZERO) || data.getSecondPos().equals(BlockPos.ZERO)) {
            player.sendSystemMessage(IRobot.translatable("error.robot.marker_not_set").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        GlobalPos firstPos = data.getFirstPos();
        GlobalPos secondPos = data.getSecondPos();

        Brain<IRobotEntity> brain = getBrain();
        brain.setMemory(IRobotMemoryModuleTypes.ASSIGNED_TASK.get(), RobotTask.MINING);
        brain.setMemory(IRobotMemoryModuleTypes.TASK_AREA_START.get(), GlobalPos.of(firstPos.dimension(), new BlockPos(Math.min(firstPos.pos().getX(), secondPos.pos().getX()), Math.min(firstPos.pos().getY(), secondPos.pos().getY()), Math.min(firstPos.pos().getZ(), secondPos.pos().getZ()))));
        brain.setMemory(IRobotMemoryModuleTypes.TASK_AREA_END.get(), GlobalPos.of(firstPos.dimension(), new BlockPos(Math.max(firstPos.pos().getX(), secondPos.pos().getX()), Math.max(firstPos.pos().getY(), secondPos.pos().getY()), Math.max(firstPos.pos().getZ(), secondPos.pos().getZ()))));

        brain.setActiveActivityIfPossible(IRobotActivities.MINE.get());

        player.sendSystemMessage(IRobot.translatable("robot.task.assigned_mining").withStyle(ChatFormatting.GREEN), true);

        return InteractionResult.SUCCESS;
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
                DefaultAnimations.genericAttackAnimation(DefaultAnimations.ATTACK_SWING)
        );
    }

    @Override
    public int getRechargeThreshold() {
        return (int) (getMaxEnergy() * RECHARGE_THRESHOLD_PERCENTAGE);
    }
}