package com.daqem.irobot.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CropUtils {

    private static final Map<Block, Item> CROP_TO_SEED_MAP = new HashMap<>();

    static {
        CROP_TO_SEED_MAP.put(Blocks.WHEAT, Items.WHEAT_SEEDS);
        CROP_TO_SEED_MAP.put(Blocks.CARROTS, Items.CARROT);
        CROP_TO_SEED_MAP.put(Blocks.POTATOES, Items.POTATO);
        CROP_TO_SEED_MAP.put(Blocks.BEETROOTS, Items.BEETROOT_SEEDS);
        CROP_TO_SEED_MAP.put(Blocks.NETHER_WART, Items.NETHER_WART);
    }

    /**
     * Checks if a given block state represents a crop that the robot can farm.
     * @param blockState The BlockState to check.
     * @return true if it's a farmable crop, false otherwise.
     */
    public static boolean isFarmableCrop(BlockState blockState) {
        return blockState.getBlock() instanceof CropBlock || blockState.is(Blocks.NETHER_WART);
    }

    /**
     * Checks if a farmable crop is fully grown and ready for harvest.
     * @param blockState The BlockState of the crop.
     * @return true if the crop is mature, false otherwise.
     */
    public static boolean isMature(BlockState blockState) {
        if (blockState.getBlock() instanceof CropBlock cropBlock) {
            return cropBlock.isMaxAge(blockState);
        }
        return false;
    }

    /**
     * Retrieves the seed item required to plant a given crop.
     * @param cropState The BlockState of the crop.
     * @return An Optional containing the seed Item if found, otherwise an empty Optional.
     */
    public static Optional<Item> getSeedFromCrop(BlockState cropState) {
        return Optional.ofNullable(CROP_TO_SEED_MAP.get(cropState.getBlock()));
    }
}