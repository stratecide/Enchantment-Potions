package com.stratecide.enchantment_potions.mixin;

import com.stratecide.enchantment_potions.ConfusionGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathAwareEntity.class)
public abstract class PathAwareEntityMixin extends MobEntity {
    protected PathAwareEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    void injectConstructor(EntityType<? extends PathAwareEntity> entityType, World world, CallbackInfo ci) {
        this.goalSelector.add(0, new ConfusionGoal((PathAwareEntity) (MobEntity) this));
    }
}
