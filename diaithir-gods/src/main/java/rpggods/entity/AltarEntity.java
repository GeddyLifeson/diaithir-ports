package rpggods.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import rpggods.RGEvents;
import rpggods.RGRegistry;
import rpggods.RPGGods;
import rpggods.block.AltarLightBlock;
import rpggods.deity.Altar;
import rpggods.deity.Deity;
import rpggods.deity.DeityHelper;
import rpggods.favor.IFavor;
import rpggods.item.AltarItem;
import rpggods.menu.AltarContainerMenu;
import rpggods.menu.FavorContainerMenu;
import rpggods.network.SUpdateAltarPacket;
import rpggods.perk.PerkCondition;
import rpggods.util.altar.AltarItems;
import rpggods.util.altar.AltarPose;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The statue (altar) entity. Ported from an armor-stand style LivingEntity of RPG Gods.
 * The first six inventory slots hold hand and armor items, the last slot holds the altar block.
 */
public class AltarEntity extends LivingEntity {

    private static final EntityDataAccessor<String> ALTAR = SynchedEntityData.defineId(AltarEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DEITY = SynchedEntityData.defineId(AltarEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(AltarEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> LOCKED = SynchedEntityData.defineId(AltarEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<String> POSE = SynchedEntityData.defineId(AltarEntity.class, EntityDataSerializers.STRING);

    private static final String KEY_ALTAR = "Altar";
    private static final String KEY_DEITY = "Deity";
    private static final String KEY_INVENTORY = "Inventory";
    private static final String KEY_FLAGS = "Flags";
    private static final String KEY_LOCKED = "Locked";
    private static final String KEY_POSE = "Pose";

    /** Codec for the inventory: a list of INV_SIZE item stacks, index is the slot **/
    private static final Codec<List<ItemStack>> INVENTORY_CODEC = ItemStack.OPTIONAL_CODEC.listOf();

    public static final int INV_SIZE = 7;
    private SimpleContainer inventory;

    private AltarPose pose = AltarPose.EMPTY;
    private final EntityDimensions smallSize = EntityDimensions.fixed(0.8F, 1.98F);
    private Optional<Identifier> deity = Optional.empty();
    @Nullable
    private ResolvableProfile playerProfile = null;

    public long lastHit;

    public AltarEntity(final EntityType<? extends AltarEntity> entityType, final Level world) {
        super(entityType, world);
        initInventory();
    }

    public static AltarEntity createAltar(final Level world, final BlockPos pos, Direction facing, final Identifier altar) {
        AltarEntity entity = new AltarEntity(RGRegistry.ALTAR_TYPE.get(), world);
        float f = facing.toYRot();
        entity.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, f, 0);
        entity.yHeadRot = f;
        entity.yBodyRot = f;
        entity.applyAltarProperties(altar);
        return entity;
    }

    public static AttributeSupplier.Builder registerAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0F)
                .add(Attributes.STEP_HEIGHT, 0.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ALTAR, "");
        builder.define(DEITY, "");
        builder.define(FLAGS, (byte) 0);
        builder.define(LOCKED, (byte) 0);
        builder.define(POSE, "{}");
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key.equals(DEITY)) {
            String sDeityId = getEntityData().get(DEITY);
            if (!sDeityId.isEmpty() && sDeityId.contains(":")) {
                this.deity = Optional.ofNullable(Identifier.tryParse(sDeityId));
            } else {
                this.deity = Optional.empty();
            }
        }
        if (key.equals(POSE)) {
            try {
                this.pose = new AltarPose(net.minecraft.nbt.TagParser.parseCompoundFully(getEntityData().get(POSE)));
            } catch (Exception e) {
                this.pose = AltarPose.EMPTY;
            }
        }
    }

    /** Maps an equipment slot to an inventory index, or -1 if the altar does not hold items in that slot **/
    private static int indexOf(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> 0;
            case FEET -> 1;
            case LEGS -> 2;
            case CHEST -> 3;
            case HEAD -> 4;
            case OFFHAND -> 5;
            default -> -1;
        };
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slotIn) {
        int index = indexOf(slotIn);
        return index < 0 || inventory == null ? ItemStack.EMPTY : this.inventory.getItem(index);
    }

    public ItemStack getBlockBySlot() {
        return this.inventory.getItem(INV_SIZE - 1);
    }

    @Override
    public void setItemSlot(EquipmentSlot slotIn, ItemStack stack) {
        int index = indexOf(slotIn);
        if (index >= 0 && inventory != null) {
            this.inventory.setItem(index, stack);
        }
    }

    public void setBlockSlot(ItemStack stack) {
        this.inventory.setItem(INV_SIZE - 1, stack);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return indexOf(slot) >= 0;
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entityIn) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean attackable() {
        return false;
    }

    @Override
    public boolean skipAttackInteraction(Entity source) {
        return source instanceof Player playerSource && !this.level().mayInteract(playerSource, this.blockPosition());
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    @Override
    public void kill(ServerLevel level) {
        this.remove(Entity.RemovalReason.KILLED);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isRemoved()) {
            return false;
        } else if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.kill(level);
            return false;
        } else if (this.isInvulnerableTo(level, source)) {
            return false;
        } else if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            this.brokenByAnything(level, source);
            this.kill(level);
            return false;
        } else if (source.is(DamageTypeTags.IGNITES_ARMOR_STANDS)) {
            if (this.isOnFire()) {
                this.causeDamage(level, source, 0.15F);
            } else {
                this.igniteForSeconds(5.0F);
            }
            return false;
        } else if (source.is(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5F) {
            this.causeDamage(level, source, 4.0F);
            return false;
        } else {
            boolean allowIncrementalBreaking = source.is(DamageTypeTags.CAN_BREAK_ARMOR_STAND);
            boolean shouldKill = source.is(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS);
            if (!allowIncrementalBreaking && !shouldKill) {
                return false;
            } else if (source.getEntity() instanceof Player player && !player.getAbilities().mayBuild) {
                return false;
            } else if (source.isCreativePlayer()) {
                this.playBrokenSound();
                this.showBreakingParticles();
                this.kill(level);
                return true;
            } else {
                long i = level.getGameTime();
                if (i - this.lastHit > 5L && !shouldKill) {
                    level.broadcastEntityEvent(this, (byte) 32);
                    this.lastHit = i;
                } else {
                    this.brokenByPlayer(level, source);
                    this.showBreakingParticles();
                    this.kill(level);
                }
                return true;
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 32) {
            if (this.level().isClientSide()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.STONE_HIT, this.getSoundSource(), 0.3F, 1.0F, false);
                this.lastHit = this.level().getGameTime();
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose poseIn) {
        return (inventory == null || getBlockBySlot().isEmpty()) ? smallSize : this.getType().getDimensions();
    }

    @Override
    public ItemStack getPickResult() {
        ItemStack stack = new ItemStack(RGRegistry.ALTAR_ITEM.get());
        stack.set(RGRegistry.ALTAR_ID.get(), getAltar());
        return stack;
    }

    @Override
    public void tick() {
        // client-side tick logic
        if (firstTick && level().isClientSide()) {
            // update game profile
            setCustomName(getCustomName());
        }
        // parent tick
        super.tick();
        // server-side tick logic
        if (!level().isClientSide()) {
            // check if altar has a deity
            if (getDeity().isPresent()) {
                // check if there are any perk conditions for "ritual"
                DeityHelper helper = RPGGods.DEITY_HELPER.computeIfAbsent(getDeity().get(), DeityHelper::new);
                if (!helper.perkByConditionMap.getOrDefault(PerkCondition.Type.RITUAL, ImmutableList.of()).isEmpty()) {
                    // onPerformRitual
                    RGEvents.performRitual(this, getDeity().get());
                }
            }
            // attempt to place light block
            if (tickCount % 4 == 1) {
                Altar altar = RPGGods.ALTAR_MAP.getOrDefault(getAltar(), Altar.EMPTY);
                int lightLevel = altar.getLightLevel();
                // check light level
                if (lightLevel > 0) {
                    BlockPos posIn = getOnPos().above();
                    BlockState blockIn = level().getBlockState(posIn);
                    // check if current block can be replaced
                    if ((blockIn.isAir() || blockIn.getFluidState().is(FluidTags.WATER))
                            && !RGRegistry.LIGHT_BLOCK.get().defaultBlockState().is(blockIn.getBlock())) {
                        // determine waterlog value
                        boolean waterlogged = blockIn.getFluidState().isSource() && blockIn.getFluidState().is(FluidTags.WATER);
                        // create light block
                        BlockState lightBlock = RGRegistry.LIGHT_BLOCK.get()
                                .defaultBlockState()
                                .setValue(AltarLightBlock.LEVEL, lightLevel)
                                .setValue(AltarLightBlock.WATERLOGGED, waterlogged);
                        // place light block
                        level().setBlock(posIn, lightBlock, Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    private void showBreakingParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), this.getX(), this.getY(0.66D), this.getZ(), 10, (double) (this.getBbWidth() / 4.0F), (double) (this.getBbHeight() / 4.0F), (double) (this.getBbWidth() / 4.0F), 0.05D);
        }
    }

    private void causeDamage(ServerLevel level, DamageSource source, float amount) {
        float f = this.getHealth();
        f = f - amount;
        if (f <= 0.5F) {
            this.brokenByAnything(level, source);
            this.kill(level);
        } else {
            this.setHealth(f);
        }
    }

    private void brokenByPlayer(ServerLevel level, DamageSource source) {
        // drop altar
        final ItemStack altarItem = new ItemStack(RGRegistry.ALTAR_ITEM.get());
        altarItem.set(RGRegistry.ALTAR_ID.get(), getAltar());
        Block.popResource(level, blockPosition().above(), altarItem);
        // drop other
        this.brokenByAnything(level, source);
    }

    private void brokenByAnything(ServerLevel level, DamageSource source) {
        this.playBrokenSound();
        BlockPos pos = blockPosition().above();
        // drop inventory
        if (this.inventory != null) {
            if (!isHandsLocked()) {
                Block.popResource(level, pos, getItemBySlot(EquipmentSlot.MAINHAND));
                Block.popResource(level, pos, getItemBySlot(EquipmentSlot.OFFHAND));
                setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            }
            if (!isArmorLocked()) {
                Block.popResource(level, pos, getItemBySlot(EquipmentSlot.FEET));
                Block.popResource(level, pos, getItemBySlot(EquipmentSlot.LEGS));
                Block.popResource(level, pos, getItemBySlot(EquipmentSlot.CHEST));
                Block.popResource(level, pos, getItemBySlot(EquipmentSlot.HEAD));
                setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
                setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
                setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            }
            if (!isBlockLocked()) {
                Block.popResource(level, pos, getBlockBySlot());
                setBlockSlot(ItemStack.EMPTY);
            }
        }
    }

    private void playBrokenSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.STONE_BREAK, this.getSoundSource(), 1.0F, 1.0F);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 vec) {
        if (hand != InteractionHand.MAIN_HAND || !this.isAlive() || player.isSpectator()) {
            return super.interact(player, hand, vec);
        }
        // name tags use the default behavior
        if (player.getItemInHand(hand).is(net.minecraft.world.item.Items.NAME_TAG)) {
            return super.interact(player, hand, vec);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        // check if altar is deity
        if (getDeity().isPresent()) {
            // attempt to process favor
            Optional<IFavor> favor = RPGGods.getFavor(player);
            if (favor.isPresent()) {
                Identifier deity = getDeity().get();
                IFavor ifavor = favor.get();
                boolean enabled = ifavor.getFavor(deity).isEnabled();
                if (!enabled) {
                    // send feedback about disabled deity
                    Component message = Component.translatable("favor.locked");
                    rpggods.util.Nbt.msg(player, message, true);
                    return InteractionResult.PASS;
                }
                // detect item in mainhand
                ItemStack heldItem = player.getItemInHand(hand);
                if (!heldItem.isEmpty()) {
                    // attempt to process held item as offering
                    Optional<ItemStack> offeringResult = RGEvents.onOffering(Optional.of(this), deity, player, ifavor, heldItem, false);
                    // if offering succeeded, update player inventory
                    if (offeringResult.isPresent()) {
                        player.setItemInHand(hand, offeringResult.get());
                        return InteractionResult.CONSUME;
                    }
                }
                // no offering result, open favor GUI
                serverPlayer.openMenu(
                        new SimpleMenuProvider((id, inventory, p) ->
                                new FavorContainerMenu(id, inventory, ifavor, deity),
                                Component.empty()),
                        buf -> {
                            buf.writeNbt(ifavor.serializeNBT());
                            buf.writeBoolean(true);
                            buf.writeIdentifier(deity);
                        });
                return InteractionResult.SUCCESS_SERVER;
            }
        } else if (!(isArmorLocked() && isHandsLocked() && isBlockLocked() && isAltarPoseLocked())) {
            // open altar GUI
            serverPlayer.openMenu(
                    new SimpleMenuProvider((id, inv, p) ->
                            new AltarContainerMenu(id, inv, this.inventory, this),
                            Component.empty()),
                    buf -> buf.writeInt(this.getId()));
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        // write deity
        if (getDeity().isPresent()) {
            output.putString(KEY_DEITY, getDeity().get().toString());
        }
        // write altar
        output.putString(KEY_ALTAR, getAltar().toString());
        // write inventory
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            stacks.add(inventory.getItem(i));
        }
        output.store(KEY_INVENTORY, INVENTORY_CODEC, stacks);
        // write flags
        output.putByte(KEY_FLAGS, getFlags());
        // write locked flags
        output.putByte(KEY_LOCKED, getLocked());
        // write pose
        output.store(KEY_POSE, CompoundTag.CODEC, getAltarPose().serializeNBT());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        // read deity
        input.getString(KEY_DEITY).ifPresent(deity -> {
            if (!deity.isEmpty()) {
                setDeity(Optional.ofNullable(Identifier.tryParse(deity)));
            }
        });
        // read altar
        input.getString(KEY_ALTAR).ifPresent(altar -> {
            Identifier id = Identifier.tryParse(altar);
            if (id != null) {
                setAltar(id);
            }
        });
        // init inventory
        initInventory();
        // read inventory
        input.read(KEY_INVENTORY, INVENTORY_CODEC).ifPresent(list -> {
            for (int i = 0; i < list.size() && i < inventory.getContainerSize(); i++) {
                inventory.setItem(i, list.get(i));
            }
        });
        this.inventory.setChanged();
        // read flags
        setFlags(input.getByteOr(KEY_FLAGS, (byte) 0));
        // read locked flags
        setLocked(input.getByteOr(KEY_LOCKED, (byte) 0));
        // read pose
        setAltarPose(new AltarPose(input.read(KEY_POSE, CompoundTag.CODEC).orElseGet(CompoundTag::new)));
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public void initInventory() {
        SimpleContainer simplecontainer = this.inventory;
        this.inventory = new AltarContainer(INV_SIZE);
        if (simplecontainer != null) {
            int i = Math.min(simplecontainer.getContainerSize(), this.inventory.getContainerSize());

            for (int j = 0; j < i; ++j) {
                ItemStack itemstack = simplecontainer.getItem(j);
                if (!itemstack.isEmpty()) {
                    this.inventory.setItem(j, itemstack.copy());
                }
            }
        }

        this.containerChanged(this.inventory);
    }

    @Override
    public void refreshDimensions() {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        super.refreshDimensions();
        this.setPos(d0, d1, d2);
    }

    /** Container that notifies the altar entity when its contents change **/
    private class AltarContainer extends SimpleContainer {
        AltarContainer(int size) {
            super(size);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            AltarEntity.this.containerChanged(this);
        }
    }

    public void containerChanged(Container inv) {
        this.refreshDimensions();
        // send packet to client to notify change
        if (!this.level().isClientSide() && this.isAddedToLevel()) {
            ItemStack block = getBlockBySlot();
            PacketDistributor.sendToPlayersTrackingEntity(this, new SUpdateAltarPacket(this.getId(), block));
        }
    }

    /**
     * Applies the Altar properties to this entity
     *
     * @param altarId the Altar with properties to use
     */
    public void applyAltarProperties(final Identifier altarId) {
        // query altar by id
        Altar altar = RPGGods.ALTAR_MAP.getOrDefault(altarId, Altar.EMPTY);
        // apply properties
        setDeity(altar.getDeity());
        setFemale(altar.isFemale());
        setSlim(altar.isSlim());
        setArmorLocked(altar.getItems().isArmorLocked());
        setHandsLocked(altar.getItems().isHandsLocked());
        setBlockLocked(altar.getItems().isBlockLocked());
        setAltarPose(altar.getPose());
        setAltarPoseLocked(altar.isPoseLocked());
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setItemSlot(slot, altar.getItems().getItemStackFromSlot(slot).copy());
        }
        setBlockSlot(new ItemStack(altar.getItems().getBlock().asItem()));
        // custom name
        if (altar.getDeity().isPresent()) {
            setCustomName(DeityHelper.getName(altarId));
        } else if (altar.getName().isPresent()) {
            setCustomName(Component.literal(altar.getName().get()));
        }
        // save altar id
        setAltar(altarId);
    }

    /**
     * Directly writes properties from an Altar to a Compound NBT Tag
     * @param registries registry access used to encode item stacks and the custom name
     * @param altarId the Altar ID
     * @param altar the Altar as retrieved from the altar map
     * @return a CompoundTag that represents the Altar applied to an AltarEntity
     */
    public static CompoundTag writeAltarProperties(final HolderLookup.Provider registries, final Identifier altarId, final Altar altar) {
        // create compound tag
        CompoundTag compoundTag = new CompoundTag();
        if (null == altar) {
            return compoundTag;
        }
        RegistryOps<net.minecraft.nbt.Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        // write altar
        compoundTag.putString(KEY_ALTAR, altarId.toString());
        // determine custom name
        Component customName = Component.literal(altar.getName().orElse(""));
        // write deity
        if (altar.getDeity().isPresent() && !altar.getDeity().get().toString().isEmpty()) {
            Identifier deityId = altar.getDeity().get();
            customName = DeityHelper.getName(altarId);
            compoundTag.putString(KEY_DEITY, deityId.toString());
        }
        // write custom name
        if (!customName.getString().isEmpty()) {
            compoundTag.store("CustomName", ComponentSerialization.CODEC, ops, customName);
            compoundTag.putBoolean("CustomNameVisible", true);
        }
        // write inventory
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < INV_SIZE; i++) {
            stacks.add(ItemStack.EMPTY);
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            int index = indexOf(slot);
            if (index >= 0) {
                stacks.set(index, altar.getItems().getItemStackFromSlot(slot).copy());
            }
        }
        stacks.set(INV_SIZE - 1, new ItemStack(altar.getItems().getBlock()));
        compoundTag.store(KEY_INVENTORY, INVENTORY_CODEC, ops, stacks);
        // determine flags
        byte flags = 0;
        if (altar.isFemale()) flags = (byte) (flags | 0x01);
        if (altar.isSlim()) flags = (byte) (flags | 0x02);
        // write flags
        compoundTag.putByte(KEY_FLAGS, flags);
        // determine locked flags
        byte locked = 0;
        if (altar.getItems().isArmorLocked()) locked = (byte) (locked | 0x01);
        if (altar.getItems().isHandsLocked()) locked = (byte) (locked | 0x02);
        if (altar.getItems().isBlockLocked()) locked = (byte) (locked | 0x04);
        if (altar.isPoseLocked()) locked = (byte) (locked | 0x08);
        // write locked flags
        compoundTag.putByte(KEY_LOCKED, locked);
        // write pose
        compoundTag.put(KEY_POSE, altar.getPose().serializeNBT());
        return compoundTag;
    }

    /**
     * @return a new Altar instance with the same properties as found in this entity.
     */
    public rpggods.deity.Altar createAltarProperties() {
        AltarItems items;
        items = new AltarItems(
                rpggods.deity.Offering.toTemplate(getItemBySlot(EquipmentSlot.HEAD)),
                rpggods.deity.Offering.toTemplate(getItemBySlot(EquipmentSlot.CHEST)),
                rpggods.deity.Offering.toTemplate(getItemBySlot(EquipmentSlot.LEGS)),
                rpggods.deity.Offering.toTemplate(getItemBySlot(EquipmentSlot.FEET)),
                rpggods.deity.Offering.toTemplate(getItemBySlot(EquipmentSlot.MAINHAND)),
                rpggods.deity.Offering.toTemplate(getItemBySlot(EquipmentSlot.OFFHAND)),
                Block.byItem(getBlockBySlot().getItem()),
                isArmorLocked(), isHandsLocked(), isBlockLocked());
        Optional<String> name = hasCustomName() ? Optional.of(getCustomName().getString()) : Optional.empty();
        boolean enabled = true;
        Identifier material = rpggods.deity.Altar.MATERIAL;
        int lightLevel = 0;
        return new rpggods.deity.Altar(enabled, name, isFemale(), isSlim(), lightLevel, items,
                material, getAltarPose(), isAltarPoseLocked());
    }

    public void setAltar(final Identifier altar) {
        getEntityData().set(ALTAR, altar.toString());
    }

    public Identifier getAltar() {
        String altar = getEntityData().get(ALTAR);
        if (!altar.isEmpty() && altar.contains(":")) {
            return Identifier.tryParse(altar);
        }
        return Identifier.parse("null");
    }

    public void setDeity(final Optional<Identifier> deity) {
        this.deity = deity;
        String deityString = "";
        if (deity.isPresent() && !deity.get().toString().isEmpty()) {
            deityString = deity.get().toString();
        }
        // update data manager
        getEntityData().set(DEITY, deityString);
    }

    public Optional<Identifier> getDeity() {
        return this.deity;
    }

    public boolean isNameLocked() {
        return getDeity().isPresent();
    }

    public boolean isArmorLocked() {
        return (getLocked() & 0x01) > 0;
    }

    public boolean isHandsLocked() {
        return (getLocked() & 0x02) > 0;
    }

    public boolean isBlockLocked() {
        return (getLocked() & 0x04) > 0;
    }

    public boolean isAltarPoseLocked() {
        return (getLocked() & 0x08) > 0;
    }

    public void setArmorLocked(boolean armorLocked) {
        byte locked = getLocked();
        setLocked((byte) (armorLocked ? (locked | 0x01) : (locked & ~0x01)));
    }

    public void setHandsLocked(boolean handsLocked) {
        byte locked = getLocked();
        setLocked((byte) (handsLocked ? (locked | 0x02) : (locked & ~0x02)));
    }

    public void setAltarPoseLocked(final boolean poseLocked) {
        byte locked = getLocked();
        setLocked((byte) (poseLocked ? (locked | 0x08) : (locked & ~0x08)));
    }

    public void setBlockLocked(boolean blockLocked) {
        byte locked = getLocked();
        setLocked((byte) (blockLocked ? (locked | 0x04) : (locked & ~0x04)));
    }

    private byte getLocked() {
        return getEntityData().get(LOCKED);
    }

    private void setLocked(byte b) {
        getEntityData().set(LOCKED, b);
    }

    public void setFemale(boolean female) {
        byte flags = getFlags();
        setFlags((byte) (female ? (flags | 0x01) : (flags & ~0x01)));
    }

    public void setSlim(boolean slim) {
        byte flags = getFlags();
        setFlags((byte) (slim ? (flags | 0x02) : (flags & ~0x02)));
    }

    @Override
    public void setCustomName(@Nullable final Component name) {
        super.setCustomName(name);
        // attempt to use custom name to pick a player skin for the statue
        if (name != null) {
            String sName = name.getString();
            if (sName.length() > 0 && sName.length() <= 16 && !sName.contains(":") && !sName.contains(" ")) {
                this.playerProfile = ResolvableProfile.createUnresolved(sName);
            } else {
                this.playerProfile = null;
            }
        } else {
            this.playerProfile = null;
        }
    }

    public boolean isFemale() {
        return (getFlags() & 0x01) > 0;
    }

    public boolean isSlim() {
        return (getFlags() & 0x02) > 0;
    }

    private byte getFlags() {
        return getEntityData().get(FLAGS).byteValue();
    }

    private void setFlags(byte b) {
        getEntityData().set(FLAGS, b);
    }

    public AltarPose getAltarPose() {
        return pose;
    }

    public void setAltarPose(final AltarPose pose) {
        this.pose = pose;
        this.getEntityData().set(POSE, this.pose.serializeNBT().toString());
    }

    @Nullable
    public ResolvableProfile getPlayerProfile() {
        return this.playerProfile;
    }
}
