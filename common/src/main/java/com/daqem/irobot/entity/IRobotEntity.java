package com.daqem.irobot.entity;

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
        super.die(damageSource);
    }
}