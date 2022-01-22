package com.stratecide.enchantment_potions.mixin;

import com.stratecide.enchantment_potions.DummyHungerManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
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

}
