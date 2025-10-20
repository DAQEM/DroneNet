package com.daqem.irobot.entity.ai.behavior.mining;

import com.daqem.irobot.client.renderer.OutlineRenderer;
import com.daqem.irobot.entity.IRobotEntity;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.entity.task.RobotTask;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class FindNextBlockToMine extends Behavior<IRobotEntity> {

    private static final int SCAN_STEP_SIZE = 1;
    private static final int LANE_WIDTH = 3;

    public FindNextBlockToMine() {
        super(ImmutableMap.of(
                IRobotMemoryModuleTypes.ASSIGNED_TASK.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.TASK_AREA_START.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.TASK_AREA_END.get(), MemoryStatus.VALUE_PRESENT,
                IRobotMemoryModuleTypes.MINE_TARGET_POS.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, IRobotEntity robot) {
        return robot.getBrain().getMemory(IRobotMemoryModuleTypes.ASSIGNED_TASK.get()).orElse(null) == RobotTask.MINING;
    }

    @Override
    protected void start(ServerLevel level, IRobotEntity robot, long gameTime) {
        Optional<GlobalPos> startPosOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.TASK_AREA_START.get());
        Optional<GlobalPos> endPosOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.TASK_AREA_END.get());

        if (startPosOpt.isEmpty() || endPosOpt.isEmpty()) {
            return;
        }

        AABB miningArea = OutlineRenderer.createBoundingBox(startPosOpt.get().pos(), endPosOpt.get().pos());

        if (!miningArea.inflate(1).contains(robot.position())) {
            Vec3 closestPoint = getClosestPointInAABB(robot.position(), miningArea);
            if (!level.getBlockState(BlockPos.containing(closestPoint).below()).isAir()) {
                robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(closestPoint, 0.5f, 0));
                return;
            }
        }

        Direction miningDir = robot.getBrain().getMemory(IRobotMemoryModuleTypes.MINING_DIRECTION.get()).orElse(Direction.EAST);
        if (!robot.getBrain().hasMemoryValue(IRobotMemoryModuleTypes.MINING_DIRECTION.get())) {
            robot.getBrain().setMemory(IRobotMemoryModuleTypes.MINING_DIRECTION.get(), miningDir);
        }
        if (!robot.getBrain().hasMemoryValue(IRobotMemoryModuleTypes.LANE_DIRECTION.get())) {
            robot.getBrain().setMemory(IRobotMemoryModuleTypes.LANE_DIRECTION.get(), miningDir.getClockWise());
        }


        // --- LOCAL SEARCH ---
        // First, search for blocks in the immediate vicinity
        Optional<BlockPos> nearestBlock = findNearestBlock(level, robot, miningArea);

        if (nearestBlock.isPresent()) {
            BlockPos targetPos = nearestBlock.get();
            robot.getBrain().setMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get(), GlobalPos.of(level.dimension(), targetPos));
            robot.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(targetPos));
            // If not close enough to mine, walk towards it
            if (!robot.blockPosition().closerThan(targetPos, 2.0)) {
                robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, 0.5f, 2));
            }
        } else {
            // --- SYSTEMATIC REPOSITIONING ---
            // If no blocks are nearby, find the next mining face using a directed scan
            findNextMiningFace(level, robot, miningArea, gameTime);
        }
    }

    /**
     * Scans for the nearest non-air block in the robot's immediate vicinity.
     */
    private Optional<BlockPos> findNearestBlock(ServerLevel level, IRobotEntity robot, AABB miningArea) {
        BlockPos robotBlockPos = robot.blockPosition();
        AABB aroundRobot = new AABB(robotBlockPos).inflate(3, 2, 3);
        AABB searchBox = aroundRobot.intersect(miningArea);

        Map<Integer, List<BlockPos>> positions = new HashMap<>();
        for (int y = Mth.floor(searchBox.minY); y <= Mth.floor(searchBox.maxY); y++) {
            for (int x = Mth.floor(searchBox.minX); x <= Mth.floor(searchBox.maxX); x++) {
                for (int z = Mth.floor(searchBox.minZ); z <= Mth.floor(searchBox.maxZ); z++) {
                    positions.computeIfAbsent(y, k -> new ArrayList<>()).add(new BlockPos(x, y, z));
                }
            }
        }

        positions.values().forEach(list -> list.sort(Comparator.comparingDouble(pos -> pos.distSqr(robotBlockPos))));

        // Search from top to bottom, closest to farthest
        for (Integer y : positions.keySet().stream().sorted(Comparator.reverseOrder()).toList()) {
            for (BlockPos pos : positions.get(y)) {
                if (!level.getBlockState(pos).isAir() && !level.isOutsideBuildHeight(pos)) {
                    return Optional.of(pos);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Scans forward to find the next wall of blocks, or moves to the next lane if a row is complete.
     */
    private void findNextMiningFace(ServerLevel level, IRobotEntity robot, AABB miningArea, long gameTime) {
        Direction miningDir = robot.getBrain().getMemory(IRobotMemoryModuleTypes.MINING_DIRECTION.get()).orElse(Direction.EAST);
        BlockPos robotPos = robot.blockPosition();

        // Scan forward in the mining direction
        for (int i = 0; i < (int) Math.max(miningArea.getXsize(), miningArea.getZsize()); i += SCAN_STEP_SIZE) {
            BlockPos scanPos = robotPos.relative(miningDir, i);
            Optional<BlockPos> target = findWalkablePosNear(level, scanPos, miningArea);

            if (target.isPresent()) {
                robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target.get(), 0.5f, 0));
                return; // Found a new spot, exit
            }
        }

        // If scan completes, we've hit a wall. Move to the next lane.
        // Use the PERSISTENT lane direction from memory.
        Optional<Direction> laneDirOpt = robot.getBrain().getMemory(IRobotMemoryModuleTypes.LANE_DIRECTION.get());
        if (laneDirOpt.isEmpty()) {
            // Failsafe in case lane direction is somehow not set.
            finishMiningTask(robot);
            return;
        }
        Direction laneDir = laneDirOpt.get();
        BlockPos nextLanePos = robotPos.relative(laneDir, LANE_WIDTH);

        if (miningArea.contains(Vec3.atCenterOf(nextLanePos))) {
            // If the next lane is inside the area, move to it and reverse mining direction
            Direction newMiningDir = miningDir.getOpposite();
            robot.getBrain().setMemory(IRobotMemoryModuleTypes.MINING_DIRECTION.get(), newMiningDir);
            robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(nextLanePos, 0.5f, 0));
        } else {
            // We have reached the end of the mining area. Try a last resort search before finishing.
            lastResortSearch(level, robot, miningArea);
        }
    }

    /**
     * Finds a walkable position on the ground near a target that contains non-air blocks.
     */
    private Optional<BlockPos> findWalkablePosNear(ServerLevel level, BlockPos targetPos, AABB miningArea) {
        for (int y = Mth.floor(miningArea.maxY); y >= Mth.floor(miningArea.minY); --y) {
            BlockPos currentPos = new BlockPos(targetPos.getX(), y, targetPos.getZ());
            if (miningArea.contains(Vec3.atCenterOf(currentPos)) && !level.getBlockState(currentPos).isAir()) {
                // We found a solid block. Now find a walkable spot on the ground in front of it.
                BlockPos groundPos = currentPos.below();
                while (groundPos.getY() >= Mth.floor(miningArea.minY)) {
                    if (level.getBlockState(groundPos).isSolidRender() && level.getBlockState(groundPos.above()).isAir()) {
                        return Optional.of(groundPos.above());
                    }
                    groundPos = groundPos.below();
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Instead of finishing the task, performs a last-resort search for a walkable block in a large radius.
     */
    private void lastResortSearch(ServerLevel level, IRobotEntity robot, AABB miningArea) {
        BlockPos robotPos = robot.blockPosition();
        AABB searchBox = new AABB(robotPos).inflate(50, 5, 50).intersect(miningArea);
        PathNavigation navigator = robot.getNavigation();

        for (int y = Mth.floor(searchBox.maxY); y >= Mth.floor(searchBox.minY); y--) {
            for (int x = Mth.floor(searchBox.minX); x <= Mth.floor(searchBox.maxX); x++) {
                for (int z = Mth.floor(searchBox.minZ); z <= Mth.floor(searchBox.maxZ); z++) {
                    BlockPos potentialTarget = new BlockPos(x, y, z);
                    if (!level.getBlockState(potentialTarget).isAir()) {
                        BlockPos walkToTarget = potentialTarget.above();

                        if (level.getBlockState(walkToTarget).isAir()) {
                            Path path = navigator.createPath(walkToTarget, 1);

                            if (path != null && path.canReach()) {
                                robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(walkToTarget, 0.5f, 0));
                                return; // Found a walkable target
                            }
                        }
                    }
                }
            }
        }

        // If we get here, the last resort search failed. Now we can truly finish.
        finishMiningTask(robot);
    }


    /**
     * Clears all mining-related memories from the robot's brain.
     */
    private void finishMiningTask(IRobotEntity robot) {
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.MINE_TARGET_POS.get());
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.ASSIGNED_TASK.get());
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.TASK_AREA_START.get());
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.TASK_AREA_END.get());
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.MINING_DIRECTION.get());
        robot.getBrain().eraseMemory(IRobotMemoryModuleTypes.LANE_DIRECTION.get());
        robot.getBrain().setActiveActivityIfPossible(Activity.IDLE);
    }

    private Vec3 getClosestPointInAABB(Vec3 point, AABB box) {
        double x = Mth.clamp(point.x, box.minX, box.maxX);
        double y = Mth.clamp(point.y, box.minY, box.maxY);
        double z = Mth.clamp(point.z, box.minZ, box.maxZ);
        return new Vec3(x, y, z);
    }
}