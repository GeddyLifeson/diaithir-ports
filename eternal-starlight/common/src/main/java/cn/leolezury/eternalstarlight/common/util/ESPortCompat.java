package cn.leolezury.eternalstarlight.common.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/** Small helpers that keep the 1.21.1-shaped call sites readable after the 26.1 API changes. */
public class ESPortCompat {
	public static boolean hurt(Entity entity, DamageSource source, float amount) {
		return entity.level() instanceof ServerLevel serverLevel && entity.hurtServer(serverLevel, source, amount);
	}

	public static boolean doHurtTarget(Mob mob, Entity target) {
		return mob.level() instanceof ServerLevel serverLevel && mob.doHurtTarget(serverLevel, target);
	}

	public static boolean has(ValueInput input, String key) {
		return input.read(key, Codec.PASSTHROUGH).isPresent();
	}

	public static boolean has(CompoundTag tag, String key) {
		return tag.contains(key);
	}

	public static UUID getUUID(ValueInput input, String key) {
		return input.read(key, UUIDUtil.CODEC).orElse(null);
	}

	public static void putUUID(ValueOutput output, String key, UUID uuid) {
		output.store(key, UUIDUtil.CODEC, uuid);
	}

	public static <T extends net.minecraft.world.entity.LivingEntity> java.util.List<T> getNearbyEntities(net.minecraft.world.level.Level level, Class<T> type, net.minecraft.world.entity.ai.targeting.TargetingConditions conditions, net.minecraft.world.entity.LivingEntity source, net.minecraft.world.phys.AABB box) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return java.util.List.of();
		}
		return level.getEntitiesOfClass(type, box, e -> conditions.test(serverLevel, source, e));
	}

	public static java.util.List<net.minecraft.world.entity.player.Player> getNearbyPlayers(net.minecraft.world.level.Level level, net.minecraft.world.entity.ai.targeting.TargetingConditions conditions, net.minecraft.world.entity.LivingEntity source, net.minecraft.world.phys.AABB box) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return java.util.List.of();
		}
		return level.getEntitiesOfClass(net.minecraft.world.entity.player.Player.class, box, e -> conditions.test(serverLevel, source, e));
	}

	public static net.minecraft.world.entity.item.ItemEntity spawnAtLocation(Entity entity, net.minecraft.world.item.ItemStack stack) {
		return entity.level() instanceof ServerLevel serverLevel ? entity.spawnAtLocation(serverLevel, stack) : null;
	}

	public static net.minecraft.world.entity.item.ItemEntity spawnAtLocation(Entity entity, net.minecraft.world.level.ItemLike item) {
		return entity.level() instanceof ServerLevel serverLevel ? entity.spawnAtLocation(serverLevel, item) : null;
	}

	public static net.minecraft.world.entity.item.ItemEntity spawnAtLocation(Entity entity, net.minecraft.world.item.ItemStack stack, float yOffset) {
		return entity.level() instanceof ServerLevel serverLevel ? entity.spawnAtLocation(serverLevel, stack, yOffset) : null;
	}

	public static net.minecraft.world.entity.item.ItemEntity spawnAtLocation(Entity entity, net.minecraft.world.level.ItemLike item, int count) {
		return spawnAtLocation(entity, new net.minecraft.world.item.ItemStack(item, count), 0F);
	}

	public static net.minecraft.world.DifficultyInstance difficulty(net.minecraft.world.level.LevelAccessor level, net.minecraft.core.BlockPos pos) {
		return level instanceof net.minecraft.world.level.ServerLevelAccessor accessor ? accessor.getCurrentDifficultyAt(pos) : new net.minecraft.world.DifficultyInstance(net.minecraft.world.Difficulty.NORMAL, 0L, 0L, 0F);
	}

	public static boolean gameRule(net.minecraft.world.level.Level level, net.minecraft.world.level.gamerules.GameRule<Boolean> rule) {
		return !(level instanceof ServerLevel serverLevel) || serverLevel.getGameRules().get(rule);
	}

	public static boolean gameRule(net.minecraft.world.level.LevelAccessor level, net.minecraft.world.level.gamerules.GameRule<Boolean> rule) {
		return !(level instanceof ServerLevel serverLevel) || serverLevel.getGameRules().get(rule);
	}

	public static net.minecraft.core.HolderSet<net.minecraft.world.item.Item> items(net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag) {
		return net.minecraft.core.registries.BuiltInRegistries.ITEM.getOrThrow(tag);
	}
}
