package com.daqem.irobot.item;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.block.IRobotBlocks;
import com.daqem.irobot.item.module.ModuleItem;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public interface IRobotItems {

    Registrar<Item> ITEMS = IRobot.MANAGER.get().get(Registries.ITEM);

    RegistrySupplier<RobotStationItem> ROBOT_STATION = register("robot_station", RobotStationItem::new);
    RegistrySupplier<TaskTableItem> TASK_TABLE = register("task_table", TaskTableItem::new);
    RegistrySupplier<BlockItem> DROPOFF_CHEST = register("dropoff_chest", p -> new BlockItem(IRobotBlocks.DROPOFF_CHEST.get(), p));
    RegistrySupplier<MiniRobotItem> MINI_ROBOT = register("mini_robot", MiniRobotItem::new);
    RegistrySupplier<TaskMarkerItem> TASK_MARKER = register("task_marker", TaskMarkerItem::new);
    RegistrySupplier<TaskItem> TASK = register("task", TaskItem::new);
    RegistrySupplier<BatteryItem> SMALL_BATTERY = register("small_battery", p -> new BatteryItem(p, 500));
    RegistrySupplier<BatteryItem> MEDIUM_BATTERY = register("medium_battery", p -> new BatteryItem(p, 1500));
    RegistrySupplier<BatteryItem> LARGE_BATTERY = register("large_battery", p -> new BatteryItem(p, 3000));

    RegistrySupplier<ModuleItem> SPEED_BOOST_MODULE = register("speed_boost_module", p -> new ModuleItem(p, ModuleItem.ModuleType.SPEED_BOOST));
    RegistrySupplier<ModuleItem> MINING_SPEED_MODULE = register("mining_speed_module", p -> new ModuleItem(p, ModuleItem.ModuleType.MINING_SPEED));
    RegistrySupplier<ModuleItem> ATTACK_DAMAGE_MODULE = register("attack_damage_module", p -> new ModuleItem(p, ModuleItem.ModuleType.ATTACK_DAMAGE));
    RegistrySupplier<ModuleItem> DURABILITY_MODULE = register("durability_module", p -> new ModuleItem(p, ModuleItem.ModuleType.DURABILITY));
    RegistrySupplier<ModuleItem> BATTERY_EFFICIENCY_MODULE = register("battery_efficiency_module", p -> new ModuleItem(p, ModuleItem.ModuleType.BATTERY_EFFICIENCY));
    RegistrySupplier<ModuleItem> SOLAR_PANEL_MODULE = register("solar_panel_module", p -> new ModuleItem(p, ModuleItem.ModuleType.SOLAR_PANEL));
    RegistrySupplier<ModuleItem> REFORESTATION_MODULE = register("reforestation_module", p -> new ModuleItem(p, ModuleItem.ModuleType.REFORESTATION));
    RegistrySupplier<ModuleItem> CROP_REPLANT_MODULE = register("crop_replant_module", p -> new ModuleItem(p, ModuleItem.ModuleType.CROP_REPLANT));


    static void init() {
    }

    static <T extends Item> RegistrySupplier<T> register(String name, Function<Item.Properties, T> constructor) {
        ResourceLocation id = IRobot.getId(name);
        return ITEMS.register(id, () -> constructor.apply(new Item.Properties().arch$tab(IRobot.IROBOT_TAB).setId(ResourceKey.create(Registries.ITEM, id))));
    }
}