package com.stratecide.enchantment_potions.mixin;

import com.stratecide.enchantment_potions.PotionsMod;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow public abstract boolean hasStatusEffect(StatusEffect effect);

    @Shadow @Nullable public abstract StatusEffectInstance getStatusEffect(StatusEffect effect);

    @Shadow public abstract Iterable<ItemStack> getArmorItems();

    @Shadow @Final private Map<StatusEffect, StatusEffectInstance> activeStatusEffects;

    @Shadow protected abstract void onStatusEffectUpgraded(StatusEffectInstance effect, boolean reapplyEffect);

    @Shadow protected abstract void onStatusEffectRemoved(StatusEffectInstance effect);

    @Shadow private boolean effectsChanged;

    @Shadow protected abstract void updatePotionVisibility();

    @Shadow @Final private static TrackedData<Integer> POTION_SWIRLS_COLOR;

    @Shadow @Final private static TrackedData<Boolean> POTION_SWIRLS_AMBIENT;

    @Shadow public abstract Random getRandom();

    @Shadow public abstract double getAttributeValue(EntityAttribute attribute);

    @Shadow public abstract void setHealth(float health);

    @Shadow public abstract boolean clearStatusEffects();

    @Shadow public abstract boolean addStatusEffect(StatusEffectInstance effect);

    @Inject(method = "applyEnchantmentsToDamage", at = @At("HEAD"), cancellable = true)
    private void applyEnchantmentsToDamageInject(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        if (source.isUnblockable()) {
            return;
        } else {
            int k;
            if (this.hasStatusEffect(StatusEffects.RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
                k = (this.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5;
                int j = 25 - k;
                float f = amount * (float)j;
                float g = amount;
                amount = Math.max(f / 25.0F, 0.0F);
                float h = g - amount;
                if (h > 0.0F && h < 3.4028235E37F) {
                    Object self = this;
                    if (self instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity)self).increaseStat(Stats.DAMAGE_RESISTED, Math.round(h * 10.0F));
                    } else if (source.getAttacker() instanceof ServerPlayerEntity) {
                        ((ServerPlayerEntity)source.getAttacker()).increaseStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(h * 10.0F));
                    }
                }
            }

            if (amount <= 0.0F) {
                cir.setReturnValue(0.0F);
            } else {
                k = EnchantmentHelper.getProtectionAmount(this.getArmorItems(), source);
                if (this.hasStatusEffect(PotionsMod.PROTECTION))
                    k += (1 + this.getStatusEffect(PotionsMod.PROTECTION).getAmplifier()) * 2;
                if (k > 0) {
                    amount = DamageUtil.getInflictedDamage(amount, (float)k);
                }

                cir.setReturnValue(amount);
            }
        }
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
        if (StatusEffects.CONDUIT_POWER == effect ||
                StatusEffects.LUCK == effect ||
                StatusEffects.UNLUCK == effect ||
                StatusEffects.DOLPHINS_GRACE == effect ||
                StatusEffects.BAD_OMEN == effect ||
                StatusEffects.HERO_OF_THE_VILLAGE == effect ||
                PotionsMod.EFFICIENCY == effect ||
                PotionsMod.PROTECTION == effect ||
                PotionsMod.FAST_METABOLISM == effect ||
                PotionsMod.SLOW_METABOLISM == effect)
            return true;
        return false;
    }

    @Inject(method = "tickStatusEffects", at = @At("HEAD"), cancellable = true)
    protected void injectTickStatusEffects(CallbackInfo ci) {
        Iterator iterator = this.activeStatusEffects.keySet().iterator();

        double metabolism = getMetabolism() + leftOverMetabolism;
        leftOverMetabolism = metabolism % 1;

        try {
            while(iterator.hasNext()) {
                StatusEffect statusEffect = (StatusEffect) iterator.next();
                StatusEffectInstance statusEffectInstance = this.activeStatusEffects.get(statusEffect);
                boolean ignoreMetabolism = ignoreMetabolismForEffect(statusEffect);
                for (int i = ignoreMetabolism ? 0 : 1; i <= metabolism; i++) {
                    if (!statusEffectInstance.update((LivingEntity) ((Entity) this), () -> {
                        this.onStatusEffectUpgraded(statusEffectInstance, true);
                    })) {
                        if (!this.world.isClient) {
                            iterator.remove();
                            this.onStatusEffectRemoved(statusEffectInstance);
                            break;
                        }
                    } else if (statusEffectInstance.getDuration() % 600 == 0) {
                        this.onStatusEffectUpgraded(statusEffectInstance, false);
                    }
                    // some status effects shouldn't depend on metabolism
                    if (ignoreMetabolism)
                        break;
                }
            }
        } catch (ConcurrentModificationException var11) {
        }

        if (this.effectsChanged) {
            if (!this.world.isClient) {
                this.updatePotionVisibility();
            }

            this.effectsChanged = false;
        }

        int i = (Integer)this.dataTracker.get(POTION_SWIRLS_COLOR);
        boolean bl = (Boolean)this.dataTracker.get(POTION_SWIRLS_AMBIENT);
        if (i > 0) {
            boolean bl3;
            if (this.isInvisible()) {
                bl3 = this.getRandom().nextInt(15) == 0;
            } else {
                bl3 = this.getRandom().nextBoolean();
            }

            if (bl) {
                bl3 &= this.getRandom().nextInt(5) == 0;
            }

            if (bl3 && i > 0) {
                double d = (double)(i >> 16 & 255) / 255.0D;
                double e = (double)(i >> 8 & 255) / 255.0D;
                double f = (double)(i >> 0 & 255) / 255.0D;
                this.world.addParticle(bl ? ParticleTypes.AMBIENT_ENTITY_EFFECT : ParticleTypes.ENTITY_EFFECT, this.getParticleX(0.5D), this.getRandomBodyY(), this.getParticleZ(0.5D), d, e, f);
            }
        }

        ci.cancel();
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
