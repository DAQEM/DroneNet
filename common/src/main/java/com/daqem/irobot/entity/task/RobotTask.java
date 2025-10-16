package com.daqem.irobot.entity.task;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum RobotTask implements StringRepresentable {
    NONE("none"),
    MINING("mining"),
    FAILED_TOO_FAR("failed_too_far");

    public static final Codec<RobotTask> CODEC = StringRepresentable.fromEnum(RobotTask::values);

    private final String name;

    RobotTask(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}