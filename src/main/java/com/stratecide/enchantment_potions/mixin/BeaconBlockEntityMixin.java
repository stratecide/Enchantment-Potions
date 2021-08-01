package com.stratecide.enchantment_potions.mixin;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTime()J"))
    private static long getTimeModulo60(World world) {
        return world.getTime() % 60L;
    }
}
