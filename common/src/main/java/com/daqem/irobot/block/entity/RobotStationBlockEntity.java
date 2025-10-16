package com.daqem.irobot.block.entity;

import com.daqem.irobot.entity.IRobotEntity;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.item.IRobotItem;
import com.daqem.irobot.level.poi.IRobotPoiTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

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
        if (this.level instanceof ServerLevel serverLevel) {
            PoiManager poiManager = serverLevel.getPoiManager();
            BlockPos pos = this.getBlockPos();
            return poiManager.getFreeTickets(pos) == 0;
        }
        return false;
    }

    public void deployMiniRobot(ServerPlayer serverPlayer, IRobotItem item) {
        if (this.level instanceof ServerLevel serverLevel) {
            BlockPos stationPos = this.getBlockPos();

            Optional<BlockPos> acquiredPos = serverLevel.getPoiManager().take(
                    poiType -> poiType.is(IRobotPoiTypes.ROBOT_STATION.getKey()),
                    (poiType, pos) -> pos.equals(stationPos),
                    stationPos,
                    8
            );

            if (acquiredPos.isPresent()) {
                IRobotEntity robot = item.createRobot(serverPlayer, serverLevel, this.worldPosition);
                if (robot instanceof MiniRobotEntity miniRobot) {
                    miniRobot.getBrain().setMemory(IRobotMemoryModuleTypes.STATION_POS.get(), GlobalPos.of(this.level.dimension(), stationPos));
                }
                serverLevel.addFreshEntity(robot);
            }
        }
    }
}