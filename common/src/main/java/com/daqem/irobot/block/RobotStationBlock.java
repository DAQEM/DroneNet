package com.daqem.irobot.block;

import com.daqem.irobot.block.entity.IRobotBlockEntities;
import com.daqem.irobot.block.entity.RobotStationBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RobotStationBlock extends BaseEntityBlock {
    public static final MapCodec<RobotStationBlock> CODEC = simpleCodec(RobotStationBlock::new);

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public RobotStationBlock(Properties properties) {
        super(properties.mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(2.0f, 6.0f));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return IRobotBlockEntities.ROBOT_STATION.get().create(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, IRobotBlockEntities.ROBOT_STATION.get(), RobotStationBlockEntity::serverTick);
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.or(
                Block.box(0, 0, 0, 4, 3, 4),
                Block.box(12, 0, 0, 16, 3, 4),
                Block.box(0, 0, 12, 4, 3, 16),
                Block.box(12, 0, 12, 16, 3, 16),
                Block.box(1, 0, 1, 15, 7, 15),
                Block.box(0, 7, 0, 16, 10, 16)
        );
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}