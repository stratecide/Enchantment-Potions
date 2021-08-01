package com.stratecide.enchantment_potions.mixin;

import com.stratecide.enchantment_potions.PotionsMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

@Mixin(ApplyBonusLootFunction.class)
public class ApplyBonusLootFunctionMixin {

    @Shadow @Final private Enchantment enchantment;

    @Inject(method = "process", at = @At("HEAD"), cancellable = true)
    private void processInject(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (PotionsMod.LUCK_GIVES_FORTUNE && this.enchantment == Enchantments.FORTUNE && context.get(LootContextParameters.THIS_ENTITY) instanceof LivingEntity) {
            ItemStack itemStack = (ItemStack)context.get(LootContextParameters.TOOL);
            int i = 0;
            if (itemStack != null) {
                i += EnchantmentHelper.getLevel(this.enchantment, itemStack);
            }
            LivingEntity entity = (LivingEntity) context.get(LootContextParameters.THIS_ENTITY);
            if (entity.hasStatusEffect(StatusEffects.LUCK)) {
                i += 1 + entity.getStatusEffect(StatusEffects.LUCK).getAmplifier();
            }
            if (i > 0) {
                System.out.println("ASDF sum total of Fortune " + i);
                Field formula = ApplyBonusLootFunction.class.getDeclaredField("formula");
                formula.setAccessible(true);
                Object f = formula.get(this);
                Method formulaGetValue = f.getClass().getDeclaredMethod("getValue", Random.class, int.class, int.class);
                int j = (int) formulaGetValue.invoke(f, context.getRandom(), stack.getCount(), i);
                stack.setCount(j);
            }
            cir.setReturnValue(stack);
        }
    }

}
