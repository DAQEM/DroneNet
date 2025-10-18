package com.daqem.irobot.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record BatteryDataComponent(int energy, int maxEnergy) {

    public static final Codec<BatteryDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("energy").forGetter(BatteryDataComponent::energy),
            Codec.INT.fieldOf("max_energy").forGetter(BatteryDataComponent::maxEnergy)
    ).apply(instance, BatteryDataComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BatteryDataComponent> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeInt(data.energy());
                buf.writeInt(data.maxEnergy());
            },
            buf -> new BatteryDataComponent(buf.readInt(), buf.readInt())
    );

    public BatteryDataComponent withEnergy(int energy) {
        return new BatteryDataComponent(Math.max(0, Math.min(energy, this.maxEnergy)), this.maxEnergy);
    }
}