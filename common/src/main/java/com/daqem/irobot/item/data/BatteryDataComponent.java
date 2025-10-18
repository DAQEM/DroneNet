package com.daqem.irobot.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record BatteryDataComponent(double energy, double maxEnergy) {

    public static final Codec<BatteryDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("energy").forGetter(BatteryDataComponent::energy),
            Codec.DOUBLE.fieldOf("max_energy").forGetter(BatteryDataComponent::maxEnergy)
    ).apply(instance, BatteryDataComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BatteryDataComponent> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                buf.writeDouble(data.energy());
                buf.writeDouble(data.maxEnergy());
            },
            buf -> new BatteryDataComponent(buf.readDouble(), buf.readDouble())
    );

    public BatteryDataComponent withEnergy(double energy) {
        return new BatteryDataComponent(Math.max(0, Math.min(energy, this.maxEnergy)), this.maxEnergy);
    }
}