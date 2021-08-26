package com.stratecide.enchantment_potions.mixin;

import com.stratecide.enchantment_potions.PotionsMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ApplyBonusLootFunction.class)
public abstract class ApplyBonusLootFunctionMixin {

    private LivingEntity epEntity = null;
    private boolean epNegativeLuck;

    @Shadow @Final Enchantment enchantment;

    @Inject(method = "process", at = @At("HEAD"))
    private void processInject(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
        if (PotionsMod.LUCK_GIVES_FORTUNE && this.enchantment == Enchantments.FORTUNE && context.get(LootContextParameters.THIS_ENTITY) instanceof LivingEntity entity) {
            this.epEntity = entity;
        }
    }

    @Redirect(method = "process", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getLevel(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)I"))
    int redirectEnchantment(Enchantment enchantment, ItemStack stack) {
        int i = EnchantmentHelper.getLevel(this.enchantment, stack);
        if (epEntity != null) {
            StatusEffectInstance statusEffectInstance = epEntity.getStatusEffect(StatusEffects.LUCK);
            if (statusEffectInstance != null) {
                i += 1 + statusEffectInstance.getAmplifier();
            }
            statusEffectInstance = epEntity.getStatusEffect(StatusEffects.UNLUCK);
            if (statusEffectInstance != null) {
                i -= 1 + statusEffectInstance.getAmplifier();
            }
            epNegativeLuck = i < 0;
        }
        return i;
    }

    @Redirect(method = "process", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setCount(I)V"))
    void redirectSetCount(ItemStack itemStack, int count) {
        if (epNegativeLuck) {
            itemStack.setCount(itemStack.getCount() * 2 - count);
        } else {
            itemStack.setCount(count);
        }
        epNegativeLuck = false;
        epEntity = null;
    }

}
