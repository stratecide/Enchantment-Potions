package com.stratecide.enchantment_potions.mixin;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.class)
public interface ItemMixin {

    @Accessor("maxCount")
    public void setMaxCount(int value);
}
