package com.daqem.irobot.block.entity;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.block.IRobotBlocks;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public interface IRobotBlockEntities {

    Registrar<BlockEntityType<?>> BLOCK_ENTITY_TYPES = IRobot.MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE);

    RegistrySupplier<BlockEntityType<RobotStationBlockEntity>> ROBOT_STATION = register("robot_station", RobotStationBlockEntity::new, IRobotBlocks.ROBOT_STATION.get());
    RegistrySupplier<BlockEntityType<TaskTableBlockEntity>> TASK_TABLE = register("task_table", TaskTableBlockEntity::new, IRobotBlocks.TASK_TABLE.get());
    RegistrySupplier<BlockEntityType<DropoffChestBlockEntity>> DROPOFF_CHEST = register("dropoff_chest", DropoffChestBlockEntity::new, IRobotBlocks.DROPOFF_CHEST.get());

    static void init() {
    }

    static <T extends BlockEntityType<?>> RegistrySupplier<T> register(String name, BlockEntityType.BlockEntitySupplier<?> supplier, Block... validBlocks) {
        return BLOCK_ENTITY_TYPES.register(IRobot.getId(name), () -> (T) new BlockEntityType<>(supplier, Set.of(validBlocks)));
    }
}
