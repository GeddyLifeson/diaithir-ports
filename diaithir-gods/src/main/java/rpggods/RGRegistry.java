package rpggods;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import rpggods.block.AltarLightBlock;
import rpggods.block.BrazierBlock;
import rpggods.blockentity.BrazierBlockEntity;
import rpggods.entity.AltarEntity;
import rpggods.favor.Favor;
import rpggods.favor.IFavor;
import rpggods.item.AltarItem;
import rpggods.item.ScrollItem;
import rpggods.menu.AltarContainerMenu;
import rpggods.menu.FavorContainerMenu;
import rpggods.tameable.ITameable;
import rpggods.tameable.Tameable;
import rpggods.util.AltarStructureProcessor;
import rpggods.util.AutosmeltOrCobbleModifier;
import rpggods.util.CropMultiplierModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class RGRegistry {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RPGGods.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RPGGods.MODID);
    /** The economy pack expects the vault under the shared "diaithir" namespace (data/diaithir/vault.json) **/
    public static final String VAULT_NAMESPACE = "diaithir";
    private static final DeferredRegister.Blocks VAULT_BLOCKS = DeferredRegister.createBlocks(VAULT_NAMESPACE);
    private static final DeferredRegister.Items VAULT_ITEMS = DeferredRegister.createItems(VAULT_NAMESPACE);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, RPGGods.MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, RPGGods.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RPGGods.MODID);
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, RPGGods.MODID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RPGGods.MODID);
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, RPGGods.MODID);
    private static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSORS = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, RPGGods.MODID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RPGGods.MODID);

    public static void register(final IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        VAULT_BLOCKS.register(bus);
        VAULT_ITEMS.register(bus);
        ENTITY_TYPES.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        MENU_TYPES.register(bus);
        LOOT_MODIFIER_SERIALIZERS.register(bus);
        ATTACHMENTS.register(bus);
        DATA_COMPONENTS.register(bus);
        STRUCTURE_PROCESSORS.register(bus);
        TABS.register(bus);
        bus.addListener(RGRegistry::registerEntityAttributes);
    }

    public static void registerEntityAttributes(final EntityAttributeCreationEvent event) {
        event.put(ALTAR_TYPE.get(), AltarEntity.registerAttributes().build());
        event.put(PRIEST_TYPE.get(), rpggods.entity.PriestEntity.registerAttributes().build());
    }

    //// ATTACHMENTS (replace the Forge capabilities) ////
    /** Favor of a player, kept across death **/
    public static final Supplier<AttachmentType<IFavor>> FAVOR = ATTACHMENTS.register("favor", () ->
            AttachmentType.builder(() -> (IFavor) new Favor()).serialize(Favor.CODEC).copyOnDeath().build());
    /** Tame and sitting state of a mob **/
    public static final Supplier<AttachmentType<ITameable>> TAMEABLE = ATTACHMENTS.register("tameable", () ->
            AttachmentType.builder(() -> (ITameable) new Tameable()).serialize(Tameable.CODEC).build());

    //// DATA COMPONENTS ////
    /** The ID of the altar data that an altar item will place **/
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Identifier>> ALTAR_ID = DATA_COMPONENTS.registerComponentType("altar",
            builder -> builder.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC));

    //// STRUCTURE PROCESSORS ////
    public static final Supplier<StructureProcessorType<AltarStructureProcessor>> ALTAR_STRUCTURE_PROCESSOR =
            STRUCTURE_PROCESSORS.register("altar", () -> (StructureProcessorType<AltarStructureProcessor>) () -> AltarStructureProcessor.CODEC);

    //// ENTITIES ////
    public static final DeferredHolder<EntityType<?>, EntityType<AltarEntity>> ALTAR_TYPE = ENTITY_TYPES.register("altar", () ->
            EntityType.Builder.<AltarEntity>of(AltarEntity::new, MobCategory.MISC)
                    .sized(0.8F, 2.48F).clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(RPGGods.MODID, "altar"))));

    public static final DeferredHolder<EntityType<?>, EntityType<rpggods.entity.PriestEntity>> PRIEST_TYPE = ENTITY_TYPES.register("priest", () ->
            EntityType.Builder.<rpggods.entity.PriestEntity>of(rpggods.entity.PriestEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F).clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(RPGGods.MODID, "priest"))));

    //// BLOCKS ////
    public static final DeferredBlock<rpggods.block.TempleVaultBlock> TEMPLE_VAULT_BLOCK = VAULT_BLOCKS.registerBlock("temple_vault", rpggods.block.TempleVaultBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops()
                    .strength(5.0F, 12.0F).sound(SoundType.STONE));
    public static final DeferredBlock<AltarLightBlock> LIGHT_BLOCK = BLOCKS.registerBlock("light", AltarLightBlock::new,
            () -> BlockBehaviour.Properties.of().replaceable().noCollision().noLootTable().air()
                    .strength(-1F).randomTicks()
                    .lightLevel(b -> b.getValue(AltarLightBlock.LEVEL)));
    public static final DeferredBlock<BrazierBlock> BRAZIER_BLOCK = BLOCKS.registerBlock("brazier", BrazierBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                    .strength(3.0F).sound(SoundType.STONE).pushReaction(PushReaction.NORMAL).noOcclusion()
                    .lightLevel(b -> b.getValue(BrazierBlock.LIT) ? 15 : 0));

    //// ITEMS ////
    public static final DeferredItem<AltarItem> ALTAR_ITEM = ITEMS.registerItem("altar", AltarItem::new, () -> new Item.Properties());
    public static final DeferredItem<ScrollItem> SCROLL_ITEM = ITEMS.registerItem("scroll", ScrollItem::new, () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> LIGHT_ITEM = ITEMS.registerSimpleBlockItem("light", LIGHT_BLOCK);
    public static final DeferredItem<net.minecraft.world.item.BlockItem> BRAZIER_ITEM = ITEMS.registerSimpleBlockItem("brazier", BRAZIER_BLOCK);

    public static final DeferredItem<net.minecraft.world.item.BlockItem> TEMPLE_VAULT_ITEM = VAULT_ITEMS.registerSimpleBlockItem("temple_vault", TEMPLE_VAULT_BLOCK);
    public static final DeferredItem<Item> PRIEST_SPAWN_EGG = ITEMS.registerItem("priest_spawn_egg",
            props -> new net.minecraft.world.item.SpawnEggItem(props.spawnEgg(PRIEST_TYPE.get())));

    //// BLOCK ENTITIES ////
    public static final Supplier<BlockEntityType<rpggods.blockentity.TempleVaultBlockEntity>> TEMPLE_VAULT_TYPE = BLOCK_ENTITY_TYPES.register("temple_vault", () ->
            new BlockEntityType<>(rpggods.blockentity.TempleVaultBlockEntity::new, TEMPLE_VAULT_BLOCK.get()));
    public static final Supplier<BlockEntityType<BrazierBlockEntity>> BRAZIER_TYPE = BLOCK_ENTITY_TYPES.register("brazier", () ->
            new BlockEntityType<>(BrazierBlockEntity::new, BRAZIER_BLOCK.get()));

    //// MENU TYPES ////
    public static final Supplier<MenuType<AltarContainerMenu>> ALTAR_CONTAINER = MENU_TYPES.register("altar_container", () ->
            IMenuTypeExtension.create((windowId, inv, data) -> {
                final int entityId = data.readInt();
                net.minecraft.world.entity.Entity entity = inv.player.level().getEntity(entityId);
                AltarEntity altarEntity = (AltarEntity) entity;
                return new AltarContainerMenu(windowId, inv, altarEntity.getInventory(), altarEntity);
            }));
    public static final Supplier<MenuType<FavorContainerMenu>> FAVOR_CONTAINER = MENU_TYPES.register("favor_container", () ->
            IMenuTypeExtension.create((windowId, inv, data) -> {
                CompoundTag nbt = data.readNbt();
                // load favor from the server-sent copy
                IFavor favor = new Favor();
                if (nbt != null) {
                    favor.deserializeNBT(nbt);
                }
                // load deity
                boolean hasDeity = data.readBoolean();
                Identifier deityId = null;
                if (hasDeity) {
                    deityId = data.readIdentifier();
                }
                return new FavorContainerMenu(windowId, inv, favor, deityId);
            }));

    //// LOOT MODIFIER SERIALIZERS ////
    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> AUTOSMELT_LOOT_MODIFIER =
            LOOT_MODIFIER_SERIALIZERS.register("autosmelt_or_cobble", () -> AutosmeltOrCobbleModifier.CODEC);
    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> CROP_LOOT_MODIFIER =
            LOOT_MODIFIER_SERIALIZERS.register("crop_multiplier", () -> CropMultiplierModifier.CODEC);

    //// CREATIVE TAB ////
    public static final Supplier<CreativeModeTab> TAB = TABS.register("gods", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.diaithir_gods"))
            .icon(() -> new ItemStack(SCROLL_ITEM.get()))
            .displayItems((params, output) -> {
                output.accept(SCROLL_ITEM.get());
                output.accept(BRAZIER_ITEM.get());
                output.accept(TEMPLE_VAULT_ITEM.get());
                output.accept(PRIEST_SPAWN_EGG.get());
                List<ItemStack> altars = new ArrayList<>();
                AltarItem.addAltarItems(altars);
                altars.forEach(output::accept);
            }).build());
}
