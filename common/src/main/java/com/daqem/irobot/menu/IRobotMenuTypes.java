package com.daqem.irobot.menu;

import com.daqem.irobot.IRobot;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public interface IRobotMenuTypes {

    Registrar<MenuType<?>> MENUS = IRobot.MANAGER.get().get(Registries.MENU);

    static void init() {
    }    RegistrySupplier<MenuType<RobotMenu>> ROBOT_MENU = MENUS.register(IRobot.getId("mob_farm_menu"), () -> new MenuType<>(RobotMenu::new, FeatureFlags.VANILLA_SET));
    RegistrySupplier<MenuType<TaskTableMenu>> TASK_TABLE_MENU = MENUS.register(IRobot.getId("task_table_menu"), () -> new MenuType<>(TaskTableMenu::new, FeatureFlags.VANILLA_SET));


}
