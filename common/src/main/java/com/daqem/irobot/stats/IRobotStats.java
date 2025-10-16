package com.daqem.irobot.stats;

import com.daqem.irobot.IRobot;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

public interface IRobotStats {

    Registrar<ResourceLocation> STATS = IRobot.MANAGER.get().get(Registries.CUSTOM_STAT);

    RegistrySupplier<ResourceLocation> TALKED_TO_ROBOT = register("talked_to_robot");

    static void init() {
    }

    static RegistrySupplier<ResourceLocation> register(String name) {
        ResourceLocation location = IRobot.getId(name);
        RegistrySupplier<ResourceLocation> stat = STATS.register(location, () -> location);
        Stats.CUSTOM.get(location, StatFormatter.DEFAULT);
        return stat;
    }
}
