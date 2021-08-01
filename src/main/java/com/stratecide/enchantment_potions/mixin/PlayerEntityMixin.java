package com.stratecide.enchantment_potions.mixin;

import com.stratecide.enchantment_potions.DummyHungerManager;
import com.stratecide.enchantment_potions.PotionsMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin {

    @Shadow protected HungerManager hungerManager;

    public PlayerEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    private HungerManager realHungerManager = null;

    @Inject(method = "tick", at=@At(value = "INVOKE",target = "Lnet/minecraft/entity/player/HungerManager;update(Lnet/minecraft/entity/player/PlayerEntity;)V"))
    public void injectTick(CallbackInfo ci) {
        double metabolism = getMetabolism();
        for (int i = 2; i <= metabolism; i++)
            hungerManager.update((PlayerEntity) (Object) this);
        if (metabolism % 1 > 0 && this.random.nextFloat() < metabolism % 1)
            hungerManager.update((PlayerEntity) (Object) this);
        if (metabolism < 1) {
            if (realHungerManager == null)
                realHungerManager = hungerManager;
            hungerManager = DummyHungerManager.getSingleton();
        }
    }
    @Inject(method = "tick", at=@At(value = "INVOKE",target = "Lnet/minecraft/entity/player/PlayerEntity;incrementStat(Lnet/minecraft/util/Identifier;)V", ordinal = 0))
    public void injectTick2(CallbackInfo ci) {
        if (realHungerManager != null) {
            hungerManager = realHungerManager;
            realHungerManager = null;
        }
    }

    @Inject(method = "applyDamage", at = @At("RETURN"))
    void injectApplyDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (amount > 0 && !this.isInvulnerableTo(source) && ((Entity) this) instanceof ServerPlayerEntity) {
            if (PotionsMod.BURST_CHANCE > 0 && (DamageSource.FALL == source || DamageSource.CACTUS == source ||
                    DamageSource.FLY_INTO_WALL == source || DamageSource.ANVIL == source || DamageSource.FALLING_BLOCK == source ||
                    source instanceof EntityDamageSource && !source.isMagic())) {
                ServerPlayerEntity self = (ServerPlayerEntity) (Entity) this;
                for(int i = 0; i < self.getInventory().size(); ++i) {
                    ItemStack itemStack = self.getInventory().getStack(i);
                    if (!itemStack.isEmpty() && (itemStack.getItem() == Items.POTION || itemStack.getItem() == Items.SPLASH_POTION || itemStack.getItem() == Items.LINGERING_POTION) && random.nextFloat() < PotionsMod.BURST_CHANCE) {
                        PotionEntity potionEntity = new PotionEntity(world, self);
                        potionEntity.setItem(itemStack);
                        potionEntity.setProperties(self, self.getPitch(), self.getYaw(), -20.0F, 0.5F, 1.0F);
                        world.spawnEntity(potionEntity);
                        PotionEntityInvoker pot = (PotionEntityInvoker) potionEntity;
                        pot.invokeOnCollision(new EntityHitResult(self));
                        itemStack.decrement(1);
                    }
                }
            }
        }
    }

}
