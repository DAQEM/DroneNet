package com.daqem.irobot.block.entity;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.block.IRobotBlocks;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public interface IRobotBlockEntities {

    Registrar<BlockEntityType<?>> BLOCK_ENTITY_TYPES = IRobot.MANAGER.get().get(Registries.BLOCK_ENTITY_TYPE);

    RegistrySupplier<BlockEntityType<RobotStationBlockEntity>> ROBOT_STATION = register("robot_station", RobotStationBlockEntity::new, IRobotBlocks.ROBOT_STATION);
    RegistrySupplier<BlockEntityType<TaskTableBlockEntity>> TASK_TABLE = register("task_table", TaskTableBlockEntity::new, IRobotBlocks.TASK_TABLE);
    RegistrySupplier<BlockEntityType<DropoffChestBlockEntity>> DROPOFF_CHEST = register("dropoff_chest", DropoffChestBlockEntity::new, IRobotBlocks.DROPOFF_CHEST);

    static void init() {
    }

    @SafeVarargs
    static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> supplier, RegistrySupplier<? extends Block>... validBlockSuppliers) {
        return BLOCK_ENTITY_TYPES.register(IRobot.getId(name), () -> {
            Set<Block> validBlocks = Arrays.stream(validBlockSuppliers)
                    .map(RegistrySupplier::get)
                    .collect(Collectors.toSet());
            return new BlockEntityType<>(supplier, validBlocks);
        });
    }
}