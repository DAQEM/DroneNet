package com.daqem.irobot.level.storage;

import com.google.common.collect.BiMap;
import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.UUID;

public interface IRobotLevelData {
    BiMap<UUID, BlockPos> irobot$getRobotStationMap();
    void irobot$setRobotStationMap(BiMap<UUID, BlockPos> robotStationMap);
}
