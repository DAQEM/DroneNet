package com.daqem.irobot.entity.ai;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.entity.task.RobotTask;
import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Optional;

public interface IRobotMemoryModuleTypes {

    Registrar<MemoryModuleType<?>> MEMORY_MODULE_TYPES = IRobot.MANAGER.get().get(Registries.MEMORY_MODULE_TYPE);

    RegistrySupplier<MemoryModuleType<RobotTask>> ASSIGNED_TASK = register("assigned_task", RobotTask.CODEC);
    RegistrySupplier<MemoryModuleType<GlobalPos>> TASK_AREA_START = register("task_area_start", GlobalPos.CODEC);
    RegistrySupplier<MemoryModuleType<GlobalPos>> TASK_AREA_END = register("task_area_end", GlobalPos.CODEC);
    RegistrySupplier<MemoryModuleType<GlobalPos>> MINE_TARGET_POS = register("mine_target_pos", GlobalPos.CODEC);
    RegistrySupplier<MemoryModuleType<GlobalPos>> STATION_POS = register("station_pos", GlobalPos.CODEC);
    RegistrySupplier<MemoryModuleType<Direction>> MINING_DIRECTION = register("mining_direction", Direction.CODEC);


    static void init() {
    }

    static <U> RegistrySupplier<MemoryModuleType<U>> register(String name, Codec<U> codec) {
        return MEMORY_MODULE_TYPES.register(IRobot.getId(name), () -> new MemoryModuleType<>(Optional.of(codec)));
    }

    static RegistrySupplier<MemoryModuleType<BlockPos>> register(String name) {
        return MEMORY_MODULE_TYPES.register(IRobot.getId(name), () -> new MemoryModuleType<>(Optional.of(BlockPos.CODEC)));
    }
}