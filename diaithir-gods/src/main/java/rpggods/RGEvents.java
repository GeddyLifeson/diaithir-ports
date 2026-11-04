package rpggods;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.ImmutablePair;
import rpggods.deity.Cooldown;
import rpggods.util.Nbt;
import rpggods.deity.DeityHelper;
import rpggods.deity.Offering;
import rpggods.deity.Sacrifice;
import rpggods.entity.AffinityGoal;
import rpggods.entity.AltarEntity;
import rpggods.util.FavorChangedEvent;
import rpggods.favor.Favor;
import rpggods.favor.IFavor;
import rpggods.network.SUpdateSittingPacket;
import rpggods.perk.Perk;
import rpggods.perk.PerkAction;
import rpggods.perk.PerkCondition;
import rpggods.tameable.ITameable;
import rpggods.tameable.Tameable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class RGEvents {

    public static final int COMBAT_TIMER = 40;

    /**
     * Called when the player attempts to give an offering
     * @param entity the AltarEntity associated with this offering, if any
     * @param deity the deity ID
     * @param player the player
     * @param favor the player's favor
     * @param item the item being offered
     * @param silent true if the player should not receive any feedback
     * @return the ItemStack to replace the one provided, if any
     */
    public static Optional<ItemStack> onOffering(final Optional<AltarEntity> entity, final Identifier deity, final Player player, final IFavor favor, final ItemStack item, boolean silent) {
        boolean deityEnabled = favor.getFavor(deity).isEnabled();
        if(favor.isEnabled() && deityEnabled && !item.isEmpty()) {
            // find first matching offering for the given deity
            Identifier offeringId = null;
            Offering offering = null;
            for(Identifier id : RPGGods.DEITY_HELPER.get(deity).offeringMap.getOrDefault(BuiltInRegistries.ITEM.getKey(item.getItem()), ImmutableList.of())) {
                Offering o = RPGGods.OFFERING_MAP.get(id);
                if(o != null && o.matches(item)) {
                    offeringId = id;
                    offering = o;
                    break;
                }
            }
            // process the offering
            if(offering != null && offeringId != null) {
                // ensure offering can be accepted
                if(!favor.getOfferingCooldown(offeringId).canUse()) {
                    // send message to player informing them of maxed offering
                    if(!silent) {
                        Component message = Component.translatable("favor.offering.cooldown");
                        rpggods.util.Nbt.msg(player, message, true);
                    }
                    return Optional.empty();
                }
                // ensure player meets level requirement, if any
                int level = favor.getFavor(deity).getLevel();
                if(offering.hasLevelRange() && (level < offering.getTradeMinLevel() || level > offering.getTradeMaxLevel())) {
                    // Send message to player informing them of level requirements
                    if(!silent) {
                        Component message;
                        if(offering.hasMinLevel() && offering.hasMaxLevel()) {
                            message = Component.translatable("favor.offering.deny.level.multiple", offering.getTradeMinLevel(), offering.getTradeMaxLevel());
                        } else {
                            message = Component.translatable("favor.offering.deny.level.single", offering.getTradeMinLevel());
                        }
                        rpggods.util.Nbt.msg(player, message, true);
                    }
                    return Optional.empty();
                }
                // add favor and run function, if any
                favor.getFavor(deity).addFavor(player, deity, offering.getFavor(), FavorChangedEvent.Source.OFFERING);
                offering.getFunction().ifPresent(f -> runFunction(player.level(), player, f));
                // add cooldown
                favor.getOfferingCooldown(offeringId).addUse();
                // process trade, if any
                Optional<ItemStack> trade = offering.getTrade(item);
                if(trade.isPresent() && !trade.get().isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), trade.get().copy());
                    itemEntity.setNoPickUpDelay();
                    player.level().addFreshEntity(itemEntity);
                }
                // shrink item stack
                if(!player.isCreative()) {
                    item.shrink(offering.getAccept().getCount());
                }
                // particles
                if(entity.isPresent() && player.level() instanceof ServerLevel) {
                    Vec3 pos = Vec3.atBottomCenterOf(entity.get().blockPosition().above());
                    ParticleOptions particle = offering.getFavor() >= 0 ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.ANGRY_VILLAGER;
                    ((ServerLevel)player.level()).sendParticles(particle, pos.x, pos.y, pos.z, 8, 0.5D, 0.5D, 0.5D, 0);
                }
                // send player message
                if(!silent) {
                    favor.getFavor(deity).sendStatusMessage(player, deity);
                }
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    /**
     * Called when the player kills a living entity
     *
     * @param player the player
     * @param favor  the player's favor
     * @param entity the entity that was killed
     * @return true if the player's favor was modified
     */
    public static boolean onSacrifice(final Player player, final IFavor favor, final LivingEntity entity) {
        boolean success = false;
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        Optional<Identifier> optionalEntityId = Optional.of(entityId);
        Optional<CompoundTag> optionalEntityTag = Optional.of(Nbt.saveEntity(entity));
        if (favor.isEnabled()) {
            // find and process all matching sacrifices
            Identifier deityId;
            Sacrifice sacrifice;
            Cooldown cooldown;
            for (Map.Entry<Identifier, Sacrifice> entry : RPGGods.SACRIFICE_MAP.entrySet()) {
                if (entry.getValue() != null) {
                    sacrifice = entry.getValue();
                    // check sacrifice matches entity that was killed
                    if (entityId.equals(sacrifice.getEntity())) {
                        // check sacrifice cooldown
                        cooldown = favor.getSacrificeCooldown(entry.getKey());
                        deityId = Sacrifice.getDeity(entry.getKey());
                        boolean deityEnabled = favor.getFavor(deityId).isEnabled();
                        if (deityEnabled && cooldown.canUse()) {
                            // check sacrifice conditions
                            boolean matchConditions = true;
                            for (PerkCondition condition : sacrifice.getConditions()) {
                                if (!condition.match(deityId, player, favor, optionalEntityId, optionalEntityTag)) {
                                    matchConditions = false;
                                    break;
                                }
                            }
                            // attempt to process the sacrifice
                            if (sacrifice.getConditions().isEmpty() || matchConditions) {
                                success = true;
                                // add sacrifice cooldown
                                cooldown.addUse();
                                // add favor and run function, if any
                                favor.getFavor(deityId).addFavor(player, deityId, sacrifice.getFavor(), FavorChangedEvent.Source.SACRIFICE);
                                sacrifice.getFunction().ifPresent(f -> runFunction(player.level(), player, f));
                            }
                        }
                    }
                }
            }
        }
        return success;
    }

    /**
     * Loads all perks that match the given {@link PerkCondition.Type} and
     * attempts to run them.
     *
     * @param type   The Perk Condition Type to run
     * @param player The player
     * @param favor  The player's favor
     * @param entity An entity associated with this condition, if any
     * @param data   A Identifier associated with this condition, if any
     * @param object An event associated with this condition, if any
     * @return true if at least one perk ran successfully
     */
    public static boolean triggerCondition(final PerkCondition.Type type, final Player player, final IFavor favor,
                                           final Optional<Entity> entity, final Optional<Identifier> data,
                                           final Optional<? extends Event> object) {
        boolean success = false;
        if (favor.isEnabled()) {
            // find matching perks (use set to ensure no duplicates)
            Set<Identifier> perks = new HashSet<>();
            for (DeityHelper helper : RPGGods.DEITY_HELPER.values()) {
                boolean deityEnabled = favor.getFavor(helper.id).isEnabled();
                if (deityEnabled) {
                    perks.addAll(helper.perkByConditionMap.getOrDefault(type, ImmutableList.of()));
                }
            }
            // shuffle perks
            List<Identifier> perkList = Lists.newArrayList(perks);
            Collections.shuffle(perkList);
            // run each perk
            Perk perk;
            for (Identifier id : perkList) {
                perk = RPGGods.PERK_MAP.get(id);
                success |= runPerk(perk, player, favor, entity, data, object);
            }
        }
        return success;
    }

    /**
     * Loads all perks with the given {@link PerkAction.Type} and attempts to run each one.
     *
     * @param type   the action type (eg, function, item, potion, summon, arrow, xp)
     * @param player the player
     * @param favor  the player's favor
     * @param entity an entity to use when running the perk, if any
     * @return True if at least one perk ran successfully
     */
    public static boolean triggerPerks(final PerkAction.Type type, final Player player, final IFavor favor, final Optional<Entity> entity) {
        return triggerPerks(type, player, favor, entity, Optional.empty(), Optional.empty());
    }

    /**
     * Loads all perks with the given {@link PerkAction.Type} and attempts to run each one.
     *
     * @param type   the action type (eg, function, item, potion, summon, arrow, xp)
     * @param player the player
     * @param favor  the player's favor
     * @param entity an entity to use when running the perk, if any
     * @param data   a Identifier ID to use when running the perk, if any
     * @param object the Event to reference when running the perk, if any
     * @return True if at least one perk ran successfully
     */
    public static boolean triggerPerks(final PerkAction.Type type, final Player player, final IFavor favor,
                                       final Optional<Entity> entity, final Optional<Identifier> data,
                                       final Optional<? extends Event> object) {
        boolean success = false;
        if (favor.isEnabled()) {
            // find matching perks
            List<Identifier> perks = new ArrayList<>();
            for (DeityHelper helper : RPGGods.DEITY_HELPER.values()) {
                boolean deityEnabled = favor.getFavor(helper.id).isEnabled();
                if (deityEnabled) {
                    perks.addAll(helper.perkByTypeMap.getOrDefault(type, ImmutableList.of()));
                }
            }
            // shuffle perks
            Collections.shuffle(perks);
            // run each perk
            Perk perk;
            for (Identifier id : perks) {
                perk = RPGGods.PERK_MAP.get(id);
                success |= runPerk(perk, player, favor, entity, data, object);
            }
        }
        return success;
    }

    /**
     * Loads and runs a single function at the entity position
     *
     * @param worldIn    the world
     * @param entity     the entity (for example, a player)
     * @param functionId the function ID of a function to run
     * @return true if the function ran successfully
     */
    public static boolean runFunction(final Level worldIn, final LivingEntity entity, Identifier functionId) {
        final MinecraftServer server = worldIn.getServer();
        if (server != null) {
            final net.minecraft.server.ServerFunctionManager manager = server.getFunctions();
            final Optional<CommandFunction<CommandSourceStack>> function = manager.get(functionId);
            if (function.isPresent()) {
                final CommandSourceStack commandSource = manager.getGameLoopSender()
                        .withEntity(entity)
                        .withPosition(entity.position())
                        .withPermission(net.minecraft.server.permissions.LevelBasedPermissionSet.OWNER)
                        .withSuppressedOutput();
                manager.execute(function.get(), commandSource);
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to run a single perk and sets a cooldown if successful.
     * Checks favor range, cooldown, random chance, and conditions before running the perk.
     *
     * @param perk   the Perk to run
     * @param player the player to affect
     * @param favor  the player's favor
     * @return True if the perk was run and cooldown was added.
     * @see #runPerk(Perk, Player, IFavor, Optional, Optional, Optional)
     */
    public static boolean runPerk(final Perk perk, final Player player, final IFavor favor) {
        return runPerk(perk, player, favor, Optional.empty(), Optional.empty(), Optional.empty());
    }

    /**
     * Attempts to run a single perk and sets a cooldown if successful.
     * Checks favor range, cooldown, random chance, and conditions before running the perk.
     *
     * @param perk   the Perk to run
     * @param player the player to affect
     * @param favor  the player's favor
     * @param entity an entity to use when running the perk, if any
     * @param data   a Identifier ID to use when running the perk, if any
     * @param object the Event to reference when running the perk, if any
     * @return True if the perk was run and cooldown was added.
     */
    public static boolean runPerk(final Perk perk, final Player player, final IFavor favor, final Optional<Entity> entity,
                                  final Optional<Identifier> data, final Optional<? extends Event> object) {
        // check favor range, perk cooldown, and random chance
        if (perk != null && !player.level().isClientSide() && perk.getRange().isInRange(favor)
                && favor.getFavor(perk.getDeity()).isEnabled()
                && favor.hasNoPerkCooldown(perk.getCategory())
                && Math.random() < perk.getAdjustedChance(favor.getFavor(perk.getDeity()))) {
            // load nbt data
            Optional<CompoundTag> nbt = Optional.empty();
            if (entity.isPresent()) {
                nbt = Optional.ofNullable(Nbt.saveEntity(entity.get()));
            }
            // check perk conditions
            for (final PerkCondition condition : perk.getConditions()) {
                if (!condition.match(perk.getDeity(), player, favor, data, nbt)) {
                    return false;
                }
            }
            boolean success = false;
            for (final PerkAction action : perk.getActions()) {
                success |= action.run(perk.getDeity(), player, favor, entity, data, object);
            }
            if (success) {
                // send feedback
                sendPerkFeedback(perk.getDeity(), player, favor, perk.isPositive());
                // apply cooldown
                long cooldown = (long) Math.floor(perk.getCooldown() * (1.0D + Math.random() * 0.25D));
                favor.setPerkCooldown(perk.getCategory(), cooldown);
                return true;
            }
        }

        return false;
    }

    public static void sendPerkFeedback(Identifier deity, Player player, IFavor favor, boolean isPositive) {
        if (RPGGods.CONFIG.canGiveFeedback()) {
            final Component deityName = DeityHelper.getName(deity);
            final Component message;
            if (isPositive) {
                message = Component.translatable("favor.perk.feedback.positive", deityName).withStyle(ChatFormatting.GREEN);
            } else {
                message = Component.translatable("favor.perk.feedback.negative", deityName).withStyle(ChatFormatting.RED);
            }
            rpggods.util.Nbt.msg(player, message, !RPGGods.CONFIG.isFeedbackChat());
        }
    }

    /**
     * @param altar   the altar entity
     * @param deityId the Deity ID of the deity for this altar
     * @return true if a ritual was detected and patron was changed
     */
    public static boolean performRitual(final AltarEntity altar, final Identifier deityId) {
        Vec3i facing = net.minecraft.core.Direction.fromYRot(altar.getYRot()).getUnitVec3i();
        BlockPos pos = altar.blockPosition().offset(facing);
        AABB aabb = new AABB(pos).inflate(0.15D, 1.0D, 0.15D);
        List<ItemEntity> list = altar.level().getEntitiesOfClass(ItemEntity.class, aabb, e -> e.isOnFire());
        // detect first burning item in list
        if (list.isEmpty()) {
            return false;
        }
        ItemEntity item = list.get(0);
        // detect player who threw the item
        if (!(item.getOwner() instanceof Player player)) {
            return false;
        }
        // detect ritual perks for this deity
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item.getItem().getItem());
        if (null == itemId) {
            return false;
        }
        DeityHelper deity = RPGGods.DEITY_HELPER.computeIfAbsent(deityId, DeityHelper::new);
        List<Identifier> perkIds = deity.perkByConditionMap
                .getOrDefault(PerkCondition.Type.RITUAL, ImmutableList.of());
        // create list using perk IDs
        List<Perk> perks = new ArrayList<>();
        for (Identifier perkId : perkIds) {
            perks.add(RPGGods.PERK_MAP.getOrDefault(perkId, Perk.EMPTY));
        }
        // load favor
        boolean success = false;
        Optional<IFavor> ifavor = RPGGods.getFavor(player);
        if (ifavor.isPresent()) {
            IFavor favor = ifavor.get();
            // attempt to run the perks
            if (favor.isEnabled()) {
                for (Perk perk : perks) {
                    success |= runPerk(perk, player, favor, Optional.of(altar), Optional.of(itemId), Optional.empty());
                }
            }
            // send feedback
            if (success && favor.getPatron().isPresent()) {
                // summon visual lightning bolt
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(altar.level(), net.minecraft.world.entity.EntitySpawnReason.TRIGGERED);
                bolt.setVisualOnly(true);
                Vec3 position = Vec3.atBottomCenterOf(pos.below());
                bolt.setPos(position.x, position.y, position.z);
                altar.level().addFreshEntity(bolt);
                // send message
                Component message = Component.translatable("favor.perk.type.patron.description.add", DeityHelper.getName(favor.getPatron().get()))
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
                rpggods.util.Nbt.msg(player, message, true);
            }
        }

        return success;
    }

    public static class ModEvents {

        @SubscribeEvent
        public static void onAddEntityAttributes(final EntityAttributeModificationEvent event) {
            for (final EntityType<? extends LivingEntity> type : event.getTypes()) {
                if (!event.has(type, Attributes.ATTACK_DAMAGE)) {
                    event.add(type, Attributes.ATTACK_DAMAGE, 0);
                }
            }
        }
    }

    public static class ForgeEvents {

        @SubscribeEvent
        public static void onLivingDeath(final LivingDeathEvent event) {
            if (!event.isCanceled() && event.getEntity() != null && !event.getEntity().level().isClientSide() && event.getEntity().isEffectiveAi()) {
                if (event.getEntity() instanceof Player) {
                    final Player player = (Player) event.getEntity();
                    final Entity source = event.getSource().getEntity();
                    // onEntityKillPlayer
                    if (source instanceof LivingEntity && !player.isSpectator() && !player.isCreative()) {
                        RPGGods.getFavor(player).ifPresent(f -> {
                            triggerCondition(PerkCondition.Type.ENTITY_KILLED_PLAYER, player, f, Optional.of(source),
                                    Optional.ofNullable(BuiltInRegistries.ENTITY_TYPE.getKey(source.getType())),
                                    Optional.empty());
                        });
                    }
                } else if (event.getSource().getEntity() instanceof Player) {
                    final Player player = (Player) event.getSource().getEntity();
                    // onPlayerKillEntity
                    RPGGods.getFavor(player).ifPresent(f -> {
                        triggerCondition(PerkCondition.Type.PLAYER_KILLED_ENTITY, player, f, Optional.of(event.getEntity()),
                                Optional.ofNullable(BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType())),
                                Optional.empty());
                        onSacrifice(player, f, event.getEntity());
                    });
                }
                // onTameDeath
                Optional<ITameable> tameable = RPGGods.getTameable(event.getEntity());
                tameable.ifPresent(t -> {
                    Optional<LivingEntity> owner = t.getOwner(event.getEntity().level());
                    // send death message to owner
                    if (owner.isPresent() && owner.get() instanceof Player) {
                        Component message = event.getSource().getLocalizedDeathMessage(event.getEntity());
                        rpggods.util.Nbt.msg((Player) owner.get(), message, false);
                    }
                });
            }
        }

        @SubscribeEvent
        public static void onLivingHurt(final LivingDamageEvent.Pre event) {
            if (!event.getEntity().level().isClientSide() && event.getEntity().isEffectiveAi() && event.getEntity().isAlive()) {
                if (event.getSource().getDirectEntity() != null && event.getEntity() instanceof Player) {
                    Player player = (Player) event.getEntity();
                    Entity source = event.getSource().getDirectEntity();
                    if (!player.isSpectator() && !player.isCreative()) {
                        // onEntityHurtPlayer
                        RPGGods.getFavor(player).ifPresent(f -> {
                            triggerCondition(PerkCondition.Type.ENTITY_HURT_PLAYER, player, f, Optional.of(source),
                                    Optional.ofNullable(BuiltInRegistries.ENTITY_TYPE.getKey(source.getType())), Optional.of(event));
                        });
                    }
                } else if (event.getSource().getDirectEntity() instanceof Player) {
                    Player player = (Player) event.getSource().getDirectEntity();
                    LivingEntity target = event.getEntity();
                    // onPlayerHurtEntity
                    RPGGods.getFavor(player).ifPresent(f -> {
                        triggerCondition(PerkCondition.Type.PLAYER_HURT_ENTITY, player, f, Optional.of(target),
                                Optional.ofNullable(BuiltInRegistries.ENTITY_TYPE.getKey(target.getType())), Optional.of(event));
                        // onEnterCombat
                        if (player.getCombatTracker().getCombatDuration() < COMBAT_TIMER) {
                            triggerCondition(PerkCondition.Type.ENTER_COMBAT, player, f,
                                    Optional.of(target), Optional.of(BuiltInRegistries.ENTITY_TYPE.getKey(target.getType())),
                                    Optional.empty());
                        }
                    });
                }
            }
        }

        @SubscribeEvent
        public static void onEntityInteract(final PlayerInteractEvent.EntityInteract event) {
            if (!event.getEntity().level().isClientSide() && event.getHand() == InteractionHand.MAIN_HAND) {
                // onPlayerInteractEntity
                RPGGods.getFavor(event.getEntity()).ifPresent(f -> {
                    final Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(event.getTarget().getType());
                    if (triggerCondition(PerkCondition.Type.PLAYER_INTERACT_ENTITY, event.getEntity(), f, Optional.of(event.getTarget()), Optional.ofNullable(id), Optional.empty())) {
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        event.setCanceled(true);
                    }
                    if (event.getTarget() instanceof Merchant && triggerPerks(PerkAction.Type.SPECIAL_PRICE, event.getEntity(), f, Optional.of(event.getTarget()), Optional.ofNullable(id), Optional.of(event))) {
                        if (event.isCanceled()) {
                            event.setCancellationResult(InteractionResult.FAIL);
                        }
                    }
                });
                // toggle sitting for tamed mobs
                if (!event.isCanceled()) {
                    RPGGods.getTameable(event.getTarget()).ifPresent(t -> {
                        if (t.isOwner(event.getEntity())) {
                            t.setSittingWithUpdate(event.getTarget(), !t.isSitting());
                            // send packet to notify the interacting player of the new sitting state
                            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) {
                                PacketDistributor.sendToPlayer(sp, new SUpdateSittingPacket(event.getTarget().getId(), t.isSitting()));
                            }
                            event.setCancellationResult(InteractionResult.SUCCESS);
                            event.setCanceled(true);
                        }
                    });
                }
            }
        }

        @SubscribeEvent
        public static void onInteractBlock(final PlayerInteractEvent.RightClickBlock event) {
            if (!event.getEntity().level().isClientSide()) {
                BlockState state = event.getEntity().level().getBlockState(event.getHitVec().getBlockPos());
                Identifier blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                if (blockId != null) {
                    // onPlayerInteractBlock
                    RPGGods.getFavor(event.getEntity()).ifPresent(f -> {
                        triggerCondition(PerkCondition.Type.PLAYER_INTERACT_BLOCK, event.getEntity(), f, Optional.empty(),
                                Optional.of(blockId), Optional.empty());
                    });
                }
            }
        }

        @SubscribeEvent
        public static void onChangeFavor(FavorChangedEvent.Post event) {
            if (event.getPlayer() != null && !event.getPlayer().level().isClientSide() && event.isLevelChange()) {
                // onFavorChanged
                RPGGods.getFavor(event.getPlayer()).ifPresent(f -> {
                    triggerPerks(PerkAction.Type.UNLOCK, event.getPlayer(), f, Optional.empty());
                });
            }
        }

        private static final AttributeModifier MOB_ATTACK = new AttributeModifier(
                Identifier.fromNamespaceAndPath(RPGGods.MODID, "mob_attack"),
                1.0D, AttributeModifier.Operation.ADD_VALUE);

        @SubscribeEvent
        public static void onEntityJoinWorld(final EntityJoinLevelEvent event) {
            if (!event.getEntity().level().isClientSide() && (event.getEntity() instanceof Arrow || event.getEntity() instanceof SpectralArrow)) {
                final AbstractArrow arrow = (AbstractArrow) event.getEntity();
                final Entity thrower = arrow.getOwner();
                if (thrower instanceof Player) {
                    // onArrowDamage, onArrowEffect, onArrowCount
                    RPGGods.getFavor(thrower).ifPresent(f -> {
                        triggerPerks(PerkAction.Type.ARROW_DAMAGE, (Player) thrower, f, Optional.of(arrow));
                        triggerPerks(PerkAction.Type.ARROW_EFFECT, (Player) thrower, f, Optional.of(arrow));
                        triggerPerks(PerkAction.Type.ARROW_COUNT, (Player) thrower, f, Optional.of(arrow));
                    });
                }
            }
            if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof Mob) {
                Mob mob = (Mob) event.getEntity();
                boolean fleeEnabled = RPGGods.CONFIG.isFleeEnabled();
                boolean hostileEnabled = RPGGods.CONFIG.isHostileEnabled();
                boolean passiveEnabled = RPGGods.CONFIG.isPassiveEnabled();
                boolean tameableEnabled = RPGGods.CONFIG.isTameableEnabled();
                boolean checkAttackGoal = false;
                // add tameable goals
                if (tameableEnabled && RPGGods.getTameable(event.getEntity()).isPresent()) {
                    mob.goalSelector.addGoal(0, new AffinityGoal.AffinitySittingGoal(mob));
                    mob.goalSelector.addGoal(0, new AffinityGoal.AffinitySittingResetGoal(mob));
                    mob.goalSelector.addGoal(1, new AffinityGoal.AffinityFollowOwnerGoal(mob, 1.0D, 10.0F, 5.0F, false));
                    mob.goalSelector.addGoal(1, new AffinityGoal.AffinityOwnerHurtByTargetGoal(mob));
                    mob.goalSelector.addGoal(1, new AffinityGoal.AffinityOwnerHurtTargetGoal(mob));
                    checkAttackGoal = true;
                }
                // add flee goal
                if (fleeEnabled && event.getEntity() instanceof PathfinderMob) {
                    mob.goalSelector.addGoal(1, new AffinityGoal.AffinityFleeGoal((PathfinderMob) mob));
                }
                // add hostile goal
                if (hostileEnabled) {
                    mob.goalSelector.addGoal(4, new AffinityGoal.AffinityNearestAttackableGoal(mob, 0.1F));
                    checkAttackGoal = true;
                }
                // add target reset goal
                if (hostileEnabled || passiveEnabled) {
                    mob.goalSelector.addGoal(2, new AffinityGoal.AffinityNearestAttackableResetGoal(mob));
                }
                // ensure target has attack goal
                if (checkAttackGoal && event.getEntity() instanceof PathfinderMob
                        && !(event.getEntity() instanceof RangedAttackMob)
                        && !(event.getEntity() instanceof NeutralMob)) {
                    // check for existing attack goal
                    boolean hasAttackGoal = false;
                    for (Goal g : mob.goalSelector.getAvailableGoals().stream().filter(net.minecraft.world.entity.ai.goal.WrappedGoal::isRunning).map(net.minecraft.world.entity.ai.goal.WrappedGoal::getGoal).toList()) {
                        if (g instanceof MeleeAttackGoal) {
                            hasAttackGoal = true;
                            break;
                        }
                    }
                    // add attack goal if none was found
                    if (!hasAttackGoal) {
                        mob.goalSelector.addGoal(4, new AffinityGoal.AffinityMeleeAttackGoal((PathfinderMob) event.getEntity(), 1.2D, false));
                        // ensure mob has attack damage
                        AttributeInstance attack = mob.getAttribute(Attributes.ATTACK_DAMAGE);
                        if (attack != null && attack.getBaseValue() < 0.5D && !attack.hasModifier(MOB_ATTACK.id())) {
                            attack.addPermanentModifier(MOB_ATTACK);
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onAddPotion(final MobEffectEvent.Added event) {
            if (event.getEntity() instanceof Player && !event.getEntity().level().isClientSide()
                    && event.getEntity().isAlive() && event.getEffectInstance() != null) {
                Player player = (Player) event.getEntity();
                if (!player.isSpectator() && !player.isCreative()) {
                    // onEffectStart
                    RPGGods.getFavor(player).ifPresent(f -> {
                        triggerCondition(PerkCondition.Type.EFFECT_START, player, f, Optional.empty(),
                                Optional.ofNullable(BuiltInRegistries.MOB_EFFECT.getKey(event.getEffectInstance().getEffect().value())), Optional.empty());
                    });
                }
            }
        }

        @SubscribeEvent
        public static void onLivingTarget(final LivingChangeTargetEvent event) {
            if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof Mob
                    && event.getNewAboutToBeSetTarget() instanceof Player) {
                // Determine if entity is passive or hostile toward target
                ImmutablePair<Boolean, Boolean> passiveHostile = AffinityGoal.getPassiveAndHostile(event.getEntity(), event.getNewAboutToBeSetTarget());
                if (passiveHostile.getLeft()) {
                    event.setNewAboutToBeSetTarget(null);
                    return;
                }
            }
        }

        @SubscribeEvent
        public static void onBabySpawn(final BabyEntitySpawnEvent event) {
            if (!event.isCanceled() && event.getParentA().isEffectiveAi() && event.getCausedByPlayer() != null
                    && !event.getCausedByPlayer().isCreative() && !event.getCausedByPlayer().isSpectator()
                    && event.getParentA() instanceof Animal && event.getParentB() instanceof Animal) {
                RPGGods.getFavor(event.getCausedByPlayer()).ifPresent(f -> {
                    triggerPerks(PerkAction.Type.OFFSPRING, event.getCausedByPlayer(), f, Optional.of(event.getParentA()), Optional.empty(), Optional.of(event));
                });
            }
        }

        @SubscribeEvent
        public static void onPlayerPickupXp(final PlayerXpEvent.PickupXp event) {
            if (event.getEntity().isEffectiveAi() && !event.getEntity().level().isClientSide()) {
                RPGGods.getFavor(event.getEntity()).ifPresent(f -> {
                    triggerPerks(PerkAction.Type.XP, event.getEntity(), f, Optional.of(event.getOrb()));
                });
            }
        }

        @SubscribeEvent
        public static void onPlayerTick(final PlayerTickEvent.Post event) {
            if (!event.getEntity().level().isClientSide() && event.getEntity().isEffectiveAi()
                    && event.getEntity().isAlive() && canTickFavor(event.getEntity())) {
                RPGGods.getFavor(event.getEntity()).ifPresent(f -> {
                    // trigger perks
                    if (Math.random() < RPGGods.CONFIG.getRandomPerkChance()) {
                        // onRandomTick
                        triggerCondition(PerkCondition.Type.RANDOM_TICK, event.getEntity(), f, Optional.empty(),
                                Optional.empty(), Optional.empty());
                    }
                    // player tick
                    if(RPGGods.CONFIG.usePlayerFavor()) {
                        // reduce cooldown
                        f.tickCooldown(event.getEntity().level().getGameTime());
                        // deplete favor
                        if (Math.random() < RPGGods.CONFIG.getFavorDecayRate()) {
                            f.depleteFavor(event.getEntity());
                        }
                    }
                });
            }
        }

        /**
         * Used to tick global and team favor
         * @param event the server tick event
         */
        @SubscribeEvent
        public static void onServerTick(final ServerTickEvent.Post event) {
            // locate the current server
            MinecraftServer server = event.getServer();
            int tickCount = server.getTickCount();
            // attempt to tick non-player favor
            if(!RPGGods.CONFIG.usePlayerFavor()
                    && canTickFavor(tickCount) && !server.getPlayerList().getPlayers().isEmpty()) {
                // load RGSavedData
                RGSavedData data = RGSavedData.get(server);
                // create set of favor to tick
                Set<IFavor> favorSet;
                if(RPGGods.CONFIG.useGlobalFavor()) {
                    // add global favor
                    favorSet = ImmutableSet.of(data.getFavor());
                } else /*if(RPGGods.CONFIG.useTeamFavor())*/ {
                    // add team favor
                    favorSet = ImmutableSet.copyOf(data.getTeamFavor());
                }
                // load game time
                long gameTime = server.overworld().getGameTime();
                // favor tick
                for(IFavor favor : favorSet) {
                    // reduce cooldown
                    favor.tickCooldown(gameTime);
                    // decay favor
                    if(Math.random() < RPGGods.CONFIG.getFavorDecayRate()) {
                        favor.depleteFavor(null);
                    }
                }
            }
        }

        /**
         * @param entity the entity
         * @return true if the favor is enabled and the correct number of ticks have elapsed
         * @see #canTickFavor(int)
         */
        public static boolean canTickFavor(final LivingEntity entity) {
            return canTickFavor(entity.tickCount + entity.getId());
        }

        /**
         * @param tickCount the current tick count
         * @return true if the favor is enabled and the correct number of ticks have elapsed
         */
        public static boolean canTickFavor(final int tickCount) {
            return RPGGods.CONFIG.isFavorEnabled() && tickCount % RPGGods.CONFIG.getFavorUpdateRate() == 0;
        }
    }
}
