package com.daqem.irobot.entity;

import com.daqem.irobot.IRobot;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;

public interface IRobotEntities {

    Registrar<EntityType<?>> ENTITY_TYPES = IRobot.MANAGER.get().get(Registries.ENTITY_TYPE);

    RegistrySupplier<EntityType<MiniRobotEntity>> MINI_ROBOT = entityType("mini_robot", EntityType.Builder.of(MiniRobotEntity::new, MobCategory.CREATURE).sized(0.7f, 0.95f));
    RegistrySupplier<EntityType<GyroRobotEntity>> GYRO_ROBOT = entityType("gyro_robot", EntityType.Builder.of(GyroRobotEntity::new, MobCategory.CREATURE).sized(0.7f, 0.95f));

    static void init() {
    }

    static <T extends Mob> RegistrySupplier<EntityType<T>> entityType(String name, EntityType.Builder<T> builder) {
        ResourceLocation id = IRobot.getId(name);
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        return ENTITY_TYPES.register(id, () -> builder.build(key));
    }

}
