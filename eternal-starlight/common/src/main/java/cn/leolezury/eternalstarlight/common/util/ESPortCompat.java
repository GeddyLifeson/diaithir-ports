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

	public static net.minecraft.world.item.Item.Properties armor(net.minecraft.world.item.Item.Properties properties, net.minecraft.world.item.equipment.ArmorMaterial material, net.minecraft.world.item.equipment.ArmorType type, net.minecraft.world.item.component.ItemAttributeModifiers extra) {
		net.minecraft.world.item.component.ItemAttributeModifiers.Builder builder = net.minecraft.world.item.component.ItemAttributeModifiers.builder();
		for (net.minecraft.world.item.component.ItemAttributeModifiers.Entry entry : material.createAttributes(type).modifiers()) {
			builder.add(entry.attribute(), entry.modifier(), entry.slot());
		}
		for (net.minecraft.world.item.component.ItemAttributeModifiers.Entry entry : extra.modifiers()) {
			builder.add(entry.attribute(), entry.modifier(), entry.slot());
		}
		return properties.humanoidArmor(material, type).attributes(builder.build());
	}

	public static java.util.List<net.minecraft.world.item.ItemStack> armorItems(net.minecraft.world.entity.LivingEntity entity) {
		return java.util.List.of(
			entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET),
			entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS),
			entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST),
			entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD));
	}

	public static net.minecraft.world.item.component.ItemAttributeModifiers baseAttributes(cn.leolezury.eternalstarlight.common.item.combat.ESItemTiers tier, float damage, float speed) {
		return net.minecraft.world.item.component.ItemAttributeModifiers.builder()
			.add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, new net.minecraft.world.entity.ai.attributes.AttributeModifier(net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID, damage + tier.getAttackDamageBonus(), net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
			.add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED, new net.minecraft.world.entity.ai.attributes.AttributeModifier(net.minecraft.world.item.Item.BASE_ATTACK_SPEED_ID, speed, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
			.build();
	}

	public static net.minecraft.core.particles.ItemParticleOption itemParticle(net.minecraft.core.particles.ParticleType<net.minecraft.core.particles.ItemParticleOption> type, net.minecraft.world.item.ItemStack stack) {
		return new net.minecraft.core.particles.ItemParticleOption(type, net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(stack));
	}

	public static net.minecraft.world.entity.EquipmentSlot slotForHand(net.minecraft.world.InteractionHand hand) {
		return hand == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND;
	}
}
