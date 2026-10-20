package untamedwilds.util;

import java.util.Collections;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import untamedwilds.UntamedWilds;
import untamedwilds.config.ConfigGamerules;
import untamedwilds.entity.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// A series of functions that are not meant to be limited to ComplexMobs
public abstract class EntityUtils {

    // Destroy the boat of the selected entity (if it exists)
    public static void destroyBoat(Level worldIn, LivingEntity entityIn) {
        if (entityIn.getVehicle() != null && entityIn.getVehicle() instanceof Boat boat) {
            if (worldIn instanceof ServerLevel serverLevel) {
                boat.destroy(serverLevel, boat.getPickResult().getItem());
            }
        }
    }

    // Spawn particles throughout the entity
    public static <T extends ParticleOptions> void spawnParticlesOnEntity(Level worldIn, LivingEntity entityIn, T particle, int count, int iter) {
        if (worldIn.isClientSide()) return;
        if (entityIn.isMultipartEntity()) {
            for (PartEntity<?> part : entityIn.getParts()) {
                for (int i = 0; i < iter; i++) {
                    ((ServerLevel) worldIn).sendParticles(particle, part.getX(), part.getY() + (double) part.getBbHeight() / 1.5D, part.getZ(), count, part.getBbWidth() / 4.0F, part.getBbHeight() / 4.0F, part.getBbWidth() / 4.0F, 0.05D);
                }
            }
        } else {
            for (int i = 0; i < iter; i++) {
                ((ServerLevel) worldIn).sendParticles(particle, entityIn.getX(), entityIn.getY() + (double) entityIn.getBbHeight() / 1.5D, entityIn.getZ(), count, entityIn.getBbWidth() / 4.0F, entityIn.getBbHeight() / 4.0F, entityIn.getBbWidth() / 4.0F, 0.05D);
            }
        }
    }

    public static int getPackSize(EntityType<?> type, int variant) {
        return ComplexMob.getEntityData(type).getGroupCount(variant);
    }

    /**
     * Method to calculate the Blockpos relative to a mob's facing direction, returns a BlockPos
     *
     * @param entityIn The entity whose facing direction will be used
     * @param xzOffset Offset in the X and Z axis
     * @param yOffset  Offset in the Y axis
     */
    public static BlockPos getRelativeBlockPos(Entity entityIn, float xzOffset, float yOffset) {
        return entityIn.blockPosition().offset((int)(Math.cos(Math.toRadians(entityIn.getYRot() + 90)) * xzOffset), (int)yOffset, (int)(Math.sin(Math.toRadians(entityIn.getYRot() + 90)) * xzOffset));
    }

    // Self-explanatory
    public static EntityType<?> getEntityTypeFromTag(CompoundTag nbt, @Nullable EntityType<?> alt) {
        if (nbt != null && nbt.contains("EntityTag")) {
            CompoundTag entityNBT = nbt.getCompoundOrEmpty("EntityTag");
            if (entityNBT.contains("id")) {
                return EntityType.byString(entityNBT.getStringOr("id", "")).orElse(alt);
            }
        }
        return alt;
    }

    // This function builds a full tooltip containing Custom Name, EntityType, Gender and Scientific name (if available)
    public static void buildTooltipData(ItemStack stack, List<Component> tooltip, EntityType<?> entity, String path) {
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (custom != null && custom.contains("EntityTag")) {
            CompoundTag compound = custom.getCompoundOrEmpty("EntityTag");
            //String component = "mobspawn.tooltip." + (compound.contains("Gender") ? (compound.getInt("Gender") == 0 ? "male" : "female") : "unknown");
            //tooltip.add(new TranslatableComponent(component).mergeStyle(TextFormatting.GRAY));
            String gender = compound.contains("Gender") ? net.minecraft.network.chat.Component.translatable("mobspawn.tooltip." + (compound.getIntOr("Gender", 0) == 0 ? "male" : "female")).getString() + " " : "";
            String type;
            if (path.isEmpty())
                type = net.minecraft.network.chat.Component.translatable(entity.getDescriptionId()).getString();
            else
                type = net.minecraft.network.chat.Component.translatable(entity.getDescriptionId() + "_" + path).getString();
                if (custom.getCompoundOrEmpty("EntityTag").contains("CustomName")) {
                String customName = custom.getCompoundOrEmpty("EntityTag").getStringOr("CustomName", "");
                // Entity uses ITextComponent.Serializer.getComponentFromJson(s) instead of substrings
                tooltip.add(Component.literal(customName.substring(9, customName.length() - 2) + " (" + gender + type + ")").withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.literal(gender + type).withStyle(ChatFormatting.GRAY));
            }
        }
        if (ConfigGamerules.scientificNames.get()) {
            String scipath = path.isEmpty() ? "" : "_" + path;
            net.minecraft.network.chat.MutableComponent tooltipText = net.minecraft.network.chat.Component.translatable(entity.getDescriptionId() + scipath + ".sciname");
            if (!tooltipText.getString().contains(".")) {
                tooltip.add(tooltipText.withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
            }
        }
    }

    public static void createMobFromItem(ServerLevel worldIn, ItemStack itemstack, EntityType<?> entity, @Nullable Integer species, BlockPos spawnPos, @Nullable Player player, boolean offset) {
        createMobFromItem(worldIn, itemstack, entity, species, spawnPos, player, offset, false);
    }

    // This function creates a new mob from the NBT Tag stored in an ItemStack. Uses default EntityType and Species data to allow creating new entities from zero
    public static void createMobFromItem(ServerLevel worldIn, ItemStack itemstack, EntityType<?> entity, @Nullable Integer species, BlockPos spawnPos, @Nullable Player player, boolean offset, boolean skipNBTCheck) {
        Entity spawn;
        CompoundTag custom = itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (custom != null) {
            //UntamedWilds.LOGGER.info(custom.toString());
            if (custom.contains("EntityTag") && !skipNBTCheck) {
                CompoundTag savedEntityTag = custom.getCompoundOrEmpty("EntityTag");
                if (savedEntityTag.contains("UUID")) {
                    java.util.UUID savedUUID = savedEntityTag.read("UUID", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
                    if (savedUUID != null && worldIn.getEntity(savedUUID) != null) {
                        savedEntityTag.store("UUID", net.minecraft.core.UUIDUtil.CODEC, Mth.createInsecureUUID(worldIn.getRandom()));
                    }
                }
                spawn = entity.spawn(worldIn, itemstack, player, spawnPos, EntitySpawnReason.BUCKET, true, offset);
                if (spawn != null && !itemstack.getHoverName().equals(itemstack.getItem().getName(itemstack))) {
                    spawn.setCustomName(itemstack.getHoverName());
                }
            } else {
                // If no NBT data is assigned to the entity (eg. Item taken from the Creative menu), create a new, random mob
                    // New EntityType.create overloads take a Consumer<Entity> instead of the player argument.
                    spawn = entity.create(worldIn, null, spawnPos, EntitySpawnReason.SPAWN_ITEM_USE, true, offset);
                if (spawn instanceof ComplexMob entitySpawn) {
                    EntityDataHolder data = ComplexMob.getEntityData(entitySpawn.getType());
                    if (data != null) {
                        int true_species = species != null ? species : entitySpawn.getRandom().nextInt(data.getSpeciesData().size());
                        entitySpawn.setVariant(true_species);
                        entitySpawn.chooseSkinForSpecies(entitySpawn, true);
                        entitySpawn.setRandomMobSize();
                        entitySpawn.setGender(entitySpawn.getRandom().nextInt(2));
                        if (spawn instanceof INeedsPostUpdate)
                            ((INeedsPostUpdate) spawn).updateAttributes();
                    }
                }
                if (spawn != null) {
                    if (!itemstack.getHoverName().equals(itemstack.getItem().getName(itemstack))) {
                        spawn.setCustomName(itemstack.getHoverName());
                    }
                    worldIn.addFreshEntityWithPassengers(spawn);
                }
            }
        }
    }

    // Helper for 1.21.1: PotionUtils was removed; use PotionContents component instead
    public static java.util.List<MobEffectInstance> getMobEffectsFromItem(ItemStack itemstack) {
        PotionContents potionContents = itemstack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        List<MobEffectInstance> effects = new ArrayList<>();
        potionContents.getAllEffects().forEach(effects::add);
        return effects;
    }

    // This function makes the entity drop some Eggs of the given item_name, and with random stacksize between 1 and number
    public static void dropEggs(ComplexMob entity, String item_name, int number) {
        if (ConfigGamerules.mobsLayEggs.get()) {
            CompoundTag baseTag = new CompoundTag();
            ItemStack item = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.tryParse(UntamedWilds.MOD_ID + ":" + item_name.toLowerCase())));
            baseTag.putInt("variant", entity.getVariant());
            baseTag.putInt("custom_model_data", entity.getVariant());
            item.set(DataComponents.CUSTOM_DATA, CustomData.of(baseTag));
            ItemEntity entityitem = entity.level() instanceof ServerLevel serverLevel ? entity.spawnAtLocation(serverLevel, item, 0.2F) : null;
            if (entityitem != null) {
                entityitem.getItem().setCount(1 + entity.getRandom().nextInt(number - 1));
            }
        }
    }

    // This function turns the entity into an item with item_name registry name, and removes the entity from the world
    public static void turnEntityIntoItem(LivingEntity entity, String item_name) {
        if (ConfigGamerules.easyMobCapturing.get() || ((Mob) entity).getTarget() == null) {
            ItemEntity entityitem = entity.level() instanceof ServerLevel serverLevel ? entity.spawnAtLocation(serverLevel, new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.tryParse(UntamedWilds.MOD_ID + ":" + item_name.toLowerCase()))), 0.2F) : null;
            RandomSource rand = entity.getRandom();
            if (entityitem != null) {
                entityitem.setDeltaMovement((rand.nextFloat() - rand.nextFloat()) * 0.1F, rand.nextFloat() * 0.05F, (rand.nextFloat() - rand.nextFloat()) * 0.1F);
                entityitem.getItem().set(DataComponents.CUSTOM_DATA, CustomData.of(writeEntityToNBT(entity, false, true)));
                if (entity.hasCustomName()) {
                    entityitem.getItem().set(DataComponents.CUSTOM_NAME, entity.getCustomName());
                }
                entity.discard();
            }
        }
    }

    // This function replaces a given ItemStack with a new item with item_name registry name, and removes the entity from the world
    public static void mutateEntityIntoItem(LivingEntity entity, Player player, InteractionHand hand, String item_name, ItemStack itemstack) {
        if (ConfigGamerules.easyMobCapturing.get() || ((Mob) entity).getTarget() == null) {
            entity.playSound(SoundEvents.BUCKET_FILL_FISH, 1.0F, 1.0F);
            itemstack.shrink(1);
            ItemStack newitem = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.tryParse(UntamedWilds.MOD_ID + ":" + item_name.toLowerCase())));
            newitem.set(DataComponents.CUSTOM_DATA, CustomData.of(writeEntityToNBT(entity, false, true)));
            if (entity.hasCustomName()) {
                newitem.set(DataComponents.CUSTOM_NAME, entity.getCustomName());
            }
            if (!entity.level().isClientSide()) {
                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, newitem);
            }
            if (itemstack.isEmpty()) {
                player.setItemInHand(hand, newitem);
            } else if (!player.getInventory().add(newitem)) {
                player.drop(newitem, false);
            }
            entity.discard();
        }
    }

    // This function pulls X items from the defined LootTable
    public static List<ItemStack> getItemFromLootTable(Identifier lootTableIn, Level worldIn) {
        LootParams.Builder lootcontext$builder = new LootParams.Builder((ServerLevel) worldIn);
        if (worldIn.getServer() != null) {
            ResourceKey<LootTable> tableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableIn);
            LootTable table = worldIn.getServer().reloadableRegistries().getLootTable(tableKey);
            return table.getRandomItems(lootcontext$builder.create(LootContextParamSets.EMPTY));
        }
        return new ArrayList<>();
    }

    public static CompoundTag writeEntityToNBT(LivingEntity entity) {
        return writeEntityToNBT(entity, false);
    }

    public static net.minecraft.world.phys.AABB getEntityTypeAABB(EntityType<?> type, double x, double y, double z) {
        float w = type.getWidth();
        float h = type.getHeight();
        return new net.minecraft.world.phys.AABB(x - w / 2.0F, y, z - w / 2.0F, x + w / 2.0F, y + h, z + w / 2.0F);
    }

    public static CompoundTag writeEntityToNBT(LivingEntity entity, boolean keepHomeData) {
        return writeEntityToNBT(entity, keepHomeData, false);
    }

    // This method writes this entity into a CompoundTag Tag
    public static CompoundTag writeEntityToNBT(LivingEntity entity, boolean keepHomeData, boolean attachModelData) {
        CompoundTag baseTag = new CompoundTag();
        CompoundTag entityTag;
        net.minecraft.world.level.storage.TagValueOutput valueOutput = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess());
        entity.saveAsPassenger(valueOutput);
        entityTag = valueOutput.buildResult();
        entityTag.remove("Pos"); // Remove the Position from the NBT data, as it would fuck things up later on
        entityTag.remove("Motion");
        if (entityTag.contains("BoundingBox")) {
            entityTag.remove("BoundingBox"); // Stripping this NBT data prevents RandomPatches from moving mobs back to their original position
        }
        if (entityTag.contains("Leash")) {
            entityTag.remove("Leash"); // Stripping this NBT data prevents Leash duplication from caging/catching Leashed mobs
        }
        if (entity instanceof ISpecies && !keepHomeData) {
            entityTag.remove("HomePosX");
            entityTag.remove("HomePosY");
            entityTag.remove("HomePosZ");
        }
        if (attachModelData && entity instanceof ComplexMob) {
            baseTag.putInt("CustomModelData", ((ComplexMob) entity).getVariant());
        }
        baseTag.put("EntityTag", entityTag); // Put the entity in the Tag
        return baseTag;
    }

    // Returns whether an entity has full HP or not (takes into account overloaded HP)
    public static boolean hasFullHealth(LivingEntity entityIn) {
        return entityIn.getHealth() >= entityIn.getMaxHealth();
    }

    // Pulls all resources with the given name from the provided Identifier
    public static Pair<Integer, Integer> buildSkinArrays(String name, String species, EntityDataHolder dataIn, int variant, HashMap<String, HashMap<Integer, ArrayList<Identifier>>> common_list, HashMap<String, HashMap<Integer, ArrayList<Identifier>>> rare_list) {
        return buildSkinArrays(name, species, dataIn.getSkins(variant), variant, common_list, rare_list);
    }

    // Pulls all resources with the given name from the provided Identifier
    public static Pair<Integer, Integer> buildSkinArrays(String name, String species, int skins, int variant, HashMap<String, HashMap<Integer, ArrayList<Identifier>>> common_list, HashMap<String, HashMap<Integer, ArrayList<Identifier>>> rare_list) {
        String path = "textures/entity/" + name + "/" + species;

        if (!common_list.containsKey(name)) {
            common_list.put(name, new HashMap<>());
        }
        if (!rare_list.containsKey(name)) {
            rare_list.put(name, new HashMap<>());
        }
        Pair<Integer, Integer> values = new Pair<>((skins / 10) - 1, (skins % 10) - 1);
        common_list.get(name).put(variant, new ArrayList<>());
        if (values.getFirst() >= 1) {
            for (int i = 0; i <= values.getFirst(); i++) {
                final String full_path = String.format(path + "_%d.png", i + 1);
                common_list.get(name).get(variant).add(Identifier.tryParse(UntamedWilds.MOD_ID + ":" + full_path));
            }
        } else {
            common_list.get(name).get(variant).add(Identifier.tryParse(UntamedWilds.MOD_ID + ":" + path + ".png"));
        }

        if (values.getSecond() >= 0) {
            rare_list.get(name).put(variant, new ArrayList<>());
            for (int i = 0; i <= values.getSecond(); i++) {
                final String full_path = String.format(path + "_%dr.png", i + 1);
                rare_list.get(name).get(variant).add(Identifier.tryParse(UntamedWilds.MOD_ID + ":" + full_path));
            }
        }
        //Pair<Integer, Integer> result = new Pair<>(populateSkinArray(path, "_%d.png", variant, common_list.get(name), true), populateSkinArray(path, "_%dr.png", variant, rare_list.get(name), false));
        if (UntamedWilds.DEBUG) {
            UntamedWilds.LOGGER.info("Number of textures for " + species + " " + name + ": " + values);
        }
        return values;
    }

    // Populates the provided array with the data located in the specified path
    @Deprecated
    public static int populateSkinArray(String path, String suffix, int variant, HashMap<Integer, ArrayList<Identifier>> list, boolean addDefault) {
        list.put(variant, new ArrayList<>());
        for (int i = 0; i < 99; i++) {
            int k = i;
            try {
                if (!suffix.matches("[^a-z0-9/._:-]")) {
                    final String full_path = String.format(path + suffix, i + 1);
                    Minecraft.getInstance().getResourceManager().getResource(Identifier.tryParse(UntamedWilds.MOD_ID + ":" + full_path));
                    list.get(variant).add(Identifier.tryParse(UntamedWilds.MOD_ID + ":" + full_path));
                } else {
                    UntamedWilds.LOGGER.error("Invalid character in " + suffix + ", terminating Skin registry");
                    break;
                }
            } catch (Exception e) {
                if (k == 0 && addDefault) {
                    //UntamedWilds.LOGGER.info("Using the default path instead");
                    list.get(variant).add(Identifier.tryParse(UntamedWilds.MOD_ID + ":" + path + ".png"));
                    k++;
                }
                //UntamedWilds.LOGGER.info(k + " " + variant + " " + path + " " + list);
                if (list.get(variant).isEmpty()) {
                    list.remove(variant);
                }
                return k;
            }
        }
        return 0;
    }

    public static String getVariantName(EntityType<?> typeIn, int variantIn) {
        if (ComplexMob.ENTITY_DATA_HASH.containsKey(typeIn)) {
            return ComplexMob.ENTITY_DATA_HASH.get(typeIn).getName(variantIn);
        } else if (ComplexMob.CLIENT_DATA_HASH.containsKey(typeIn)) {
            return ComplexMob.CLIENT_DATA_HASH.get(typeIn).getSpeciesName(variantIn);
        }
        //UntamedWilds.LOGGER.warn("There's no name provided for the species");
        return "";
    }

    public static int getNumberOfSpecies(EntityType<?> typeIn) {
        if (ComplexMob.ENTITY_DATA_HASH.containsKey(typeIn)) {
            return ComplexMob.ENTITY_DATA_HASH.get(typeIn).getSpeciesData().size();
        } else if (ComplexMob.CLIENT_DATA_HASH.containsKey(typeIn)) {
            return ComplexMob.CLIENT_DATA_HASH.get(typeIn).getNumberOfSpecies();
        }
        UntamedWilds.LOGGER.warn("There's no species provided for the EntityType");
        return 0;
    }

    public static SoundEvent getSound(EntityType<?> typeIn, int variantIn, String sound_type) {
        return getSound(typeIn, variantIn, sound_type, null);
    }

    public static SoundEvent getSound(EntityType<?> typeIn, int variantIn, String sound_type, @Nullable SoundEvent fallback) {
        if (ComplexMob.ENTITY_DATA_HASH.containsKey(typeIn)) {
            SoundEvent location = ComplexMob.ENTITY_DATA_HASH.get(typeIn).getSounds(variantIn, sound_type);
            if (location != null) {
                return location;
            }
        }
        /*else if (ComplexMob.CLIENT_DATA_HASH.containsKey(typeIn)) {
            SoundEvent location = ComplexMob.CLIENT_DATA_HASH.get(typeIn).getSounds(variantIn, sound_type);
            if (location != null) {
                return BuiltInRegistries.SOUND_EVENT.getValue(location.name);
            }
        }*/
        //UntamedWilds.LOGGER.warn("There's no name provided for the species");
        return fallback;
    }

    public static int getClampedNumberOfSpecies(int i, EntityType<?> typeIn) {
        int size = Math.max(0, EntityUtils.getNumberOfSpecies(typeIn) - 1);
        if (i > size) {
            UntamedWilds.LOGGER.warn("Correcting wrong Variant value of " + i + " to " + size);
        }
        return Mth.clamp(i, 0, size);
    }

    // Takes the skin from the TEXTURES_COMMON or TEXTURES_RARE array
    public static Identifier getSkinFromEntity(ComplexMob entityIn) {
        if (entityIn.getType().builtInRegistryHolder().key().identifier() != null) {
            String name = entityIn.getType().builtInRegistryHolder().key().identifier().getPath();
            if (entityIn.getSkin() > 99 && ComplexMob.TEXTURES_RARE.get(name).containsKey(entityIn.getVariant())) {
                return ComplexMob.TEXTURES_RARE.get(name).get(entityIn.getVariant()).get(Math.min(entityIn.getSkin() - 100, ComplexMob.TEXTURES_RARE.get(name).get(entityIn.getVariant()).size() - 1));
            }
            if (entityIn.getVariant() >= 0)
                return ComplexMob.TEXTURES_COMMON.get(name).get(entityIn.getVariant()).get(Math.min(entityIn.getSkin(), ComplexMob.TEXTURES_COMMON.get(name).get(entityIn.getVariant()).size() - 1));
        }
        //UntamedWilds.LOGGER.warn("No Skin found for entity: " + entityIn.getType().getRegistryName());
        Identifier missing = Identifier.tryParse("minecraft:missing");
        return missing;
    }

    // Tests an ItemStack and consumes it if found to be a Food. Also applies it's effects
    public static void consumeItemStack(TamableAnimal entityIn, ItemStack itemstack) {
        if (itemstack.get(DataComponents.FOOD) != null) {
            FoodProperties itemFood = itemstack.get(DataComponents.FOOD);
            if (itemFood != null) {
                entityIn.playSound(SoundEvents.GENERIC_EAT.value(), 1, 1);
                if (entityIn instanceof ComplexMobTerrestrial)
                    ((ComplexMobTerrestrial) entityIn).addHunger(itemFood.nutrition() * 10);
                else
                    entityIn.heal(itemFood.nutrition());

                net.minecraft.world.item.component.Consumable consumable = itemstack.get(DataComponents.CONSUMABLE);
                if (consumable != null) {
                    for (net.minecraft.world.item.consume_effects.ConsumeEffect consumeEffect : consumable.onConsumeEffects()) {
                        if (consumeEffect instanceof net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect possible) {
                            for (MobEffectInstance inst : possible.effects()) {
                                if (entityIn.getRandom().nextFloat() < possible.probability()) {
                                    entityIn.addEffect(new MobEffectInstance(inst));
                                }
                            }
                        }
                    }
                }
            }
        } else if (!getMobEffectsFromItem(itemstack).isEmpty()) {
            entityIn.playSound(SoundEvents.GENERIC_DRINK.value(), 1, 1);
            if (entityIn instanceof ComplexMobTerrestrial)
                ((ComplexMobTerrestrial) entityIn).addHunger(10);

            for (MobEffectInstance effectinstance : getMobEffectsFromItem(itemstack)) {
                // In 1.21.1, getEffect() returns Holder<MobEffect>
                if (effectinstance.getEffect().value().isInstantenous() && entityIn.level() instanceof ServerLevel serverLevel)
                    effectinstance.getEffect().value().applyInstantenousEffect(serverLevel, entityIn.getOwner(), entityIn.getOwner(), entityIn, effectinstance.getAmplifier(), 1.0D);
                else
                    entityIn.addEffect(new MobEffectInstance(effectinstance));
            }
        }
    }

    public static Vec3 getOvershootPath(Entity entityIn, Entity targetIn, double overshoot) {
        double x = targetIn.getX() - entityIn.getX();
        double z = targetIn.getZ() - entityIn.getZ();
        float angle = (float) (Math.atan2(z, x));
        double dist = Mth.sqrt((float) (Math.pow(x, 2) + Math.pow(z, 2)));
        double add_x = Mth.cos(angle) * (dist + overshoot);
        double add_z = Mth.sin(angle) * (dist + overshoot);
        return new Vec3(entityIn.getX() + add_x, targetIn.getY(), entityIn.getZ() + add_z);
    }

    // Checks if a mob is NOT a valid partner for the input. Both entityIn and partnerIn should be the same class
    public static boolean isInvalidPartner(ComplexMob entityIn, ComplexMob partnerIn, boolean isHermaphrodite) {
        if (entityIn instanceof INestingMob nesting && (nesting.wantsToLayEggs() || ((INestingMob)partnerIn).wantsToLayEggs())) {
            return true;
        }
        return (ConfigGamerules.genderedBreeding.get() && (partnerIn.getGender() == entityIn.getGender() || isHermaphrodite)) || (partnerIn.getVariant() != entityIn.getVariant()) || partnerIn.getAge() != 0;
    }
}