package com.stratecide.enchantment_potions.mixin;

import com.stratecide.enchantment_potions.PotionsMod;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {
    @ModifyArgs(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;<init>(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;II)V"))
    void modifyFishingEnchants(Args args, World world, PlayerEntity user, Hand hand) {
        if (PotionsMod.LUCK_GIVES_OF_THE_SEA) {
            StatusEffectInstance statusEffectInstance = user.getStatusEffect(StatusEffects.LUCK);
            if (statusEffectInstance != null) {
                args.set(2, ((int) args.get(2)) + statusEffectInstance.getAmplifier() + 1);
            }
        }
        StatusEffectInstance statusEffectInstance = user.getStatusEffect(PotionsMod.LURE);
        if (statusEffectInstance != null) {
            args.set(3, ((int) args.get(3)) + statusEffectInstance.getAmplifier() + 1);
        }
    }
}
