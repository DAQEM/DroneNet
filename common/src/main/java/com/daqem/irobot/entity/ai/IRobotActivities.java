package com.daqem.irobot.entity.ai;

import com.daqem.irobot.IRobot;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.schedule.Activity;

public interface IRobotActivities {

    Registrar<Activity> ACTIVITIES = IRobot.MANAGER.get().get(Registries.ACTIVITY);

    RegistrySupplier<Activity> MINE = register("mine");
    RegistrySupplier<Activity> RECHARGE = register("recharge");

    static void init() {
    }

    static RegistrySupplier<Activity> register(String name) {
        return ACTIVITIES.register(IRobot.getId(name), () -> new Activity(name));
    }
}