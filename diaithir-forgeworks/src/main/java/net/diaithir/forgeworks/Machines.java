package net.diaithir.forgeworks;

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

/** Every registry entry of the mod. Machine ids are kind id plus _age4, _age5 or _age6. */
public final class Machines {
    private Machines() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DiaithirForgeworks.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DiaithirForgeworks.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DiaithirForgeworks.ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, DiaithirForgeworks.ID);
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, DiaithirForgeworks.ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, DiaithirForgeworks.ID);
    public static final DeferredRegister<RecipeBookCategory> CATEGORIES = DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, DiaithirForgeworks.ID);

    public static final Map<Kind, DeferredHolder<RecipeType<?>, RecipeType<MachineRecipe>>> RECIPE_TYPES = new EnumMap<>(Kind.class);
    public static final Map<Kind, DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MachineRecipe>>> RECIPE_SERIALIZERS = new EnumMap<>(Kind.class);
    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> BOOK_CATEGORY = CATEGORIES.register("machine", RecipeBookCategory::new);

    public static final List<DeferredBlock<MachineBlock>> ALL_BLOCKS = new ArrayList<>();
    public static final List<DeferredItem<BlockItem>> ALL_BLOCK_ITEMS = new ArrayList<>();
    public static final List<DeferredItem<AugmentItem>> AUGMENTS = new ArrayList<>();
    public static final List<DeferredItem<Item>> MATERIALS = new ArrayList<>();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachineBlockEntity>> MACHINE_BE;
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> MENU =
            MENUS.register("machine", () -> IMenuTypeExtension.create(MachineMenu::new));

    static {
        for (Kind k : Kind.values()) {
            RECIPE_TYPES.put(k, TYPES.register(k.id, () -> new RecipeType<MachineRecipe>() {
                @Override
                public String toString() {
                    return DiaithirForgeworks.ID + ":" + k.id;
                }
            }));
            RECIPE_SERIALIZERS.put(k, SERIALIZERS.register(k.id, () -> MachineRecipe.serializer(k)));
            for (int t = 0; t < 3; t++) {
                final int tier = t;
                String name = k.id + "_age" + (4 + t);
                DeferredBlock<MachineBlock> b = BLOCKS.registerBlock(name, p -> new MachineBlock(p, k, tier), () -> BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(3.5F + tier, 9.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
                        .lightLevel(s -> s.getValue(MachineBlock.ACTIVE) ? 8 : 0));
                ALL_BLOCKS.add(b);
                ALL_BLOCK_ITEMS.add(ITEMS.registerSimpleBlockItem(b));
            }
        }
        for (Augment a : Augment.values()) {
            AUGMENTS.add(ITEMS.registerItem(a.id, p -> new AugmentItem(a, p), () -> new Item.Properties().stacksTo(16)));
        }
        for (String m : new String[] {"iron_dust", "copper_dust", "gold_dust", "sawdust", "bronze_ingot"}) {
            MATERIALS.add(ITEMS.registerSimpleItem(m));
        }
        MACHINE_BE = BLOCK_ENTITIES.register("machine", () -> new BlockEntityType<>(MachineBlockEntity::new,
                ALL_BLOCKS.stream().map(DeferredBlock::get).toArray(Block[]::new)));
    }

    static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
        TYPES.register(bus);
        SERIALIZERS.register(bus);
        CATEGORIES.register(bus);
    }
}
