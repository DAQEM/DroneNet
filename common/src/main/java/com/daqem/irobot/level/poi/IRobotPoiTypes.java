package com.daqem.irobot.level.poi;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.block.IRobotBlocks;
import com.daqem.irobot.mixin.PoiTypesAccessor;
import com.google.common.collect.ImmutableSet;
import dev.architectury.injectables.targets.ArchitecturyTarget;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public interface IRobotPoiTypes {

    Registrar<PoiType> POI_TYPES = IRobot.MANAGER.get().get(Registries.POINT_OF_INTEREST_TYPE);

    RegistrySupplier<PoiType> ROBOT_STATION = register("robot_station", getAllStates(IRobotBlocks.ROBOT_STATION.get()), 1, 1);
    RegistrySupplier<PoiType> DROPOFF_CHEST = register("dropoff_chest", getAllStates(IRobotBlocks.DROPOFF_CHEST.get()), 32, 1);

    static void init() {
    }

    private static Set<BlockState> getAllStates(Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }

    private static RegistrySupplier<PoiType> register(String name, Set<BlockState> states, int maxTickets, int validRange) {
        RegistrySupplier<PoiType> holder = POI_TYPES.register(IRobot.getId(name), () -> new PoiType(states, maxTickets, validRange));

        if (ArchitecturyTarget.getCurrentTarget().equals("fabric")) {
            PoiTypesAccessor.guildmasters$registerBlockStates(holder, states);
        }

        return holder;
    }
}