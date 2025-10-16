package com.daqem.irobot.item;

import com.daqem.irobot.IRobot;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public interface IRobotItems {

    Registrar<Item> ITEMS = IRobot.MANAGER.get().get(Registries.ITEM);

    RegistrySupplier<RobotStationItem> ROBOT_STATION = register("robot_station", RobotStationItem::new);
    RegistrySupplier<MiniRobotItem> MINI_ROBOT = register("mini_robot", MiniRobotItem::new);
    RegistrySupplier<AreaMarkerItem> AREA_MARKER = register("area_marker", AreaMarkerItem::new);

    static void init() {
    }

    static <T extends Item> RegistrySupplier<T> register(String name, Function<Item.Properties, T> constructor) {
        ResourceLocation id = IRobot.getId(name);
        return ITEMS.register(id, () -> constructor.apply(new Item.Properties().arch$tab(IRobot.IROBOT_TAB).setId(ResourceKey.create(Registries.ITEM, id))));
    }
}
