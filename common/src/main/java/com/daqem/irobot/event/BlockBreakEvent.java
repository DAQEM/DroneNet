package com.daqem.irobot.event;

import com.daqem.irobot.block.entity.RobotStationBlockEntity;
import com.daqem.irobot.level.IRobotServerLevel;
import com.google.common.collect.BiMap;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import net.minecraft.core.BlockPos;

import java.util.UUID;

public class BlockBreakEvent {

    public static void registerEvent() {
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if (level.getBlockEntity(pos) instanceof RobotStationBlockEntity) {
                if (level instanceof IRobotServerLevel iRobotServerLevel) {
                    BiMap<UUID, BlockPos> uuidBlockPosMap = iRobotServerLevel.irobot$getLevelData().irobot$getRobotStationMap();
                    uuidBlockPosMap.remove(uuidBlockPosMap.inverse().get(pos));
                }
            }
            return EventResult.pass();
        });
    }
}
