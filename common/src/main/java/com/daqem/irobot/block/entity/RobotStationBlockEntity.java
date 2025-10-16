package com.daqem.irobot.block.entity;

import com.daqem.irobot.entity.IRobotEntity;
import com.daqem.irobot.item.IRobotItem;
import com.daqem.irobot.level.IRobotServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class RobotStationBlockEntity extends BlockEntity implements GeoBlockEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RobotStationBlockEntity(BlockPos pos, BlockState blockState) {
        super(IRobotBlockEntities.ROBOT_STATION.get(), pos, blockState);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public boolean hasMiniRobot() {
        return this.level instanceof IRobotServerLevel serverLevel && serverLevel.irobot$getLevelData().irobot$getRobotStationMap().containsValue(this.getBlockPos());
    }

    public void deployMiniRobot(ServerPlayer serverPlayer, IRobotItem item) {
        if (this.level instanceof ServerLevel serverLevel && this.level instanceof IRobotServerLevel irobotServerLevel) {
            IRobotEntity robot = item.createRobot(serverPlayer, serverLevel, this.worldPosition);
            serverLevel.addFreshEntity(robot);
            irobotServerLevel.irobot$getLevelData().irobot$getRobotStationMap().put(robot.getUUID(), this.getBlockPos());
        }
    }
}
