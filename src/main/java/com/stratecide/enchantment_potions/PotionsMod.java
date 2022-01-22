package com.stratecide.enchantment_potions;

import com.stratecide.enchantment_potions.mixin.BrewingRecipeRegistryMixin;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.effect.*;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PotionsMod implements ModInitializer {

	public static final String MOD_ID = "enchantment_potions";

	public static final StatusEffect MILK;
	public static final StatusEffect EFFICIENCY;
	public static final StatusEffect FAST_METABOLISM;
	public static final StatusEffect SLOW_METABOLISM;
	public static final StatusEffect CONFUSION;
	public static final StatusEffect UNDYING;
	public static final StatusEffect DEPTH_STRIDER;
	public static final StatusEffect FEATHER_FALLING;
	public static final StatusEffect FROST_WALKER;
	public static final StatusEffect KNOCKBACK;
	public static final StatusEffect LURE;
	public static final StatusEffect SILK_TOUCH;
	public static final StatusEffect SOUL_SPEED;

	public static final Potion MILK_POTION;
	public static final List<Potion> EFFICIENCY_POTIONS;
	public static final List<Potion> LUCK_POTIONS;
	public static final List<Potion> FAST_METABOLISM_POTIONS;
	public static final List<Potion> SLOW_METABOLISM_POTIONS;
	public static final List<Potion> CONFUSION_POTIONS;
	public static final List<Potion> UNDYING_POTIONS;
	public static final List<Potion> DEPTH_STRIDER_POTIONS;
	public static final List<Potion> FEATHER_FALLING_POTIONS;
	public static final List<Potion> FROST_WALKER_POTIONS;
	public static final List<Potion> KNOCKBACK_POTIONS;
	public static final List<Potion> LURE_POTIONS;
	public static final List<Potion> SILK_TOUCH_POTIONS;
	public static final List<Potion> SOUL_SPEED_POTIONS;

	private static final String CONFIG_FILE = "config/enchantment_potions.txt";
	public static boolean WATER_BREATHING_GIVES_AQUA_AFFINITY = true;
	public static boolean LUCK_GIVES_LOOTING = true;
	public static boolean LUCK_GIVES_FORTUNE = true;
	public static boolean LUCK_GIVES_OF_THE_SEA = true;

	private static StatusEffect registerEffect(String id, StatusEffect entry) {
		return Registry.register(Registry.STATUS_EFFECT, new Identifier(MOD_ID, id), entry);
	}
	private static Potion registerPotion(String id, Potion potion) {
		return Registry.register(Registry.POTION, new Identifier(MOD_ID, id), potion);
	}

	static {
		readConfig();
		MILK = registerEffect("milk", new InstantStatusEffect(StatusEffectCategory.NEUTRAL, 0xffffff));
		EFFICIENCY = registerEffect("efficiency", new StatusEffectModded(StatusEffectCategory.BENEFICIAL, 0x8833ee));
		FAST_METABOLISM = registerEffect("metabolism_high", new StatusEffectModded(StatusEffectCategory.NEUTRAL, 0xaa0088));
		SLOW_METABOLISM = registerEffect("metabolism_low", new StatusEffectModded(StatusEffectCategory.NEUTRAL, 0xaa8800));
		CONFUSION = registerEffect("confusion", new StatusEffectModded(StatusEffectCategory.HARMFUL, 0xb1b1b1));
		UNDYING = registerEffect("undying", new StatusEffectModded(StatusEffectCategory.BENEFICIAL, 0xffdd00));
		DEPTH_STRIDER = registerEffect("depth_strider", new StatusEffectModded(StatusEffectCategory.BENEFICIAL, 0x0066bb));
		FEATHER_FALLING = registerEffect("feather_falling", new StatusEffectModded(StatusEffectCategory.BENEFICIAL, 0xccbbdd));
		FROST_WALKER = registerEffect("frost_walker", new StatusEffectModded(StatusEffectCategory.BENEFICIAL, 0x55ccff));
		KNOCKBACK = registerEffect("knockback", new StatusEffectModded(StatusEffectCategory.BENEFICIAL, 0x554422));
		LURE = registerEffect("lure", new StatusEffectModded(StatusEffectCategory.BENEFICIAL, 0xaa1155));
		SILK_TOUCH = registerEffect("silk_touch", new StatusEffectModded(StatusEffectCategory.BENEFICIAL, 0xddcc77));
		SOUL_SPEED = registerEffect("soul_speed", new StatusEffectModded(StatusEffectCategory.BENEFICIAL, 0x777777));

		MILK_POTION = registerPotion("milk", new Potion(new StatusEffectInstance(MILK)));
		EFFICIENCY_POTIONS = new ArrayList<>();
		for (int amplifier = 1; amplifier <= 5; amplifier++) {
			EFFICIENCY_POTIONS.add(registerPotion("efficiency_" + amplifier, new Potion(new StatusEffectInstance(EFFICIENCY, 12000 + (5 - amplifier) * 1200, amplifier - 1))));
		}
		LUCK_POTIONS = new ArrayList<>();
		LUCK_POTIONS.add(Potions.LUCK);
		for (int amplifier = 2; amplifier <= 3; amplifier++) {
			LUCK_POTIONS.add(registerPotion("luck_" + amplifier, new Potion(new StatusEffectInstance(StatusEffects.LUCK, 6000, amplifier - 1))));
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
		DEPTH_STRIDER_POTIONS = new ArrayList<>();
		for (int amplifier = 1; amplifier <= 3; amplifier++) {
			DEPTH_STRIDER_POTIONS.add(registerPotion("depth_strider_" + amplifier, new Potion(new StatusEffectInstance(DEPTH_STRIDER, 9600, amplifier - 1))));
		}
		FEATHER_FALLING_POTIONS = new ArrayList<>();
		for (int amplifier = 1; amplifier <= 4; amplifier++) {
			FEATHER_FALLING_POTIONS.add(registerPotion("feather_falling_" + amplifier, new Potion(new StatusEffectInstance(FEATHER_FALLING, 9600, amplifier - 1))));
		}
		FROST_WALKER_POTIONS = new ArrayList<>();
		for (int amplifier = 1; amplifier <= 2; amplifier++) {
			FROST_WALKER_POTIONS.add(registerPotion("frost_walker_" + amplifier, new Potion(new StatusEffectInstance(FROST_WALKER, 9600, amplifier - 1))));
		}
		KNOCKBACK_POTIONS = new ArrayList<>();
		for (int amplifier = 1; amplifier <= 2; amplifier++) {
			KNOCKBACK_POTIONS.add(registerPotion("knockback_" + amplifier, new Potion(new StatusEffectInstance(KNOCKBACK, 4800, amplifier - 1))));
		}
		LURE_POTIONS = new ArrayList<>();
		for (int amplifier = 1; amplifier <= 2; amplifier++) {
			LURE_POTIONS.add(registerPotion("lure_" + amplifier, new Potion(new StatusEffectInstance(LURE, 6000, amplifier - 1))));
		}
		SILK_TOUCH_POTIONS = new ArrayList<>();
		for (int amplifier = 1; amplifier <= 2; amplifier++) {
			SILK_TOUCH_POTIONS.add(registerPotion("silk_touch_" + amplifier, new Potion(new StatusEffectInstance(SILK_TOUCH, 600 + 11400 * (amplifier - 1), 0))));
		}
		SOUL_SPEED_POTIONS = new ArrayList<>();
		for (int amplifier = 1; amplifier <= 3; amplifier++) {
			SOUL_SPEED_POTIONS.add(registerPotion("soul_speed_" + amplifier, new Potion(new StatusEffectInstance(SOUL_SPEED, 9600, amplifier - 1))));
		}

		BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(Potions.MUNDANE, Items.COPPER_INGOT, EFFICIENCY_POTIONS.get(0));
		for (int i = 1; i < EFFICIENCY_POTIONS.size(); i++) {
			BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(EFFICIENCY_POTIONS.get(i - 1), Items.COPPER_INGOT, EFFICIENCY_POTIONS.get(i));
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

		BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(Potions.MUNDANE, Items.PRISMARINE_CRYSTALS, DEPTH_STRIDER_POTIONS.get(0));
		for (int i = 1; i < DEPTH_STRIDER_POTIONS.size(); i++) {
			BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(DEPTH_STRIDER_POTIONS.get(i - 1), Items.PRISMARINE_CRYSTALS, DEPTH_STRIDER_POTIONS.get(i));
		}

		BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(Potions.MUNDANE, Items.FEATHER, FEATHER_FALLING_POTIONS.get(0));
		for (int i = 1; i < FEATHER_FALLING_POTIONS.size(); i++) {
			BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(FEATHER_FALLING_POTIONS.get(i - 1), Items.FEATHER, FEATHER_FALLING_POTIONS.get(i));
		}

		BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(Potions.MUNDANE, Items.SNOW_BLOCK, FROST_WALKER_POTIONS.get(0));
		for (int i = 1; i < FROST_WALKER_POTIONS.size(); i++) {
			BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(FROST_WALKER_POTIONS.get(i - 1), Items.SNOW_BLOCK, FROST_WALKER_POTIONS.get(i));
		}

		BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(Potions.MUNDANE, Items.COOKIE, KNOCKBACK_POTIONS.get(0));
		for (int i = 1; i < KNOCKBACK_POTIONS.size(); i++) {
			BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(KNOCKBACK_POTIONS.get(i - 1), Items.COOKIE, KNOCKBACK_POTIONS.get(i));
		}

		BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(Potions.MUNDANE, Items.BEETROOT, LURE_POTIONS.get(0));
		for (int i = 1; i < LURE_POTIONS.size(); i++) {
			BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(LURE_POTIONS.get(i - 1), Items.BEETROOT, LURE_POTIONS.get(i));
		}

		BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(Potions.MUNDANE, Items.TALL_GRASS, SILK_TOUCH_POTIONS.get(0));
		for (int i = 1; i < SILK_TOUCH_POTIONS.size(); i++) {
			BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(SILK_TOUCH_POTIONS.get(i - 1), Items.TALL_GRASS, SILK_TOUCH_POTIONS.get(i));
		}

		BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(Potions.MUNDANE, Items.NETHER_WART, SOUL_SPEED_POTIONS.get(0));
		for (int i = 1; i < SOUL_SPEED_POTIONS.size(); i++) {
			BrewingRecipeRegistryMixin.invokeRegisterPotionRecipe(SOUL_SPEED_POTIONS.get(i - 1), Items.TALL_GRASS, SOUL_SPEED_POTIONS.get(i));
		}
	}

	public static void readConfig() {
		File file = new File(CONFIG_FILE);
		if (!file.exists()) {
			try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
				writer.write("""
						water_breathing_gives_aqua_affinity = true
						luck_gives_looting = true
						luck_gives_fortune = true
						luck_gives_of_the_sea = true
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
					case "water_breathing_gives_aqua_affinity" ->
							WATER_BREATHING_GIVES_AQUA_AFFINITY = value.startsWith("t");
					case "luck_gives_looting" ->
							LUCK_GIVES_LOOTING = value.startsWith("t");
					case "luck_gives_fortune" ->
							LUCK_GIVES_FORTUNE = value.startsWith("t");
					case "luck_gives_of_the_sea" ->
							LUCK_GIVES_OF_THE_SEA = value.startsWith("t");
				}
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onInitialize() {
	}
}
