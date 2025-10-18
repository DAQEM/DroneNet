package com.daqem.irobot.mixin;

import com.daqem.irobot.entity.IRobotEntity;
import com.daqem.irobot.entity.ai.IRobotBrain;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Brain.class)
public abstract class BrainMixin implements IRobotBrain {

    @Unique
    private IRobotEntity irobot$robot;

    @Inject(method = "setActiveActivityToFirstValid", at = @At("HEAD"), cancellable = true)
    private void irobot$setActiveActivityIfPossible(List<Activity> activities, CallbackInfo ci) {
        if (this.irobot$robot != null) ci.cancel();
    }

    @Override
    public void irobot$setRobot(IRobotEntity robot) {
        this.irobot$robot = robot;
    }
}
