package com.daqem.irobot.item.data;

import com.daqem.irobot.IRobot;
import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface IRobotDataComponents {

    Registrar<DataComponentType<?>> DATA_COMPONENTS = IRobot.MANAGER.get().get(Registries.DATA_COMPONENT_TYPE);

    RegistrySupplier<DataComponentType<AreaMarkerDataComponent>> AREA_MARKER_DATA = register("area_marker_data", AreaMarkerDataComponent.CODEC, AreaMarkerDataComponent.STREAM_CODEC);

    static void init() {
    }

    static <T> RegistrySupplier<DataComponentType<T>> register(String name, Codec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return DATA_COMPONENTS.register(IRobot.getId(name), () -> ((DataComponentType.Builder) DataComponentType.builder()).persistent(codec).networkSynchronized(streamCodec).build());
    }

}
