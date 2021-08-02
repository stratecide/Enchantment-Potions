package com.stratecide.enchantment_potions.mixin;

import com.stratecide.enchantment_potions.PotionsMod;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow @Nullable public abstract StatusEffectInstance getStatusEffect(StatusEffect effect);

    @Shadow public abstract Iterable<ItemStack> getArmorItems();

    @Shadow @Final private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;

    @Shadow protected abstract void onStatusEffectRemoved(StatusEffectInstance effect);

    @Shadow public abstract void setHealth(float health);

    @Shadow public abstract boolean clearStatusEffects();

    @Shadow public abstract boolean addStatusEffect(StatusEffectInstance effect);

    @Shadow protected abstract void onStatusEffectUpgraded(StatusEffectInstance effect, boolean reapplyEffect, @Nullable Entity source);

    @Redirect(method = "applyEnchantmentsToDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getProtectionAmount(Ljava/lang/Iterable;Lnet/minecraft/entity/damage/DamageSource;)I"))
    private int modifyProtection(Iterable<ItemStack> equipment, DamageSource source) {
        int protection = EnchantmentHelper.getProtectionAmount(equipment, source);
        StatusEffectInstance statusEffectInstance = this.getStatusEffect(PotionsMod.PROTECTION);
        if (statusEffectInstance != null)
            protection += (1 + statusEffectInstance.getAmplifier()) * 2;
        return protection;
    }

    private double leftOverMetabolism = 0;

    public double getMetabolism() {
        double result = 0;
        StatusEffectInstance statusEffectInstance = this.activeStatusEffects.get(PotionsMod.FAST_METABOLISM);
        if (statusEffectInstance != null)
            result += statusEffectInstance.getAmplifier() + 1;
        statusEffectInstance = this.activeStatusEffects.get(PotionsMod.SLOW_METABOLISM);
        if (statusEffectInstance != null)
            result -= statusEffectInstance.getAmplifier() + 1;
        return Math.pow(2, result);
    }

    private boolean ignoreMetabolismForEffect(StatusEffect effect) {
        return StatusEffects.CONDUIT_POWER == effect ||
                StatusEffects.LUCK == effect ||
                StatusEffects.UNLUCK == effect ||
                StatusEffects.DOLPHINS_GRACE == effect ||
                StatusEffects.BAD_OMEN == effect ||
                StatusEffects.HERO_OF_THE_VILLAGE == effect ||
                PotionsMod.EFFICIENCY == effect ||
                PotionsMod.PROTECTION == effect ||
                PotionsMod.FAST_METABOLISM == effect ||
                PotionsMod.SLOW_METABOLISM == effect;
    }

    @ModifyVariable(method = "tickStatusEffects", at = @At("STORE"))
    Iterator<StatusEffect> applyMetabolismToStatusEffects(Iterator<StatusEffect> iterator) {
        double metabolism = getMetabolism() + leftOverMetabolism;
        leftOverMetabolism = metabolism % 1;

        try {
            while(iterator.hasNext()) {
                StatusEffect statusEffect = iterator.next();
                StatusEffectInstance statusEffectInstance = this.activeStatusEffects.get(statusEffect);
                boolean ignoreMetabolism = ignoreMetabolismForEffect(statusEffect);
                for (int i = ignoreMetabolism ? 0 : 1; i <= metabolism; i++) {
                    if (!statusEffectInstance.update((LivingEntity) ((Entity) this), () -> {
                        this.onStatusEffectUpgraded(statusEffectInstance, true, null);
                    })) {
                        if (!this.world.isClient) {
                            iterator.remove();
                            this.onStatusEffectRemoved(statusEffectInstance);
                            break; // don't want to repeat the status effect if it's already removed
                        }
                    } else if (statusEffectInstance.getDuration() % 600 == 0) {
                        this.onStatusEffectUpgraded(statusEffectInstance, false, null);
                    }
                    // some status effects shouldn't depend on metabolism
                    if (ignoreMetabolism)
                        break;
                }
            }
        } catch (ConcurrentModificationException var11) {
            // ignore
        }
        // return iterator that can't be iterated over anymore
        return iterator;
    }

    @Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
    void injectTryUseTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!source.isOutOfWorld() && this.activeStatusEffects.get(PotionsMod.UNDYING) != null) {
            this.setHealth(1.0F);
            this.clearStatusEffects();
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
            this.world.sendEntityStatus(this, (byte)35);
            cir.setReturnValue(true);
        }
    }

}
