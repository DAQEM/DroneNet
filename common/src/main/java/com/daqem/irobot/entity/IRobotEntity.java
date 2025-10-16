package com.daqem.irobot.entity;

import com.daqem.irobot.level.IRobotServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;

public abstract class IRobotEntity extends TamableAnimal implements GeoEntity, InteractableRobot {

    protected IRobotEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (this.level() instanceof IRobotServerLevel serverLevel) {
            serverLevel.irobot$getLevelData().irobot$getRobotStationMap().remove(this.getUUID());
        }
        super.die(damageSource);
    }
}
