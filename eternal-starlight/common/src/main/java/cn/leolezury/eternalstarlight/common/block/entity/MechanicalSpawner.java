package cn.leolezury.eternalstarlight.common.block.entity;

import net.minecraft.world.entity.EntitySpawnReason;

import cn.leolezury.eternalstarlight.common.util.ESPortCompat;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.block.MechanicalSpawnerBlock;
import cn.leolezury.eternalstarlight.common.network.ParticlePacket;
import cn.leolezury.eternalstarlight.common.particle.ExplosionShockParticleOptions;
import cn.leolezury.eternalstarlight.common.platform.ESPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public abstract class MechanicalSpawner {
	public static final String TAG_SPAWN_DATA = "spawn_data";
	public static final String TAG_SPAWN_POTENTIALS = "spawn_potentials";
	private static final String TAG_SPAWN_DELAY = "spawn_delay";
	private static final String TAG_MIN_SPAWN_DELAY = "min_spawn_delay";
	private static final String TAG_MAX_SPAWN_DELAY = "max_spawn_delay";
	private static final String TAG_SPAWN_COUNT = "spawn_count";
	private static final String TAG_MAX_NEARBY_ENTITIES = "max_nearby_entities";
	private static final String TAG_REQUIRED_PLAYER_RANGE = "required_player_range";
	private static final String TAG_SPAWN_RANGE = "spawn_range";
	private static final int EVENT_SPAWN = 1;
	private int spawnDelay = 20;
	private WeightedList<SpawnData> spawnPotentials = WeightedList.of();
	@Nullable
	private SpawnData nextSpawnData;
	private double spin;
	private double oSpin;
	private float animationScale;
	private float oAnimationScale;
	private int minSpawnDelay = 20;
	private int maxSpawnDelay = 30;
	private int spawnCount = 1;
	@Nullable
	private Entity displayEntity;
	private int maxNearbyEntities = 6;
	private int requiredPlayerRange = 16;
	private int spawnRange = 4;

	public void setEntityId(EntityType<?> type, @Nullable Level level, RandomSource random, BlockPos pos) {
		this.getOrCreateNextSpawnData(level, random, pos)
			.getEntityToSpawn()
			.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
	}

	private boolean isNearPlayer(Level level, BlockPos pos, int range) {
		return !level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(range)).stream().filter(player -> (level.isClientSide() || inLineOfSight(level, pos.getCenter(), player.getEyePosition())) && !player.isCreative() && !player.isSpectator()).toList().isEmpty();
	}

	private static boolean inLineOfSight(Level level, Vec3 block, Vec3 target) {
		BlockHitResult blockHitResult = level.clip(new ClipContext(target, block, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
		return blockHitResult.getBlockPos().equals(BlockPos.containing(block)) || blockHitResult.getType() == HitResult.Type.MISS;
	}

	public void clientTick(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		this.oSpin = this.spin;
		this.oAnimationScale = this.animationScale;
		if (this.isNearPlayer(level, pos, this.requiredPlayerRange) && state.getValue(MechanicalSpawnerBlock.POWER) > 0) {
			RandomSource random = level.getRandom();
			double x = pos.getX() + random.nextDouble();
			double y = pos.getY() + random.nextDouble() * 2 - 1;
			double z = pos.getZ() + random.nextDouble();
			level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
			level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
			if (this.spawnDelay > 0) {
				this.spawnDelay--;
			}

			this.spin = (this.spin + (1000.0 / (this.spawnDelay + 200.0))) % 360.0;
			this.animationScale = Mth.clamp(this.animationScale + 0.5F, 0, 1);
		} else {
			this.animationScale = Mth.clamp(this.animationScale - 0.2F, 0, 1);
		}
	}

	public void serverTick(ServerLevel serverLevel, BlockPos pos) {
		BlockState state = serverLevel.getBlockState(pos);
		RandomSource random = serverLevel.getRandom();
		if (this.isNearPlayer(serverLevel, pos, this.requiredPlayerRange)) {
			if (this.spawnDelay == -1) {
				this.delay(serverLevel, pos);
			}

			if (this.spawnDelay > 0) {
				if (state.getValue(MechanicalSpawnerBlock.POWER) > 0) {
					this.spawnDelay--;
				}
			} else if (ESPortCompat.gameRule(serverLevel, GameRules.SPAWN_MOBS)) {
				boolean success = false;
				SpawnData spawnData = this.getOrCreateNextSpawnData(serverLevel, random, pos);

				for (int i = 0; i < this.spawnCount; i++) {
					CompoundTag entityToSpawn = spawnData.getEntityToSpawn();
					ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, serverLevel.registryAccess(), entityToSpawn);
					Optional<EntityType<?>> type = EntityType.by(input);
					if (type.isEmpty()) {
						this.delay(serverLevel, pos);
						return;
					}

					Vec3 spawnVec = input.read("Pos", Vec3.CODEC).orElseGet(() -> new Vec3(
						pos.getX() + (random.nextDouble() - random.nextDouble()) * this.spawnRange + 0.5,
						pos.getY() + random.nextInt(3) - 1,
						pos.getZ() + (random.nextDouble() - random.nextDouble()) * this.spawnRange + 0.5));
					double x = spawnVec.x;
					double y = spawnVec.y;
					double z = spawnVec.z;
					if (serverLevel.noCollision(type.get().getSpawnAABB(x, y, z)) && inLineOfSight(serverLevel, pos.getCenter(), new Vec3(x, y, z))) {
						BlockPos spawnPos = BlockPos.containing(x, y, z);
						if (spawnData.getCustomSpawnRules().isPresent()) {
							if (!type.get().getCategory().isFriendly() && serverLevel.getDifficulty() == Difficulty.PEACEFUL) {
								continue;
							}

							SpawnData.CustomSpawnRules rules = spawnData.getCustomSpawnRules().get();
							if (!rules.isValidPosition(spawnPos, serverLevel)) {
								continue;
							}
						} else if (!SpawnPlacements.checkSpawnRules(type.get(), serverLevel, EntitySpawnReason.SPAWNER, spawnPos, serverLevel.getRandom())) {
							continue;
						}

						Entity entity = EntityType.loadEntityRecursive(input, serverLevel, EntitySpawnReason.SPAWNER, e -> {
							e.snapTo(x, y, z, e.getYRot(), e.getXRot());
							return e;
						});
						if (entity == null) {
							this.delay(serverLevel, pos);
							return;
						}

						int entitiesCount = serverLevel.getEntities(EntityTypeTest.forExactClass(entity.getClass()), new AABB(pos.getX(), pos.getY(), pos.getZ(), (pos.getX() + 1), (pos.getY() + 1), (pos.getZ() + 1)).inflate(this.spawnRange), EntitySelector.NO_SPECTATORS).size();
						if (entitiesCount >= this.maxNearbyEntities) {
							this.delay(serverLevel, pos);
							return;
						}

						entity.snapTo(entity.getX(), entity.getY(), entity.getZ(), random.nextFloat() * 360.0F, 0.0F);
						if (entity instanceof Mob mob) {
							if (!mob.checkSpawnObstruction(serverLevel)) {
								continue;
							}

							boolean shouldFinalize = spawnData.getEntityToSpawn().size() == 1 && spawnData.getEntityToSpawn().getString("id").isPresent();
							if (shouldFinalize) {
								mob.finalizeSpawn(serverLevel, ESPortCompat.difficulty(serverLevel, entity.blockPosition()), EntitySpawnReason.SPAWNER, null);
							}

							spawnData.getEquipment().ifPresent(mob::equip);
						}

						if (!serverLevel.tryAddFreshEntityWithPassengers(entity)) {
							this.delay(serverLevel, pos);
							return;
						}

						serverLevel.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, pos, 0);
						serverLevel.gameEvent(entity, GameEvent.ENTITY_PLACE, spawnPos);
						if (entity instanceof Mob mob) {
							mob.spawnAnim();
						}
						for (int j = 0; j <= 10; j++) {
							ESPlatform.INSTANCE.sendToAllClients(serverLevel, new ParticlePacket(ExplosionShockParticleOptions.ENERGY, entity.getX() + (random.nextFloat() - 0.5f) * entity.getBbWidth() * 1.2f, entity.getY(), entity.getZ() + (random.nextFloat() - 0.5f) * entity.getBbWidth() * 1.2f, 0, 1, 0));
						}

						success = true;
					}
				}

				if (success) {
					this.delay(serverLevel, pos);
				}
			}
		}
	}

	private void delay(Level level, BlockPos pos) {
		RandomSource random = level.getRandom();
		if (this.maxSpawnDelay <= this.minSpawnDelay) {
			this.spawnDelay = this.minSpawnDelay;
		} else {
			this.spawnDelay = this.minSpawnDelay + random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
		}
		BlockState state = level.getBlockState(pos);
		this.spawnDelay *= state.hasProperty(MechanicalSpawnerBlock.POWER) ? (16 - state.getValue(MechanicalSpawnerBlock.POWER)) : 1;
		this.spawnPotentials.getRandom(random).ifPresent(entry -> this.setNextSpawnData(level, pos, entry));
		this.broadcastEvent(level, pos, EVENT_SPAWN);
	}

	public void load(@Nullable Level level, BlockPos pos, ValueInput tag) {
		this.spawnDelay = tag.getShortOr(TAG_SPAWN_DELAY, (short) 0);
		tag.read(TAG_SPAWN_DATA, SpawnData.CODEC).ifPresent(data -> this.setNextSpawnData(level, pos, data));
		this.spawnPotentials = tag.read(TAG_SPAWN_POTENTIALS, SpawnData.LIST_CODEC)
			.orElseGet(() -> WeightedList.of(this.nextSpawnData != null ? this.nextSpawnData : new SpawnData()));
		this.minSpawnDelay = tag.getShortOr(TAG_MIN_SPAWN_DELAY, (short) this.minSpawnDelay);
		this.maxSpawnDelay = tag.getShortOr(TAG_MAX_SPAWN_DELAY, (short) this.maxSpawnDelay);
		this.spawnCount = tag.getShortOr(TAG_SPAWN_COUNT, (short) this.spawnCount);
		this.maxNearbyEntities = tag.getShortOr(TAG_MAX_NEARBY_ENTITIES, (short) this.maxNearbyEntities);
		this.requiredPlayerRange = tag.getShortOr(TAG_REQUIRED_PLAYER_RANGE, (short) this.requiredPlayerRange);
		this.spawnRange = tag.getShortOr(TAG_SPAWN_RANGE, (short) this.spawnRange);
		this.displayEntity = null;
	}

	public void save(ValueOutput tag) {
		tag.putShort(TAG_SPAWN_DELAY, (short) this.spawnDelay);
		tag.putShort(TAG_MIN_SPAWN_DELAY, (short) this.minSpawnDelay);
		tag.putShort(TAG_MAX_SPAWN_DELAY, (short) this.maxSpawnDelay);
		tag.putShort(TAG_SPAWN_COUNT, (short) this.spawnCount);
		tag.putShort(TAG_MAX_NEARBY_ENTITIES, (short) this.maxNearbyEntities);
		tag.putShort(TAG_REQUIRED_PLAYER_RANGE, (short) this.requiredPlayerRange);
		tag.putShort(TAG_SPAWN_RANGE, (short) this.spawnRange);
		tag.storeNullable(TAG_SPAWN_DATA, SpawnData.CODEC, this.nextSpawnData);
		tag.store(TAG_SPAWN_POTENTIALS, SpawnData.LIST_CODEC, this.spawnPotentials);
	}

	@Nullable
	public Entity getOrCreateDisplayEntity(Level level, BlockPos pos) {
		if (this.displayEntity == null) {
			CompoundTag tag = this.getOrCreateNextSpawnData(level, level.getRandom(), pos).getEntityToSpawn();
			if (tag.getString("id").isEmpty()) {
				return null;
			}
			this.displayEntity = EntityType.loadEntityRecursive(tag, level, EntitySpawnReason.SPAWNER, EntityProcessor.NOP);
		}
		return this.displayEntity;
	}

	public boolean onEventTriggered(Level level, BlockState state, int id) {
		if (id == EVENT_SPAWN) {
			if (level.isClientSide()) {
				this.spawnDelay = this.minSpawnDelay * (state.hasProperty(MechanicalSpawnerBlock.POWER) ? (16 - state.getValue(MechanicalSpawnerBlock.POWER)) : 1);
			}
			return true;
		} else {
			return false;
		}
	}

	protected void setNextSpawnData(@Nullable Level level, BlockPos pos, SpawnData nextSpawnData) {
		this.nextSpawnData = nextSpawnData;
	}

	private SpawnData getOrCreateNextSpawnData(@Nullable Level level, RandomSource random, BlockPos pos) {
		if (this.nextSpawnData == null) {
			this.setNextSpawnData(level, pos, this.spawnPotentials.getRandom(random).orElseGet(SpawnData::new));
		}
		return this.nextSpawnData;
	}

	public abstract void broadcastEvent(Level level, BlockPos pos, int eventId);

	public double getSpin() {
		return this.spin;
	}

	public double getOSpin() {
		return this.oSpin;
	}

	public float getAnimationScale() {
		return this.animationScale;
	}

	public float getOAnimationScale() {
		return this.oAnimationScale;
	}
}
