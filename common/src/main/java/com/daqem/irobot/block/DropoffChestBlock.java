package com.daqem.irobot.block;

import com.daqem.irobot.block.entity.DropoffChestBlockEntity;
import com.daqem.irobot.block.entity.IRobotBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class DropoffChestBlock extends ChestBlock {

    public static final MapCodec<DropoffChestBlock> CODEC = simpleCodec(DropoffChestBlock::new);

    @Override
    public @NotNull MapCodec<DropoffChestBlock> codec() {
        return CODEC;
    }

    public DropoffChestBlock(BlockBehaviour.Properties properties) {
        super(() -> IRobotBlockEntities.DROPOFF_CHEST.get(), properties);
    }

    @Override
    public @NotNull BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DropoffChestBlockEntity(pos, state);
    }
}