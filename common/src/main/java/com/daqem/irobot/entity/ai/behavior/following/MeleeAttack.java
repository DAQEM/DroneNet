package com.daqem.irobot.entity.ai.behavior.following;

import com.daqem.irobot.entity.IRobotEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;

import java.util.function.Predicate;

public class MeleeAttack extends Behavior<IRobotEntity> {

    private final int attackCooldown;
    private final Predicate<IRobotEntity> canAttackPredicate;

    public MeleeAttack(int attackCooldown) {
        this(attackCooldown, (mob) -> true);
    }

    public MeleeAttack(int attackCooldown, Predicate<IRobotEntity> canAttackPredicate) {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
        ));
        this.attackCooldown = attackCooldown;
        this.canAttackPredicate = canAttackPredicate;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, IRobotEntity mob) {
        LivingEntity target = this.getAttackTarget(mob);
        return target != null
                && this.canAttackPredicate.test(mob)
                && !isHoldingUsableProjectileWeapon(mob)
                && mob.isWithinMeleeAttackRange(target)
                && isTargetVisible(mob, target);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, IRobotEntity entity, long gameTime) {
        LivingEntity target = this.getAttackTarget(entity);
        return target != null && entity.isWithinMeleeAttackRange(target) && isTargetVisible(entity, target);
    }

    @Override
    protected void start(ServerLevel level, IRobotEntity mob, long gameTime) {
        LivingEntity target = this.getAttackTarget(mob);
        if (target == null) {
            doStop(level, mob, gameTime);
            return;
        }
        mob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
        mob.getInventory().selectBestWeapon(target);
        mob.swing(InteractionHand.MAIN_HAND);
        mob.doHurtTarget(level, target);
        mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, this.attackCooldown);
    }

    private LivingEntity getAttackTarget(IRobotEntity mob) {
        return mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    private static boolean isTargetVisible(IRobotEntity mob, LivingEntity target) {
        return mob.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .map(nearest -> nearest.contains(target))
                .orElse(false);
    }

    private static boolean isHoldingUsableProjectileWeapon(IRobotEntity mob) {
        return mob.isHolding((itemStack) -> {
            Item item = itemStack.getItem();
            return item instanceof ProjectileWeaponItem && mob.canFireProjectileWeapon((ProjectileWeaponItem) item);
        });
    }
}