package com.daqem.irobot;

import com.daqem.irobot.block.IRobotBlocks;
import com.daqem.irobot.block.entity.IRobotBlockEntities;
import com.daqem.irobot.config.IRobotConfig;
import com.daqem.irobot.entity.IRobotEntities;
import com.daqem.irobot.entity.ai.IRobotActivities;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.event.LeftClickBlockEvent;
import com.daqem.irobot.event.RightClickBlockEvent;
import com.daqem.irobot.item.IRobotItems;
import com.daqem.irobot.item.data.IRobotDataComponents;
import com.daqem.irobot.level.poi.IRobotPoiTypes;
import com.daqem.irobot.menu.IRobotMenuTypes;
import com.daqem.irobot.stats.IRobotStats;
import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.function.Supplier;

public class IRobot {
    public static final String MOD_ID = "irobot";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Supplier<RegistrarManager> MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    public static final Registrar<CreativeModeTab> TABS = MANAGER.get().get(Registries.CREATIVE_MODE_TAB);
    public static final RegistrySupplier<CreativeModeTab> IROBOT_TAB = TABS.register(getId(MOD_ID + "_tab"), () ->
            CreativeTabRegistry.create(Component.translatable("itemGroup." + MOD_ID + "." + MOD_ID + "_tab"),
                    () -> new ItemStack(IRobotItems.MINI_ROBOT.get())));

    public static void init() {
        IRobotConfig.init();

        IRobotBlocks.init();
        IRobotStats.init();
        IRobotMenuTypes.init();
        IRobotBlockEntities.init();
        IRobotDataComponents.init();
        IRobotItems.init();
        IRobotEntities.init();
        IRobotActivities.init();
        IRobotMemoryModuleTypes.init();
        IRobotPoiTypes.init();

        RightClickBlockEvent.registerEvent();
        LeftClickBlockEvent.registerEvent();
    }

    public static MutableComponent translatable(String s) {
        return translatable(s, new Object[0]);
    }

    public static MutableComponent translatable(String s, Object... objects) {
        return Component.translatable(MOD_ID + "." + s, objects);
    }

    public static ResourceLocation getId(String str) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, str);
    }

    public static MutableComponent literal(String str) {
        return Component.literal(str);
    }
}