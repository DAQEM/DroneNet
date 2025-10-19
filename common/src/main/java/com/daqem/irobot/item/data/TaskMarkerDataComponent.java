package com.daqem.irobot.item.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record TaskMarkerDataComponent(GlobalPos firstPos, GlobalPos secondPos) {

    public static final TaskMarkerDataComponent EMPTY = new TaskMarkerDataComponent(
            GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO),
            GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO)
    );

    public static final Codec<TaskMarkerDataComponent> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("first_pos").forGetter(TaskMarkerDataComponent::firstPos),
            GlobalPos.CODEC.fieldOf("second_pos").forGetter(TaskMarkerDataComponent::secondPos)
    ).apply(instance, TaskMarkerDataComponent::new)));

    public static final StreamCodec<RegistryFriendlyByteBuf, TaskMarkerDataComponent> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull TaskMarkerDataComponent decode(RegistryFriendlyByteBuf buf) {
            if (buf.readBoolean()) {
                return null;
            }
            GlobalPos firstPos = buf.readGlobalPos();
            GlobalPos secondPos = buf.readGlobalPos();
            return new TaskMarkerDataComponent(firstPos, secondPos);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, @Nullable TaskMarkerDataComponent packet) {
            buf.writeBoolean(packet == null);
            if (packet == null) {
                return;
            }
            buf.writeGlobalPos(packet.firstPos);
            buf.writeGlobalPos(packet.secondPos);
        }
    };

    public TaskMarkerDataComponent withFirstPos(GlobalPos blockPos) {
        return new TaskMarkerDataComponent(blockPos, this.secondPos);
    }

    public TaskMarkerDataComponent withSecondPos(GlobalPos blockPos) {
        return new TaskMarkerDataComponent(this.firstPos, blockPos);
    }
}
