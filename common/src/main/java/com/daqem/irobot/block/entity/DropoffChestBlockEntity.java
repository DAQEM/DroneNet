package com.daqem.irobot.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DropoffChestBlockEntity extends ChestBlockEntity {

    public DropoffChestBlockEntity(BlockPos pos, BlockState blockState) {
        super(IRobotBlockEntities.DROPOFF_CHEST.get(), pos, blockState);
    }
}