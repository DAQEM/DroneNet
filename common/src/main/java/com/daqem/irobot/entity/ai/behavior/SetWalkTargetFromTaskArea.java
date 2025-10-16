package com.daqem.irobot.entity.ai.behavior;

import com.daqem.irobot.client.renderer.OutlineRenderer;
import com.daqem.irobot.entity.MiniRobotEntity;
import com.daqem.irobot.entity.ai.IRobotMemoryModuleTypes;
import com.daqem.irobot.entity.task.RobotTask;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetFromTaskArea {

    public static OneShot<MiniRobotEntity> create(float speedModifier, int closeEnoughDist, int tooFarDistance) {
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE), instance.absent(MemoryModuleType.WALK_TARGET), instance.present(IRobotMemoryModuleTypes.ASSIGNED_TASK.get()), instance.present(IRobotMemoryModuleTypes.TASK_AREA_START.get()), instance.present(IRobotMemoryModuleTypes.TASK_AREA_END.get()))
                .apply(
                        instance,
                        (
                                canReachWalkTagetSince,
                                walkTarget,
                                assignTask,
                                taskAreaStart,
                                taskAreaStop
                        ) -> (serverLevel, robot, l) -> {
                            GlobalPos startPos = instance.get(taskAreaStart);
                            GlobalPos stopPos = instance.get(taskAreaStop);
                            if (startPos.dimension() != stopPos.dimension()) return false;
                            ResourceKey<Level> dimension = startPos.dimension();
                            if (serverLevel.dimension() != dimension) return false;

                            Vec3 currentPos = robot.position();
                            AABB taskArea = OutlineRenderer.createBoundingBox(startPos.pos(), stopPos.pos());
                            Vec3 closestPoint = clampToAABB(currentPos, taskArea);
                            double distance = closestPoint.distanceTo(currentPos);
                            if (distance > tooFarDistance) {
                                assignTask.set(RobotTask.FAILED_TOO_FAR);
                                canReachWalkTagetSince.set(l);
                            }
                            if (distance > closeEnoughDist) {
                                walkTarget.set(new WalkTarget(closestPoint, speedModifier, closeEnoughDist));
                            }
                            return true;
                        }
                )
        );
    }

    public static Vec3 clampToAABB(Vec3 pos, AABB aabb) {
        double x = Mth.clamp(pos.x, aabb.minX, aabb.maxX);
        double y = Mth.clamp(pos.y, aabb.minY, aabb.maxY);
        double z = Mth.clamp(pos.z, aabb.minZ, aabb.maxZ);
        return new Vec3(x, y, z);
    }
}