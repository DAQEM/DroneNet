package com.daqem.irobot.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AreaMarkerDataComponent {

    private BlockPos firstPos;
    private BlockPos secondPos;

    public AreaMarkerDataComponent() {
        this(BlockPos.ZERO, BlockPos.ZERO);
    }

    public AreaMarkerDataComponent(BlockPos firstPos, BlockPos secondPos) {
        this.firstPos = firstPos;
        this.secondPos = secondPos;
    }

    public static final Codec<AreaMarkerDataComponent> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("first_pos").forGetter(AreaMarkerDataComponent::getFirstPos),
            BlockPos.CODEC.fieldOf("second_pos").forGetter(AreaMarkerDataComponent::getSecondPos)
    ).apply(instance, AreaMarkerDataComponent::new)));

    public static final StreamCodec<RegistryFriendlyByteBuf, AreaMarkerDataComponent> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, AreaMarkerDataComponent>() {
        @Override
        public @NotNull AreaMarkerDataComponent decode(RegistryFriendlyByteBuf buf) {
            if (buf.readBoolean()) {
                return null;
            }
            BlockPos firstPos = buf.readBlockPos();
            BlockPos secondPos = buf.readBlockPos();
            return new AreaMarkerDataComponent(firstPos, secondPos);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, @Nullable AreaMarkerDataComponent packet) {
            buf.writeBoolean(packet == null);
            if (packet == null) {
                return;
            }
            buf.writeBlockPos(packet.firstPos);
            buf.writeBlockPos(packet.secondPos);
        }
    };

    public BlockPos getFirstPos() {
        return firstPos;
    }

    public BlockPos getSecondPos() {
        return secondPos;
    }

    public void setFirstPos(BlockPos firstPos) {
        this.firstPos = firstPos;
    }

    public void setSecondPos(BlockPos secondPos) {
        this.secondPos = secondPos;
    }

    public AreaMarkerDataComponent withFirstPos(BlockPos blockPos) {
        return new AreaMarkerDataComponent(blockPos, this.secondPos);
    }

    public AreaMarkerDataComponent withSecondPos(BlockPos blockPos) {
        return new AreaMarkerDataComponent(this.firstPos, blockPos);
    }
}
