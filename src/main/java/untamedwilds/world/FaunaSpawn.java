package untamedwilds.world;

import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import untamedwilds.UntamedWilds;
import untamedwilds.config.ConfigMobControl;
import untamedwilds.entity.ComplexMob;
import untamedwilds.entity.INeedsPostUpdate;
import untamedwilds.entity.ISpecies;
import untamedwilds.util.EntityUtils;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;

public class FaunaSpawn {
    // Map our custom spawn types to Minecraft's SpawnPlacementType
    public static SpawnPlacementType getSpawnPlacementType(SpawnType type) {
        return switch (type) {
            case IN_WATER -> SpawnPlacementTypes.IN_WATER;
            case IN_LAVA -> SpawnPlacementTypes.IN_LAVA;
            case ON_GROUND -> SpawnPlacementTypes.ON_GROUND;
            case NO_RESTRICTIONS -> SpawnPlacementTypes.NO_RESTRICTIONS;
        };
    }
    
    public enum SpawnType {
        NO_RESTRICTIONS,
        IN_WATER,
        IN_LAVA,
        ON_GROUND
    }
    
    private static boolean isSpawnableSpace(BlockGetter worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn, EntityType<?> entityTypeIn) {
        if (state.isCollisionShapeFullBlock(worldIn, pos)) {
            return false;
        } else if (state.isSignalSource()) {
            return false;
        } else if (!fluidStateIn.isEmpty()) {
            return false;
        } else if (state.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE)) {
            return false;
        } else {
            return !entityTypeIn.isBlockDangerous(state);
        }
    }

    // Convert SpawnPlacementType to our internal SpawnType
    private static SpawnType toInternalSpawnType(SpawnPlacementType type) {
        if (type == SpawnPlacementTypes.IN_WATER) return SpawnType.IN_WATER;
        if (type == SpawnPlacementTypes.IN_LAVA) return SpawnType.IN_LAVA;
        if (type == SpawnPlacementTypes.ON_GROUND) return SpawnType.ON_GROUND;
        return SpawnType.NO_RESTRICTIONS;
    }
    
    private static boolean canCreatureTypeSpawnAtLocation(SpawnPlacementType placeType, LevelReader worldIn, BlockPos pos, @Nullable EntityType<?> entityTypeIn) {
        return canCreatureTypeSpawnAtLocation(toInternalSpawnType(placeType), worldIn, pos, entityTypeIn);
    }
    
    private static boolean canCreatureTypeSpawnAtLocation(SpawnType placeType, LevelReader worldIn, BlockPos pos, @Nullable EntityType<?> entityTypeIn) {
        if (placeType == SpawnType.NO_RESTRICTIONS) {
            return true;
        } else if (entityTypeIn != null && worldIn.getWorldBorder().isWithinBounds(pos)) {
            return canSpawnAtBody(placeType, worldIn, pos, entityTypeIn);
        }
        return false;
    }
    private static boolean canSpawnAtBody(SpawnType placeType, LevelReader worldIn, BlockPos pos, @Nullable EntityType<?> entityTypeIn) {
        BlockState blockstate = worldIn.getBlockState(pos);
        FluidState ifluidstate = worldIn.getFluidState(pos);
        //BlockPos blockpos = pos.up();
        BlockPos blockpos1 = pos.below();
        switch(placeType) {
            case IN_WATER:
                return ifluidstate.is(FluidTags.WATER) /*&& worldIn.getFluidState(blockpos1).isTagged(FluidTags.WATER) && !worldIn.getBlockState(blockpos).isNormalCube(worldIn, blockpos)*/;
            case IN_LAVA:
                return ifluidstate.is(FluidTags.LAVA);
            case ON_GROUND:
            default:
                BlockState blockstate1 = worldIn.getBlockState(blockpos1);
                if (!blockstate1.isValidSpawn(worldIn, blockpos1, entityTypeIn)) {
                    return false;
                } else {
                    return isSpawnableSpace(worldIn, pos, blockstate, ifluidstate, entityTypeIn); /* && isSpawnableSpace(worldIn, blockpos, worldIn.getBlockState(blockpos), worldIn.getFluidState(blockpos)*/
                }
        }
    }

    //public static boolean performWorldGenSpawning(EntityType<?> entityType, EntitySpawnPlacementRegistry.PlacementType spawnType, ISeedReader worldIn, BlockPos pos, Random rand, int groupSize) {
    //    return performWorldGenSpawning(entityType, spawnType, Heightmap.Type.WORLD_SURFACE, worldIn, pos, rand, groupSize);
    //}

    public static boolean performWorldGenSpawning(EntityType<?> entityType, SpawnType spawnType, @Nullable Heightmap.Types heightMap, ServerLevelAccessor worldIn, BlockPos pos, RandomSource random, int groupSize) {
        return performWorldGenSpawning(entityType, getSpawnPlacementType(spawnType), heightMap, worldIn, pos, random, groupSize);
    }

    public static boolean performWorldGenSpawning(EntityType<?> entityType, SpawnPlacementType spawnType, @Nullable Heightmap.Types heightMap, ServerLevelAccessor worldIn, BlockPos pos, RandomSource random, int groupSize) {
        //UntamedWilds.LOGGER.info(entityType);
        if (ConfigMobControl.dimensionBlacklist.get().contains(worldIn.getLevel().dimension().identifier().toString()))
            return false;

        if (entityType != null && !worldIn.isClientSide()) {
            int i = random.nextInt(16);
            int j = random.nextInt(16);

            if (heightMap != null) {
                pos = pos.offset(i, 0, j);
                pos = worldIn.getHeightmapPos(heightMap, pos);
                if (spawnType == SpawnPlacementTypes.IN_WATER || spawnType == SpawnPlacementTypes.IN_LAVA) {
                    // Heightmaps return the floor. Aquatic spawns need the fluid block above it.
                    pos = pos.above();
                }
                //worldIn.setBlockState(pos, Blocks.TORCH.defaultBlockState(), 2);
            }

            if (random.nextFloat() < UntamedWildsGenerator.getBioDiversityLevel(Objects.requireNonNull(worldIn.getBiome(pos).unwrapKey().get().identifier()))) {
                int k = 1; // This variable will be changed after the mob spawns
                int species = -1;

                for(int packSize = 0; packSize < k; ++packSize) {
                    int x = pos.getX();
                    int y = pos.getY();
                    int z = pos.getZ();
                    if (packSize != 0) {
                        // Do not offset the first entity of the pack
                        x += random.nextInt(6);
                        z += random.nextInt(6);
                    }

                    for(int attempt = 0; attempt < 4; ++attempt) { // 4 attempts at spawning are made for each mob
                        if (attempt != 0) {
                            if (attempt == 1 && spawnType != SpawnPlacementTypes.IN_WATER && spawnType != SpawnPlacementTypes.IN_LAVA) {
                                y += ConfigMobControl.treeSpawnBias.get();
                            }
                            x += random.nextInt(2);
                            z += random.nextInt(2);
                        }
                        BlockPos blockpos;
                        if (heightMap != null) {
                            BlockPos candidate = worldIn.getHeightmapPos(heightMap, new BlockPos(x, 0, z));
                            if (spawnType == SpawnPlacementTypes.IN_WATER || spawnType == SpawnPlacementTypes.IN_LAVA) {
                                candidate = candidate.above();
                            }
                            blockpos = candidate;
                        } else {
                            blockpos = new BlockPos(x, y, z);
                        }
                        //BlockPos blockpos = getTopSolidOrLiquidBlock(worldIn, entityType, x, z);

                        if (entityType.canSummon() && canCreatureTypeSpawnAtLocation(spawnType, worldIn, blockpos, entityType)) {
                            float f = entityType.getWidth();
                            double d0 = Mth.clamp(x, (double)blockpos.getX() + (double)f, (double)blockpos.getX() + 16.0D - (double)f);
                            double d1 = Mth.clamp(z, (double)blockpos.getZ() + (double)f, (double)blockpos.getZ() + 16.0D - (double)f);
                            if (!worldIn.noCollision(untamedwilds.util.EntityUtils.getEntityTypeAABB(entityType, d0, blockpos.getY(), d1))) {
                                continue;
                            }
                            Entity entity;
                            try {
                                entity = entityType.create(worldIn.getLevel(), net.minecraft.world.entity.EntitySpawnReason.NATURAL);
                            } catch (Exception exception) {
                                UntamedWilds.LOGGER.warn("Failed to create mob", exception);
                                continue;
                            }

                            assert entity != null;
                            entity.snapTo(blockpos.getX(), blockpos.getY(), blockpos.getZ(), random.nextFloat() * 360.0F, 0.0F);
                            if (entity instanceof Mob mobEntity) {
                                // NeoForge 1.21.1: ForgeHooks.canEntitySpawn removed, just use checkSpawnRules
                                if (mobEntity.checkSpawnRules(worldIn, EntitySpawnReason.CHUNK_GENERATION) && worldIn.noCollision(entity)) {
                                    mobEntity.finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(mobEntity.blockPosition()), EntitySpawnReason.CHUNK_GENERATION, null);
                                    if (mobEntity instanceof ComplexMob && mobEntity.isAlive()) {
                                        if (mobEntity instanceof ISpecies) {
                                            if (species == -1) {
                                                species = ((ComplexMob)mobEntity).getVariant();
                                                if (species != 99)
                                                    k = EntityUtils.getPackSize(entityType, species);
                                            } else {
                                                ((ComplexMob)mobEntity).setVariant(species);
                                                ((ComplexMob)mobEntity).chooseSkinForSpecies((ComplexMob)mobEntity, false);
                                                if (mobEntity instanceof INeedsPostUpdate) {
                                                    ((INeedsPostUpdate)mobEntity).updateAttributes();
                                                }
                                            } // Wrong spawning messages are most likely due to their inclusion on onMobSpawning, not here
                                        }
                                        // Two possible fail-states, entity being assigned variant 99 (no valid variant found) or the Entity Data does not exist
                                        if (((ComplexMob)mobEntity).getVariant() == 99 || !ComplexMob.ENTITY_DATA_HASH.containsKey(entityType)) {
                                            mobEntity.remove(Entity.RemovalReason.DISCARDED);
                                            return false;
                                        }
                                    }

                                    if (UntamedWilds.DEBUG && mobEntity instanceof ComplexMob && mobEntity.isAlive()) {
                                        if (mobEntity instanceof ISpecies)
                                            UntamedWilds.LOGGER.info("Spawned: " + ((ComplexMob)mobEntity).getGenderString() + " " + ((ISpecies)mobEntity).getSpeciesName());
                                        else
                                            UntamedWilds.LOGGER.info("Spawned: " + ((ComplexMob)mobEntity).getGenderString() + " " + mobEntity.getName().getString());
                                    }
                                    if (mobEntity.isAlive()) {
                                        worldIn.addFreshEntityWithPassengers(mobEntity);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /*private static BlockPos getTopSolidOrLiquidBlock(LevelReader worldIn, @Nullable EntityType<?> entity, int posX, int posZ) {
        BlockPos blockpos = new BlockPos(posX, worldIn.getBbHeight(EntitySpawnPlacementRegistry.func_209342_b(entity), posX, posZ), posZ);
        BlockPos blockpos1 = blockpos.below();
        return worldIn.getBlockState(blockpos1).allowsMovement(worldIn, blockpos1, PathType.WATER) ? blockpos1 : blockpos;
    }*/
}
