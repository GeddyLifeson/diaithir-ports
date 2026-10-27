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
}
