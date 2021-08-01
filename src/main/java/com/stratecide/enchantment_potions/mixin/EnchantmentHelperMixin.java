package com.stratecide.enchantment_potions.mixin;

import com.stratecide.enchantment_potions.PotionsMod;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(method = "getEfficiency", at = @At("RETURN"), cancellable = true)
    private static void getEfficiencyInject(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (entity.hasStatusEffect(PotionsMod.EFFICIENCY)) {
            cir.setReturnValue(cir.getReturnValue() + entity.getStatusEffect(PotionsMod.EFFICIENCY).getAmplifier() + 1);
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
        if (PotionsMod.LUCK_GIVES_LOOTING && entity.hasStatusEffect(StatusEffects.LUCK)) {
            cir.setReturnValue(cir.getReturnValue() + entity.getStatusEffect(StatusEffects.LUCK).getAmplifier() + 1);
        }
    }
}
