package com.stratecide.enchantment_potions;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;

import java.util.EnumSet;

public class ConfusionGoal extends Goal {

    PathAwareEntity entity;
    double turnSpeed;

    public ConfusionGoal(PathAwareEntity entity) {
        this.entity = entity;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return entity.getActiveStatusEffects().get(PotionsMod.CONFUSION) != null;
    }

    @Override
    public boolean canStop() {
        return !canStart();
    }

    @Override
    public void start() {
        turnSpeed = Math.PI / (15. + entity.getRandom().nextFloat() * 15.);
        if (entity.getRandom().nextFloat() < 0.5)
            turnSpeed *= -1.;
    }

    @Override
    public void tick() {
        double angle = Math.atan2(this.entity.getLookControl().getLookZ() - this.entity.getZ(), this.entity.getLookControl().getLookX() - this.entity.getX());
        angle += turnSpeed;
        System.out.println("ConfusionGoal::tick " + angle);
        this.entity.getLookControl().lookAt(this.entity.getX() + Math.cos(angle), this.entity.getEyeY(), this.entity.getZ() + Math.sin(angle));
    }
}
