package com.daqem.irobot.entity.task;

import com.daqem.irobot.IRobot;
import com.daqem.irobot.entity.ai.IRobotActivities;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.schedule.Activity;
import org.jetbrains.annotations.NotNull;

public enum RobotTask implements StringRepresentable {
    PROTECTING("protecting"),
    MINING("mining"),
    WOODCUTTING("woodcutting"),
    FARMING("farming"),
    FOLLOWING("following");

    public static final Codec<RobotTask> CODEC = StringRepresentable.fromEnum(RobotTask::values);

    private final String name;

    RobotTask(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public Component getName() {
        return IRobot.translatable("task." + this.name);
    }

    public Activity getActivity() {
        return switch (this) {
            case PROTECTING -> IRobotActivities.PROTECT.get();
            case MINING -> IRobotActivities.MINE.get();
            case WOODCUTTING -> IRobotActivities.CUT_WOOD.get();
            case FARMING -> IRobotActivities.FARM.get();
            case FOLLOWING -> IRobotActivities.FOLLOW.get();
        };
    }
}