package com.stratecide.enchantment_potions.mixin;

import com.stratecide.enchantment_potions.PotionsMod;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(method = "getEfficiency", at = @At("RETURN"), cancellable = true)
    private static void getEfficiencyInject(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        StatusEffectInstance statusEffectInstance = entity.getStatusEffect(PotionsMod.EFFICIENCY);
        if (statusEffectInstance != null) {
            cir.setReturnValue(cir.getReturnValue() + statusEffectInstance.getAmplifier() + 1);
        }
    }

    @Inject(method = "hasAquaAffinity", at = @At("RETURN"), cancellable = true)
    private static void hasAquaAffinityInject(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (PotionsMod.WATER_BREATHING_GIVES_AQUA_AFFINITY && entity.hasStatusEffect(StatusEffects.WATER_BREATHING)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getLooting", at = @At("RETURN"), cancellable = true)
    private static void getLootingInject(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        StatusEffectInstance statusEffectInstance = entity.getStatusEffect(StatusEffects.LUCK);
        if (statusEffectInstance != null) {
            cir.setReturnValue(cir.getReturnValue() + statusEffectInstance.getAmplifier() + 1);
        }
    }
}
