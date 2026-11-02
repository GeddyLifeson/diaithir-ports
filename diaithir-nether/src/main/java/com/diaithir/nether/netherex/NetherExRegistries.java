package com.diaithir.nether.netherex;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class NetherExRegistries {
    private static final String ID = NetherExModule.MOD_ID;
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, ID);

    public static final DeferredBlock<Block> ASHSTONE = stone("ashstone", MapColor.COLOR_GRAY, 1.5f);
    public static final DeferredBlock<Block> SLAGSTONE = stone("slagstone", MapColor.COLOR_BLACK, 1.8f);
    public static final DeferredBlock<Block> BRIMSTONE_BLOCK = stone("brimstone_block", MapColor.COLOR_YELLOW, 1.2f);
    public static final DeferredBlock<Block> RUSTWOOD_PLANKS = stone("rustwood_planks", MapColor.COLOR_ORANGE, 2.0f);
    public static final DeferredBlock<Block> RUSTWOOD_LOG = stone("rustwood_log", MapColor.COLOR_BROWN, 2.0f);
    public static final DeferredBlock<Block> EMBER_ORE = block("ember_ore", () -> BlockBehaviour.Properties.of().mapColor(MapColor.NETHER).strength(3.0f, 3.0f).requiresCorrectToolForDrops().lightLevel(s -> 7));
    public static final DeferredBlock<Block> CINDER_MOSS = block("cinder_moss", () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.3f).sound(SoundType.MOSS));
    public static final DeferredBlock<Block> HEAT_GLASS = block("heat_glass", () -> BlockBehaviour.Properties.of().strength(0.6f).sound(SoundType.GLASS).noOcclusion());

    public static final DeferredItem<Item> EMBER_INGOT = ITEMS.registerItem("ember_ingot", Item::new, p -> p.fireResistant());
    public static final DeferredItem<Item> EMBER_GEM = ITEMS.registerItem("ember_gem", Item::new, p -> p.fireResistant());
    public static final DeferredItem<Item> SLAG_SHARD = ITEMS.registerSimpleItem("slag_shard");

    public static final Supplier<EntityType<Zombie>> ASH_WISP = ENTITIES.register("ash_wisp", () -> EntityType.Builder.<Zombie>of((type, level) -> new Zombie(type, level), MobCategory.MONSTER).sized(0.6f, 0.6f).fireImmune().build(key("ash_wisp")));
    public static final Supplier<EntityType<Zombie>> SLAG_CRAWLER = ENTITIES.register("slag_crawler", () -> EntityType.Builder.<Zombie>of((type, level) -> new Zombie(type, level), MobCategory.MONSTER).sized(0.9f, 0.5f).fireImmune().build(key("slag_crawler")));
    public static final Supplier<EntityType<Cow>> CINDER_GRAZER = ENTITIES.register("cinder_grazer", () -> EntityType.Builder.of(Cow::new, MobCategory.CREATURE).sized(0.9f, 1.3f).fireImmune().build(key("cinder_grazer")));
    public static final Supplier<EntityType<IronGolem>> RUST_GOLEM = ENTITIES.register("rust_golem", () -> EntityType.Builder.of(IronGolem::new, MobCategory.CREATURE).sized(1.4f, 2.7f).fireImmune().build(key("rust_golem")));

    private NetherExRegistries() {}

    private static net.minecraft.resources.ResourceKey<EntityType<?>> key(String name) {
        return net.minecraft.resources.ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(ID, name));
    }

    private static DeferredBlock<Block> stone(String name, MapColor color, float hardness) {
        return block(name, () -> BlockBehaviour.Properties.of().mapColor(color).strength(hardness, 6.0f).requiresCorrectToolForDrops());
    }

    private static DeferredBlock<Block> block(String name, Supplier<BlockBehaviour.Properties> props) {
        DeferredBlock<Block> b = BLOCKS.registerBlock(name, Block::new, props);
        ITEMS.registerSimpleBlockItem(b);
        return b;
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
    }

    public static void onAttributes(EntityAttributeCreationEvent e) {
        e.put(ASH_WISP.get(), Zombie.createAttributes().add(Attributes.MAX_HEALTH, 12).add(Attributes.FLYING_SPEED, 0.5).build());
        e.put(SLAG_CRAWLER.get(), Zombie.createAttributes().add(Attributes.MAX_HEALTH, 24).add(Attributes.ATTACK_DAMAGE, 5).build());
        e.put(CINDER_GRAZER.get(), Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 14).build());
        e.put(RUST_GOLEM.get(), IronGolem.createAttributes().build());
    }

    public static void onSpawnPlacements(RegisterSpawnPlacementsEvent e) {
        e.register(ASH_WISP.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (t, l, r, p, rnd) -> true, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        e.register(SLAG_CRAWLER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (t, l, r, p, rnd) -> true, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        e.register(CINDER_GRAZER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (t, l, r, p, rnd) -> true, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
