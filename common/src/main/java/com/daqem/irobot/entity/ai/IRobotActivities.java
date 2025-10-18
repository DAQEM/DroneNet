package com.daqem.irobot.entity.ai;

import com.daqem.irobot.IRobot;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.schedule.Activity;

public interface IRobotActivities {

    Registrar<Activity> ACTIVITIES = IRobot.MANAGER.get().get(Registries.ACTIVITY);

    RegistrySupplier<Activity> PROTECT = register("protect");
    RegistrySupplier<Activity> MINE = register("mine");
    RegistrySupplier<Activity> CUT_WOOD = register("cut_wood");
    RegistrySupplier<Activity> FARM = register("farm");
    RegistrySupplier<Activity> FOLLOW = register("follow");
    RegistrySupplier<Activity> RECHARGE = register("recharge");
    RegistrySupplier<Activity> DROPOFF = register("dropoff");

    static void init() {
    }

    static RegistrySupplier<Activity> register(String name) {
        return ACTIVITIES.register(IRobot.getId(name), () -> new Activity(name));
    }
}