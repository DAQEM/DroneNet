package com.daqem.irobot.mixin;

import com.daqem.irobot.level.storage.IRobotLevelData;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

@Mixin(PrimaryLevelData.class)
public abstract class PrimaryLevelDataMixin implements ServerLevelData, WorldData, IRobotLevelData {

    @Unique
    private BiMap<UUID, BlockPos> irobot$robotStationMap = HashBiMap.create();

    @Override
    public BiMap<UUID, BlockPos> irobot$getRobotStationMap() {
        return irobot$robotStationMap;
    }

    @Override
    public void irobot$setRobotStationMap(BiMap<UUID, BlockPos> robotStationMap) {
        this.irobot$robotStationMap = robotStationMap;
    }

    // Only fired when creating a new world.
    @Inject(method = "<init>(Lnet/minecraft/world/level/LevelSettings;Lnet/minecraft/world/level/levelgen/WorldOptions;Lnet/minecraft/world/level/storage/PrimaryLevelData$SpecialWorldProperty;Lcom/mojang/serialization/Lifecycle;)V", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        irobot$robotStationMap = HashBiMap.create();
    }

    @Inject(method = "parse", at = @At("RETURN"))
    private static <T> void parse(Dynamic<T> dynamic, LevelSettings levelSettings, PrimaryLevelData.SpecialWorldProperty specialWorldProperty, WorldOptions worldOptions, Lifecycle lifecycle, CallbackInfoReturnable<PrimaryLevelData> cir) {
        OptionalDynamic<T> necessities = dynamic.get("IRobot");
        OptionalDynamic<T> robotStations = necessities.get("RobotStations");

        BiMap<UUID, BlockPos> robotStationMap = HashBiMap.create();

        if (robotStations.result().isPresent()) {
            robotStationMap.putAll(robotStations.asMap(
                    dynamic1 -> UUID.fromString(dynamic1.asString().getOrThrow()),
                    dynamic1 -> {
                        int x = dynamic1.get("x").asInt(0);
                        int y = dynamic1.get("y").asInt(0);
                        int z = dynamic1.get("z").asInt(0);
                        return new BlockPos(x, y, z);
                    }
            ));
        }

        if (cir.getReturnValue() instanceof IRobotLevelData data) {
            data.irobot$setRobotStationMap(robotStationMap);
        }
    }

    @Inject(method = "setTagData", at = @At("HEAD"))
    private void setTagData(RegistryAccess registryAccess, CompoundTag compoundTag, CompoundTag compoundTag2, CallbackInfo ci) {
        CompoundTag iRobotTag = new CompoundTag();

        CompoundTag robotStationsTag = new CompoundTag();

        for (Map.Entry<UUID, BlockPos> entry : irobot$robotStationMap.entrySet()) {
            UUID uuid = entry.getKey();
            BlockPos pos = entry.getValue();
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            robotStationsTag.put(uuid.toString(), posTag);
        }

        iRobotTag.put("RobotStations", robotStationsTag);

        compoundTag.put("IRobot", iRobotTag);
    }
}
