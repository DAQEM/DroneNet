package com.daqem.irobot.block;

import com.daqem.irobot.block.entity.IRobotBlockEntities;
import com.daqem.irobot.block.entity.TaskTableBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TaskTableBlock extends BaseEntityBlock {
    public static final MapCodec<TaskTableBlock> CODEC = simpleCodec(TaskTableBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty HAS_TASK_AREA = BlockStateProperties.HAS_BOOK;
    public static final BooleanProperty HAS_TASK = BlockStateProperties.HAS_RECORD;
    private static final VoxelShape SHAPE_COLLISION = Shapes.or(
            Block.column(6.0, 0.0, 3.0),
            Block.column(4.0, 3.0, 9.0),
            Block.column(6.0, 9.0, 11.0),
            Block.boxZ(6.0, 11.0, 13.0, 8.0, 11.0)
    );
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateHorizontal(
            Shapes.or(
                    Block.boxZ(14.0, 7.5, 9.5, 2, 5.5),
                    Block.boxZ(14.0, 9.5, 11.5, 4.4, 7.9),
                    Block.boxZ(14.0, 11.5, 13.5, 6.8, 10.3),
                    Block.boxZ(14.0, 13.5, 15.5, 9.25, 12.75),
                    SHAPE_COLLISION
            )
    );

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public TaskTableBlock(BlockBehaviour.Properties properties) {
        super(properties.mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(2.0f, 6.0f));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HAS_TASK, false).setValue(HAS_TASK_AREA, false));

    }

    @Override
    protected @NotNull VoxelShape getOcclusionShape(BlockState state) {
        return SHAPE_COLLISION;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        ItemStack itemStack = context.getItemInHand();
        Player player = context.getPlayer();
        boolean hasTaskArea = false;
        boolean hasTask = false;
        if (!level.isClientSide && player != null && player.canUseGameMasterBlocks()) {
            CustomData customData = itemStack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
            if (customData.contains("TaskArea")) {
                hasTaskArea = true;
            }
            if (customData.contains("Task")) {
                hasTask = true;
                hasTaskArea = false;
            }
        }

        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(HAS_TASK_AREA, hasTaskArea).setValue(HAS_TASK, hasTask);
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_COLLISION;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_TASK_AREA, HAS_TASK);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return IRobotBlockEntities.TASK_TABLE.get().create(pos, state);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TaskTableBlockEntity taskTable) {
            player.openMenu(taskTable);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}