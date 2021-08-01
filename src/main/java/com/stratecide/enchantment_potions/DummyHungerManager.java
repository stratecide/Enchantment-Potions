package com.stratecide.enchantment_potions;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;

public class DummyHungerManager extends HungerManager {

    public static DummyHungerManager getSingleton() {
        return SINGLETON;
    }

    private static final DummyHungerManager SINGLETON = new DummyHungerManager();

    private DummyHungerManager() {

    }

    @Override
    public void update(PlayerEntity player) {
        // do nothing
    }
}
