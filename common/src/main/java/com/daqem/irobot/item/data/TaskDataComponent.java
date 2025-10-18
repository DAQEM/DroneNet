package com.daqem.irobot.item.data;

import com.daqem.irobot.entity.task.RobotTask;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record TaskDataComponent(RobotTask task) {

    public static final Codec<TaskDataComponent> CODEC = RobotTask.CODEC.xmap(TaskDataComponent::new, TaskDataComponent::task);
    public static final StreamCodec<RegistryFriendlyByteBuf, TaskDataComponent> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull TaskDataComponent decode(RegistryFriendlyByteBuf buf) {
            RobotTask task = buf.readEnum(RobotTask.class);
            return new TaskDataComponent(task);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, TaskDataComponent packet) {
            buf.writeEnum(packet.task());
        }
    };
}
