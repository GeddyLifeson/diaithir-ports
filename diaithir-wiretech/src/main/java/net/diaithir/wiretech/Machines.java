package net.diaithir.wiretech;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Every registry entry of the mod.  */
public final class Machines {
    private Machines() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DiaithirWiretech.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DiaithirWiretech.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DiaithirWiretech.ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, DiaithirWiretech.ID);
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, DiaithirWiretech.ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, DiaithirWiretech.ID);
    public static final DeferredRegister<RecipeBookCategory> CATEGORIES = DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, DiaithirWiretech.ID);

    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, DiaithirWiretech.ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> WIRE_FROM =
            COMPONENTS.registerComponentType("wire_from", b -> b.persistent(GlobalPos.CODEC));

    public static final Map<Kind, DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>>> RECIPE_TYPES = new EnumMap<>(Kind.class);
    public static final Map<Kind, DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MachineRecipe>>> RECIPE_SERIALIZERS = new EnumMap<>(Kind.class);
    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> BOOK_CATEGORY = CATEGORIES.register("machine", RecipeBookCategory::new);

    public static final List<DeferredBlock<MachineBlock>> ALL_BLOCKS = new ArrayList<>();
    public static final List<DeferredItem<BlockItem>> ALL_BLOCK_ITEMS = new ArrayList<>();
    public static final List<DeferredItem<AugmentItem>> AUGMENTS = new ArrayList<>();
    public static final List<DeferredItem<Item>> MATERIALS = new ArrayList<>();
    public static final Map<WireType, DeferredBlock<ConnectorBlock>> CONNECTORS = new EnumMap<>(WireType.class);
    public static final Map<WireType, DeferredItem<BlockItem>> CONNECTOR_ITEMS = new EnumMap<>(WireType.class);
    public static final Map<WireType, DeferredItem<WireCoilItem>> COILS = new EnumMap<>(WireType.class);
    public static final Map<RotorKind, DeferredBlock<RotorBlock>> ROTORS = new EnumMap<>(RotorKind.class);
    public static final List<DeferredItem<BlockItem>> GENERATOR_ITEMS = new ArrayList<>();
    public static final List<DeferredItem<BlockItem>> BUILDING_ITEMS = new ArrayList<>();
    public static final DeferredBlock<DynamoBlock> DYNAMO;
    public static final String[] SHEETMETAL = {"iron", "copper", "steel", "electrum"};
    public static final String[] SCAFFOLDS = {"iron", "steel"};

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachineBlockEntity>> MACHINE_BE;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConnectorBlockEntity>> CONNECTOR_BE;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RotorBlockEntity>> ROTOR_BE;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DynamoBlockEntity>> DYNAMO_BE;
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> MENU =
            MENUS.register("machine", () -> IMenuTypeExtension.create(MachineMenu::new));

    static {
        for (Kind k : Kind.values()) {
            RECIPE_TYPES.put(k, TYPES.register(k.id, () -> new RecipeType<MachineRecipe>() {
                @Override
                public String toString() {
                    return DiaithirWiretech.ID + ":" + k.id;
                }
            }));
            RECIPE_SERIALIZERS.put(k, SERIALIZERS.register(k.id, () -> MachineRecipe.serializer(k)));
            String name = k.id;
            DeferredBlock<MachineBlock> b = BLOCKS.registerBlock(name, p -> new MachineBlock(p, k, 0), () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(3.5F, 9.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(MachineBlock.ACTIVE) ? 8 : 0));
            ALL_BLOCKS.add(b);
            ALL_BLOCK_ITEMS.add(ITEMS.registerSimpleBlockItem(b));
        }
        for (WireType w : WireType.values()) {
            DeferredBlock<ConnectorBlock> c = BLOCKS.registerBlock(w.id + "_connector", p -> new ConnectorBlock(p, w), () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(1.5F, 6.0F).sound(SoundType.METAL).noOcclusion());
            CONNECTORS.put(w, c);
            CONNECTOR_ITEMS.put(w, ITEMS.registerSimpleBlockItem(c));
            COILS.put(w, ITEMS.registerItem(w.id + "_wire_coil", p -> new WireCoilItem(w, p), () -> new Item.Properties().stacksTo(16)));
        }
        for (RotorKind r : RotorKind.values()) {
            DeferredBlock<RotorBlock> b = BLOCKS.registerBlock(r.id, p -> new RotorBlock(p, r), () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD).strength(2.0F, 6.0F).sound(r == RotorKind.HAND_CRANK ? SoundType.METAL : SoundType.WOOD).noOcclusion());
            ROTORS.put(r, b);
            GENERATOR_ITEMS.add(ITEMS.registerSimpleBlockItem(b));
        }
        DYNAMO = BLOCKS.registerBlock("dynamo", DynamoBlock::new, () -> BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(3.0F, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
        GENERATOR_ITEMS.add(ITEMS.registerSimpleBlockItem(DYNAMO));
        for (String m : SHEETMETAL) {
            DeferredBlock<Block> b = BLOCKS.registerBlock("sheetmetal_" + m, Block::new, () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(3.0F, 8.0F).sound(SoundType.COPPER).requiresCorrectToolForDrops());
            BUILDING_ITEMS.add(ITEMS.registerSimpleBlockItem(b));
        }
        for (String m : SCAFFOLDS) {
            DeferredBlock<ScaffoldBlock> b = BLOCKS.registerBlock(m + "_scaffolding", ScaffoldBlock::new, () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(1.5F, 5.0F).sound(SoundType.SCAFFOLDING).noOcclusion().requiresCorrectToolForDrops());
            BUILDING_ITEMS.add(ITEMS.registerSimpleBlockItem(b));
        }
        for (Augment a : Augment.values()) {
            AUGMENTS.add(ITEMS.registerItem(a.id, p -> new AugmentItem(a, p), () -> new Item.Properties().stacksTo(16)));
        }
        for (String m : new String[] {"iron_dust", "copper_dust", "gold_dust", "coal_dust", "slag", "electrum_ingot", "steel_ingot"}) {
            MATERIALS.add(ITEMS.registerSimpleItem(m));
        }
        CONNECTOR_BE = BLOCK_ENTITIES.register("connector", () -> new BlockEntityType<>(ConnectorBlockEntity::new,
                CONNECTORS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)));
        ROTOR_BE = BLOCK_ENTITIES.register("rotor", () -> new BlockEntityType<>(RotorBlockEntity::new,
                ROTORS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)));
        DYNAMO_BE = BLOCK_ENTITIES.register("dynamo", () -> new BlockEntityType<>(DynamoBlockEntity::new, DYNAMO.get()));
        MACHINE_BE = BLOCK_ENTITIES.register("machine", () -> new BlockEntityType<>(MachineBlockEntity::new,
                ALL_BLOCKS.stream().map(DeferredBlock::get).toArray(Block[]::new)));
    }

    static void register(IEventBus bus) {
        COMPONENTS.register(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
        TYPES.register(bus);
        SERIALIZERS.register(bus);
        CATEGORIES.register(bus);
    }
}
