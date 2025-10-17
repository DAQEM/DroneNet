package com.daqem.irobot.block;

import com.daqem.irobot.IRobot;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public interface IRobotBlocks {

    Registrar<Block> BLOCKS = IRobot.MANAGER.get().get(Registries.BLOCK);

    RegistrySupplier<RobotStationBlock> ROBOT_STATION = register("robot_station", BlockBehaviour.Properties.of(),RobotStationBlock::new);
    RegistrySupplier<TaskTableBlock> TASK_TABLE = register("task_table", BlockBehaviour.Properties.of(),TaskTableBlock::new);

    static void init() {
    }

    static <T extends Block> RegistrySupplier<T> register(String name, BlockBehaviour.Properties properties, Function<BlockBehaviour.Properties, T> constructor) {
        ResourceLocation id = IRobot.getId(name);
        return BLOCKS.register(id, () -> constructor.apply(properties.setId(ResourceKey.create(Registries.BLOCK, id))));
    }
}
