package com.daqem.irobot.util;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TreeUtils {

    private static final Map<Block, Item> LOG_TO_SAPLING_MAP = new HashMap<>();

    static {
        LOG_TO_SAPLING_MAP.put(Blocks.OAK_LOG, Items.OAK_SAPLING);
        LOG_TO_SAPLING_MAP.put(Blocks.SPRUCE_LOG, Items.SPRUCE_SAPLING);
        LOG_TO_SAPLING_MAP.put(Blocks.BIRCH_LOG, Items.BIRCH_SAPLING);
        LOG_TO_SAPLING_MAP.put(Blocks.JUNGLE_LOG, Items.JUNGLE_SAPLING);
        LOG_TO_SAPLING_MAP.put(Blocks.ACACIA_LOG, Items.ACACIA_SAPLING);
        LOG_TO_SAPLING_MAP.put(Blocks.DARK_OAK_LOG, Items.DARK_OAK_SAPLING);
        LOG_TO_SAPLING_MAP.put(Blocks.MANGROVE_LOG, Items.MANGROVE_PROPAGULE);
        LOG_TO_SAPLING_MAP.put(Blocks.CHERRY_LOG, Items.CHERRY_SAPLING);
    }

    public static Optional<Item> getSaplingFromLog(BlockState logState) {
        return Optional.ofNullable(LOG_TO_SAPLING_MAP.get(logState.getBlock()));
    }

    public static boolean isTree(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(BlockTags.LOGS)) {
            return false;
        }
        BlockState below = level.getBlockState(pos.below());
        return below.is(BlockTags.DIRT) || below.is(BlockTags.LOGS);
    }

    public static BlockPos getTreeBase(ServerLevel level, BlockPos logPos) {
        BlockPos currentPos = logPos;
        while (level.getBlockState(currentPos.below()).is(BlockTags.LOGS) && !level.isOutsideBuildHeight(currentPos.below())) {
            currentPos = currentPos.below();
        }
        return currentPos;
    }

    public static Set<BlockPos> getTreeBlocks(ServerLevel level, BlockPos startPos) {
        Set<BlockPos> treeBlocks = Sets.newHashSet();
        Set<BlockPos> toCheck = Sets.newHashSet();
        toCheck.add(startPos);

        while (!toCheck.isEmpty()) {
            BlockPos currentPos = toCheck.iterator().next();
            toCheck.remove(currentPos);
            treeBlocks.add(currentPos);

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = currentPos.relative(direction);
                if (level.getBlockState(neighbor).is(BlockTags.LOGS) && !treeBlocks.contains(neighbor)) {
                    toCheck.add(neighbor);
                }
            }
        }
        return treeBlocks;
    }
}