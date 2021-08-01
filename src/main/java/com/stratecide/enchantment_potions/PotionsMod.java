package com.stratecide.enchantment_potions;

import com.stratecide.enchantment_potions.mixin.BrewingRecipeRegistryMixin;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PotionsMod implements ModInitializer {

	public static final String MOD_ID = "enchantment_potions";

	public static int STACK_SIZE = 16;

	public static final StatusEffect EFFICIENCY;
	public static final StatusEffect PROTECTION;
	public static final StatusEffect FAST_METABOLISM;
	public static final StatusEffect SLOW_METABOLISM;
	public static final StatusEffect CONFUSION;
	public static final StatusEffect UNDYING;

	public static final List<Potion> EFFICIENCY_POTIONS;
	public static final List<Potion> PROTECTION_POTIONS;
	public static final List<Potion> LUCK_POTIONS;
	public static final List<Potion> FAST_METABOLISM_POTIONS;
	public static final List<Potion> SLOW_METABOLISM_POTIONS;
	public static final List<Potion> CONFUSION_POTIONS;
	public static final List<Potion> UNDYING_POTIONS;

	public static final Item MILK_BOTTLE;

	private static final String CONFIG_FILE = "config/enchantment_potions.txt";
	public static boolean WATER_BREATHING_GIVES_AQUA_AFFINITY = true;
	public static boolean LUCK_GIVES_LOOTING = true;
	public static boolean LUCK_GIVES_FORTUNE = true;
	public static double BURST_CHANCE = 0.01; // chance per potion-stack. should be lower than 5%

	private static StatusEffect registerEffect(String id, StatusEffect entry) {
		return Registry.register(Registry.STATUS_EFFECT, new Identifier(MOD_ID, id), entry);
	}
	private static Potion registerPotion(String id, Potion potion) {
		return Registry.register(Registry.POTION, new Identifier(MOD_ID, id), potion);
	}

	static {
		readConfig();
		MILK_BOTTLE = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "milk_bottle"), (Item)(new MilkBottleItem((new Item.Settings()).maxCount(STACK_SIZE).group(ItemGroup.MISC))));

		EFFICIENCY = registerEffect("efficiency", new StatusEffectModded(StatusEffectType.BENEFICIAL, 0x8833ee));
		PROTECTION = registerEffect("protection", new StatusEffectModded(StatusEffectType.BENEFICIAL, 0x0033ee));
		FAST_METABOLISM = registerEffect("metabolism_high", new StatusEffectModded(StatusEffectType.NEUTRAL, 0xaa0088));
		SLOW_METABOLISM = registerEffect("metabolism_low", new StatusEffectModded(StatusEffectType.NEUTRAL, 0xaa0088));
		CONFUSION = registerEffect("confusion", new StatusEffectModded(StatusEffectType.HARMFUL, 0xb1b1b1));
		UNDYING = registerEffect("undying", new StatusEffectModded(StatusEffectType.BENEFICIAL, 0xffdd00));

		EFFICIENCY_POTIONS = new ArrayList<>();
		for (int amplifier = 1; amplifier <= 5; amplifier++) {
			EFFICIENCY_POTIONS.add(registerPotion("efficiency_" + amplifier, new Potion(new StatusEffectInstance(EFFICIENCY, 12000 + (5 - amplifier) * 1200, amplifier - 1))));
		}
		PROTECTION_POTIONS = new ArrayList<>();
		for (int amplifier = 1; amplifier <= 4; amplifier++) {
			PROTECTION_POTIONS.add(registerPotion("protection_" + amplifier, new Potion(new StatusEffectInstance(PROTECTION, 3600 + (4 - amplifier) * 1200, amplifier - 1))));
		}
		LUCK_POTIONS = new ArrayList<>();
		LUCK_POTIONS.add(Potions.LUCK);
		for (int amplifier = 2; amplifier <= 3; amplifier++) {
			LUCK_POTIONS.add(registerPotion("luck_" + amplifier, new Potion("luck", new StatusEffectInstance(StatusEffects.LUCK, 6000, amplifier - 1))));
		}
		FAST_METABOLISM_POTIONS = new ArrayList<>();
		SLOW_METABOLISM_POTIONS = new ArrayList<>();
		for (int amplifier = 1; amplifier <= 2; amplifier++) {
			FAST_METABOLISM_POTIONS.add(registerPotion("fast_metabolism_" + amplifier, new Potion(new StatusEffectInstance(FAST_METABOLISM, 900, amplifier - 1))));
			SLOW_METABOLISM_POTIONS.add(registerPotion("slow_metabolism_" + amplifier, new Potion(new StatusEffectInstance(SLOW_METABOLISM, 1800, amplifier - 1))));
		}
		CONFUSION_POTIONS = new ArrayList<>();
		for (int amplifier = 1; amplifier <= 2; amplifier++) {
			CONFUSION_POTIONS.add(registerPotion("confusion_" + amplifier, new Potion(new StatusEffectInstance(CONFUSION, 40 + 160 * (amplifier - 1), 0))));
		}
		UNDYING_POTIONS = new ArrayList<>();
		for (int amplifier = 1; amplifier <= 2; amplifier++) {
			UNDYING_POTIONS.add(registerPotion("undying_" + amplifier, new Potion(new StatusEffectInstance(UNDYING, 3600 + 2400 * (amplifier - 1), 0))));
		}

		BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(Potions.MUNDANE, Items.REDSTONE, EFFICIENCY_POTIONS.get(0));
		for (int i = 1; i < EFFICIENCY_POTIONS.size(); i++) {
			BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(EFFICIENCY_POTIONS.get(i - 1), Items.GLOWSTONE_DUST, EFFICIENCY_POTIONS.get(i));
		}

		BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(Potions.MUNDANE, Items.SCUTE, PROTECTION_POTIONS.get(0));
		for (int i = 1; i < PROTECTION_POTIONS.size(); i++) {
			BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(PROTECTION_POTIONS.get(i - 1), Items.SCUTE, PROTECTION_POTIONS.get(i));
		}

		BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(Potions.MUNDANE, Items.RABBIT_FOOT, LUCK_POTIONS.get(0));
		for (int i = 1; i < LUCK_POTIONS.size(); i++) {
			BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(LUCK_POTIONS.get(i - 1), Items.RABBIT_FOOT, LUCK_POTIONS.get(i));
		}

		BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(Potions.MUNDANE, Items.SUGAR, FAST_METABOLISM_POTIONS.get(0));
		for (int i = 0; i < FAST_METABOLISM_POTIONS.size(); i++) {
			if (i > 0) {
				BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(FAST_METABOLISM_POTIONS.get(i - 1), Items.SUGAR, FAST_METABOLISM_POTIONS.get(i));
				BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(SLOW_METABOLISM_POTIONS.get(i - 1), Items.SUGAR, SLOW_METABOLISM_POTIONS.get(i));
			}
			BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(FAST_METABOLISM_POTIONS.get(i), Items.SPIDER_EYE, SLOW_METABOLISM_POTIONS.get(i));
			BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(SLOW_METABOLISM_POTIONS.get(i), Items.SPIDER_EYE, FAST_METABOLISM_POTIONS.get(i));
		}

		BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(Potions.MUNDANE, Items.POISONOUS_POTATO, CONFUSION_POTIONS.get(0));
		for (int i = 1; i < CONFUSION_POTIONS.size(); i++) {
			BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(CONFUSION_POTIONS.get(i - 1), Items.POISONOUS_POTATO, CONFUSION_POTIONS.get(i));
		}

		BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(Potions.MUNDANE, Items.EMERALD_BLOCK, UNDYING_POTIONS.get(0));
		for (int i = 1; i < UNDYING_POTIONS.size(); i++) {
			BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(UNDYING_POTIONS.get(i - 1), Items.EMERALD_BLOCK, UNDYING_POTIONS.get(i));
		}
	}

	public static void readConfig() {
		File file = new File(CONFIG_FILE);
		if (!file.exists()) {
			try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
				writer.write("""
						stack = 16
						water_breathing_gives_aqua_affinity = true
						luck_gives_looting = true
						luck_gives_fortune = true
						burst_chance = 0.005
						""");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				String[] lineData = scanner.nextLine().split("=");
				if (lineData.length != 2)
					continue;
				String value = lineData[1].trim();
				switch (lineData[0].trim()) {
					case "stack" ->
							STACK_SIZE = Math.max(1, Math.min(64, Integer.parseInt(value)));
					case "water_breathing_gives_aqua_affinity" ->
							WATER_BREATHING_GIVES_AQUA_AFFINITY = value.startsWith("t");
					case "luck_gives_looting" ->
							LUCK_GIVES_LOOTING = value.startsWith("t");
					case "luck_gives_fortune" ->
							LUCK_GIVES_FORTUNE = value.startsWith("t");
					case "burst_chance" ->
							BURST_CHANCE = Math.max(0, Math.min(1, Double.parseDouble(value)));
				}
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onInitialize() {
		try {
			Field maxCount = Item.class.getDeclaredField("maxCount");
			maxCount.setAccessible(true);
			maxCount.setInt(Items.POTION, STACK_SIZE);
			maxCount.setAccessible(false);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
