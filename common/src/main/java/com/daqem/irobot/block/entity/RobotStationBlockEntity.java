package com.daqem.irobot.block.entity;

import com.daqem.irobot.entity.IRobotEntity;
import com.daqem.irobot.item.BatteryItem;
import com.daqem.irobot.item.data.BatteryDataComponent;
import com.daqem.irobot.item.data.IRobotDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class RobotStationBlockEntity extends BlockEntity implements GeoBlockEntity {

    private static final int CHARGE_RATE_PER_TICK = 1;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RobotStationBlockEntity(BlockPos pos, BlockState blockState) {
        super(IRobotBlockEntities.ROBOT_STATION.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RobotStationBlockEntity station) {
        AABB chargingArea = new AABB(pos);
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, chargingArea);

        for (Entity entity : entities) {
            if (entity instanceof IRobotEntity robot) {
                chargeRobot(robot, station);
            } else if (entity instanceof ItemEntity itemEntity) {
                chargeBatteryItem(itemEntity);
            }
        }
    }

    private static void chargeRobot(IRobotEntity robot, RobotStationBlockEntity station) {
        ItemStack batteryStack = robot.getInventory().getBattery();
        if (!batteryStack.isEmpty() && batteryStack.getItem() instanceof BatteryItem) {
            BatteryDataComponent data = batteryStack.get(IRobotDataComponents.BATTERY_DATA.get());
            if (data != null && data.energy() < data.maxEnergy()) {
                double newEnergy = Math.min(data.energy() + CHARGE_RATE_PER_TICK, data.maxEnergy());
                batteryStack.set(IRobotDataComponents.BATTERY_DATA.get(), data.withEnergy(newEnergy));
            } else if (data == null) {
                batteryStack.set(IRobotDataComponents.BATTERY_DATA.get(), new BatteryDataComponent(0, ((BatteryItem) batteryStack.getItem()).getMaxEnergy()));
            }
        }
    }

    private static void chargeBatteryItem(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        if (itemStack.getItem() instanceof BatteryItem batteryItem) {
            BatteryDataComponent data = itemStack.get(IRobotDataComponents.BATTERY_DATA.get());
            if (data != null && data.energy() < data.maxEnergy()) {
                double newEnergy = Math.min(data.energy() + CHARGE_RATE_PER_TICK, data.maxEnergy());
                itemStack.set(IRobotDataComponents.BATTERY_DATA.get(), data.withEnergy(newEnergy));
            } else if (data == null) {
                itemStack.set(IRobotDataComponents.BATTERY_DATA.get(), new BatteryDataComponent(0, batteryItem.getMaxEnergy()));
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}