/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.api.configdata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.storage.loot.LootTable;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public record GaiaFightConfiguration(
		Identifier rewardLootTable,
		int teleportDelay,
		IntProvider mineCount,
		float dyingHealthPercent,
		int dyingTeleportDelay,
		IntProvider dyingMineCount,
		IntProvider pixiesAfterHurtCount,
		IntProvider pixiesAfterTeleportCount,
		int missileSpawnInterval,
		IntProvider missileSpawnCount,
		IntProvider mobSpawnsPerPlayer,
		WeightedList<MobSpawnData> spawnedMobs,
		List<MobAttributeModifier> attributeModifiers,
		List<MobEffectToApply> effectsToApply) {

	public static final Codec<GaiaFightConfiguration> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Identifier.CODEC.fieldOf("rewardLootTable")
							.forGetter(GaiaFightConfiguration::rewardLootTable),
					Codec.INT.fieldOf("teleportDelay")
							.forGetter(GaiaFightConfiguration::teleportDelay),
					IntProviders.CODEC.fieldOf("mineCount")
							.forGetter(GaiaFightConfiguration::mineCount),
					Codec.FLOAT.fieldOf("dyingHealthPercent")
							.forGetter(GaiaFightConfiguration::dyingHealthPercent),
					Codec.INT.fieldOf("dyingTeleportDelay")
							.forGetter(GaiaFightConfiguration::dyingTeleportDelay),
					IntProviders.CODEC.fieldOf("dyingMineCount")
							.forGetter(GaiaFightConfiguration::dyingMineCount),
					IntProviders.CODEC.fieldOf("pixiesAfterHurtCount")
							.forGetter(GaiaFightConfiguration::pixiesAfterHurtCount),
					IntProviders.CODEC.fieldOf("pixiesAfterTeleportCount")
							.forGetter(GaiaFightConfiguration::pixiesAfterTeleportCount),
					Codec.INT.optionalFieldOf("missileSpawnInterval", 0)
							.forGetter(GaiaFightConfiguration::missileSpawnInterval),
					IntProviders.CODEC.optionalFieldOf("missileSpawnCount", ConstantInt.ZERO)
							.forGetter(GaiaFightConfiguration::missileSpawnCount),
					IntProviders.CODEC.fieldOf("mobSpawnsPerPlayer")
							.forGetter(GaiaFightConfiguration::mobSpawnsPerPlayer),
					WeightedList.codec(MobSpawnData.CODEC)
							.validate(list -> list.isEmpty()
									? DataResult.error(() -> "Spawned mobs cannot be empty", list)
									: DataResult.success(list))
							.fieldOf("spawnedMobs")
							.forGetter(GaiaFightConfiguration::spawnedMobs),
					Codec.list(MobAttributeModifier.CODEC)
							.validate(MobAttributeModifier::validateList)
							.optionalFieldOf("attributeModifiers", List.of())
							.forGetter(GaiaFightConfiguration::attributeModifiers),
					Codec.list(MobEffectToApply.CODEC)
							.optionalFieldOf("effectsToApply", List.of())
							.forGetter(GaiaFightConfiguration::effectsToApply)
			).apply(instance, GaiaFightConfiguration::new)
	);

	public static final Identifier NORMAL = botaniaRL("normal");
	public static final Identifier HARD = botaniaRL("hard");

	public static GaiaFightConfiguration.Builder builder(ResourceKey<LootTable> playerLoot, int teleportDelay,
			IntProvider mineCount, float dyingHealthPercent, int dyingTeleportDelay, IntProvider dyingMineCount,
			IntProvider pixiesAfterHurtCount) {
		return new GaiaFightConfiguration.Builder(playerLoot.identifier(), teleportDelay, mineCount,
				dyingHealthPercent, dyingTeleportDelay, dyingMineCount, pixiesAfterHurtCount);
	}

	public ResourceKey<LootTable> getRewardLootTableKey() {
		return ResourceKey.create(Registries.LOOT_TABLE, this.rewardLootTable);
	}

	public static class Builder {
		private final Identifier playerLoot;
		private final int teleportDelay;
		private final IntProvider mineCount;
		private final float dyingHealthPercent;
		private final int dyingTeleportDelay;
		private final IntProvider dyingMineCount;
		private final IntProvider pixiesAfterHurtCount;
		private IntProvider pixiesAfterTeleportCount = ConstantInt.of(1);
		private int missileSpawnInterval;
		private IntProvider missileSpawnCount = ConstantInt.ZERO;
		IntProvider mobSpawnsPerPlayer = ConstantInt.of(3);
		@Nullable
		private WeightedList<MobSpawnData> spawnedMobs;
		@Nullable
		private List<MobAttributeModifier> attributeModifiers;
		@Nullable
		private List<MobEffectToApply> effectsToApply;

		private Builder(Identifier playerLoot, int teleportDelay, IntProvider mineCount,
				float dyingHealthPercent, int dyingTeleportDelay, IntProvider dyingMineCount,
				IntProvider pixiesAfterHurtCount) {
			this.playerLoot = playerLoot;
			this.teleportDelay = teleportDelay;
			this.mineCount = mineCount;
			this.dyingHealthPercent = dyingHealthPercent;
			this.dyingTeleportDelay = dyingTeleportDelay;
			this.dyingMineCount = dyingMineCount;
			this.pixiesAfterHurtCount = pixiesAfterHurtCount;
		}

		public Builder spawnedMobs(IntProvider mobSpawnsPerPlayer, MobSpawnData... spawnedMobs) {
			this.mobSpawnsPerPlayer = mobSpawnsPerPlayer;
			List<Weighted<MobSpawnData>> weighted = Arrays.stream(spawnedMobs)
					.map(mob -> new Weighted<>(mob, 1))
					.toList();
			this.spawnedMobs = WeightedList.of(weighted);
			return this;
		}

		public Builder attributeModifiers(MobAttributeModifier... attributeModifiers) {
			this.attributeModifiers = List.of(attributeModifiers);
			return this;
		}

		public Builder effectsToApply(MobEffectToApply... effectsToApply) {
			this.effectsToApply = List.of(effectsToApply);
			return this;
		}

		public Builder pixiesAfterTeleportCount(IntProvider pixiesAfterTeleportCount) {
			this.pixiesAfterTeleportCount = pixiesAfterTeleportCount;
			return this;
		}

		public Builder spawnMissiles(int interval, IntProvider count) {
			this.missileSpawnInterval = interval;
			this.missileSpawnCount = count;
			return this;
		}

		public GaiaFightConfiguration build() {
			return new GaiaFightConfiguration(playerLoot, teleportDelay, mineCount,
					dyingHealthPercent, dyingTeleportDelay, dyingMineCount,
					pixiesAfterHurtCount, pixiesAfterTeleportCount,
					missileSpawnInterval, missileSpawnCount,
					mobSpawnsPerPlayer, spawnedMobs == null ? WeightedList.of() : spawnedMobs,
					attributeModifiers == null ? List.of() : attributeModifiers,
					effectsToApply == null ? List.of() : effectsToApply);
		}
	}
}
