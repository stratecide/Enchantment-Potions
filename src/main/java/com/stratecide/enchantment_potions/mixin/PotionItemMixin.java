package com.stratecide.enchantment_potions.mixin;

import com.stratecide.enchantment_potions.PotionsMod;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PotionItem.class)
public class PotionItemMixin {

    @Redirect(method = "finishUsing", at=@At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"))
    boolean dropBottleIfNotEnoughInventory(PlayerInventory playerInventory, ItemStack stack) {
        if (!playerInventory.insertStack(stack)) {
            playerInventory.player.dropItem(stack, false);
        }
        return false;
    }

    @Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
    void injectHasGlint(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!PotionsMod.GLINT)
            cir.setReturnValue(false);
        List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(stack);
        if (effects.size() == 1 && effects.get(0).getEffectType() == PotionsMod.MILK)
            cir.setReturnValue(false);
    }
}
