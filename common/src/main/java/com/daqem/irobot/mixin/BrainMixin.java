package com.daqem.irobot.mixin;

import com.daqem.irobot.entity.ai.IRobotBrain;
import net.minecraft.world.entity.ai.Brain;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Brain.class)
public class BrainMixin implements IRobotBrain {
}
