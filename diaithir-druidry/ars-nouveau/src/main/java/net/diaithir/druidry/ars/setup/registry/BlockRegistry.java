package net.diaithir.druidry.ars.setup.registry;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.common.block.*;
import net.diaithir.druidry.ars.common.block.LightBlock;
import net.diaithir.druidry.ars.common.block.tile.*;
import net.diaithir.druidry.ars.common.items.MobJarItem;
import net.diaithir.druidry.ars.common.items.ModBlockItem;
import net.diaithir.druidry.ars.common.items.RendererBlockItem;
import net.diaithir.druidry.ars.common.lib.LibBlockNames;
import net.diaithir.druidry.ars.common.world.tree.MagicTree;
import net.diaithir.druidry.ars.common.world.tree.SupplierBlockStateProvider;
import net.diaithir.druidry.ars.setup.reward.Rewards;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.diaithir.druidry.ars.setup.registry.ItemsRegistry.ITEMS;
import static net.diaithir.druidry.ars.setup.registry.ItemsRegistry.defaultItemProperties;
import static net.minecraft.world.level.block.state.properties.WoodType.register;

public class BlockRegistry {
    public static TagKey<Block> CASCADING_LOGS = BlockTags.create(ArsNouveau.prefix("cascading_logs"));
    public static TagKey<Block> FLOURISHING_LOGS = BlockTags.create(ArsNouveau.prefix("flourishing_logs"));
    public static TagKey<Block> VEXING_LOGS = BlockTags.create(ArsNouveau.prefix("vexing_logs"));
    public static TagKey<Block> BLAZING_LOGS = BlockTags.create(ArsNouveau.prefix("blazing_logs"));


    public static Block.Properties woodProp = BlockBehaviour.Properties.of().strength(2.0F, 3.0F).ignitedByLava().mapColor(MapColor.WOOD).sound(SoundType.WOOD);
    public static final WoodType ARCHWOOD = register(new WoodType("ars_nouveau:archwood", BlockSetType.OAK));

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, ArsNouveau.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ArsNouveau.MODID);
    public static final DeferredRegister<BlockStateProviderType<?>> BS_PROVIDERS = DeferredRegister.create(BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE, ArsNouveau.MODID);
    public static final Holder<BlockStateProviderType<?>> stateProviderType = BS_PROVIDERS.register(LibBlockNames.STATE_PROVIDER, () -> new BlockStateProviderType<>(SupplierBlockStateProvider.CODEC));

    public static BlockBehaviour.Properties LOG_PROP = BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F, 3.0F).ignitedByLava().sound(SoundType.WOOD);
    public static BlockBehaviour.Properties SAP_PROP = BlockBehaviour.Properties.of().noCollision().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY);

    public static BlockRegistryWrapper<LightBlock> LIGHT_BLOCK = registerBlock(LibBlockNames.LIGHT_BLOCK, LightBlock::new);
    public static BlockEntityTypeRegistryWrapper<LightTile> LIGHT_TILE = registerTile(LibBlockNames.LIGHT_BLOCK, LightTile::new, LIGHT_BLOCK);






    public static BlockRegistryWrapper<SourceJar> SOURCE_JAR = registerBlockAndItem(LibBlockNames.SOURCE_JAR, SourceJar::new);
    public static BlockEntityTypeRegistryWrapper<SourceJarTile> SOURCE_JAR_TILE = registerTile(LibBlockNames.SOURCE_JAR, SourceJarTile::new, SOURCE_JAR);

    public static BlockRegistryWrapper<ScribesBlock> SCRIBES_BLOCK = registerBlockAndItem(LibBlockNames.SCRIBES_BLOCK, ScribesBlock::new, (reg) -> new RendererBlockItem(reg, defaultItemProperties()));
    public static BlockEntityTypeRegistryWrapper<ScribesTile> SCRIBES_TABLE_TILE = registerTile(LibBlockNames.SCRIBES_BLOCK, ScribesTile::new, SCRIBES_BLOCK);



    public static BlockRegistryWrapper<SaplingBlock> CASCADING_SAPLING = registerBlockAndItem(LibBlockNames.CASCADING_SAPLING, () -> new SaplingBlock(MagicTree.getGrower("cascading_tree", WorldgenRegistry.CONFIGURED_CASCADING_TREE), SAP_PROP));

    public static BlockRegistryWrapper<SaplingBlock> BLAZING_SAPLING = registerBlockAndItem(LibBlockNames.BLAZING_SAPLING, () -> new SaplingBlock(MagicTree.getGrower("blazing_tree", WorldgenRegistry.CONFIGURED_BLAZING_TREE), SAP_PROP));
    public static BlockRegistryWrapper<SaplingBlock> VEXING_SAPLING = registerBlockAndItem(LibBlockNames.VEXING_SAPLING, () -> new SaplingBlock(MagicTree.getGrower("vexing_tree", WorldgenRegistry.CONFIGURED_VEXING_TREE), SAP_PROP));
    public static BlockRegistryWrapper<SaplingBlock> FLOURISHING_SAPLING = registerBlockAndItem(LibBlockNames.FLOURISHING_SAPLING, () -> new SaplingBlock(MagicTree.getGrower("flourishing_tree", WorldgenRegistry.CONFIGURED_FLOURISHING_TREE), SAP_PROP));
    public static BlockRegistryWrapper<ModBlock> ARCHWOOD_PLANK = registerBlockAndItem(LibBlockNames.ARCHWOOD_PLANK, () -> new ModBlock(LOG_PROP));
    public static BlockRegistryWrapper<ButtonBlock> ARCHWOOD_BUTTON = registerBlockAndItem(LibBlockNames.ARCHWOOD_BUTTON, () -> new ButtonBlock(BlockSetType.OAK, 30, BlockBehaviour.Properties.of().noCollision().strength(0.5F).sound(SoundType.WOOD)));
    public static BlockRegistryWrapper<StairBlock> ARCHWOOD_STAIRS = registerBlockAndItem(LibBlockNames.ARCHWOOD_STAIRS, () -> new StairBlock(ARCHWOOD_PLANK.defaultBlockState(), woodProp));
    public static BlockRegistryWrapper<SlabBlock> ARCHWOOD_SLABS = registerBlockAndItem(LibBlockNames.ARCHWOOD_SLABS, () -> new SlabBlock(woodProp));
    public static BlockRegistryWrapper<FenceGateBlock> ARCHWOOD_FENCE_GATE = registerBlockAndItem(LibBlockNames.ARCHWOOD_FENCE_GATE, () -> new FenceGateBlock(ARCHWOOD, woodProp.forceSolidOn()));
    public static BlockRegistryWrapper<TrapDoorBlock> ARCHWOOD_TRAPDOOR = registerBlockAndItem(LibBlockNames.ARCHWOOD_TRAPDOOR, () -> new TrapDoorBlock(BlockSetType.OAK, woodProp));

    public static BlockRegistryWrapper<PressurePlateBlock> ARCHWOOD_PPlate = registerBlockAndItem(LibBlockNames.ARCHWOOD_PRESSURE_PLATE, () -> new PressurePlateBlock(BlockSetType.OAK, woodProp.forceSolidOn()));
    public static BlockRegistryWrapper<FenceBlock> ARCHWOOD_FENCE = registerBlockAndItem(LibBlockNames.ARCHWOOD_FENCE, () -> new FenceBlock(woodProp));
    public static BlockRegistryWrapper<DoorBlock> ARCHWOOD_DOOR = registerBlockAndItem(LibBlockNames.ARCHWOOD_DOOR, () -> new DoorBlock(BlockSetType.OAK, woodProp));
    public static BlockRegistryWrapper<RotatedPillarBlock> STRIPPED_AWLOG_BLUE = registerBlockAndItem(LibBlockNames.STRIPPED_AWLOG_BLUE, () -> new RotatedPillarBlock(LOG_PROP));
    public static BlockRegistryWrapper<RotatedPillarBlock> STRIPPED_AWWOOD_BLUE = registerBlockAndItem(LibBlockNames.STRIPPED_AWWOOD_BLUE, () -> new RotatedPillarBlock(LOG_PROP));
    public static BlockRegistryWrapper<RotatedPillarBlock> STRIPPED_AWLOG_GREEN = registerBlockAndItem(LibBlockNames.STRIPPED_AWLOG_GREEN, () -> new RotatedPillarBlock(LOG_PROP));
    public static BlockRegistryWrapper<RotatedPillarBlock> STRIPPED_AWWOOD_GREEN = registerBlockAndItem(LibBlockNames.STRIPPED_AWWOOD_GREEN, () -> new RotatedPillarBlock(LOG_PROP));
    public static BlockRegistryWrapper<RotatedPillarBlock> STRIPPED_AWLOG_RED = registerBlockAndItem(LibBlockNames.STRIPPED_AWLOG_RED, () -> new RotatedPillarBlock(LOG_PROP));
    public static BlockRegistryWrapper<RotatedPillarBlock> STRIPPED_AWWOOD_RED = registerBlockAndItem(LibBlockNames.STRIPPED_AWWOOD_RED, () -> new RotatedPillarBlock(LOG_PROP));
    public static BlockRegistryWrapper<RotatedPillarBlock> STRIPPED_AWLOG_PURPLE = registerBlockAndItem(LibBlockNames.STRIPPED_AWLOG_PURPLE, () -> new RotatedPillarBlock(LOG_PROP));
    public static BlockRegistryWrapper<RotatedPillarBlock> STRIPPED_AWWOOD_PURPLE = registerBlockAndItem(LibBlockNames.STRIPPED_AWWOOD_PURPLE, () -> new RotatedPillarBlock(LOG_PROP));
    public static BlockRegistryWrapper<ModBlock> SOURCE_GEM_BLOCK = registerBlockAndItem(LibBlockNames.SOURCE_GEM_BLOCK, () -> new ModBlock(ModBlock.defaultProperties().noOcclusion().lightLevel(s -> 6)));
    public static BlockRegistryWrapper<SconceBlock> GOLD_SCONCE_BLOCK = registerBlockAndItem(LibBlockNames.SCONCE, SconceBlock::new);
    public static BlockRegistryWrapper<SconceBlock> SOURCESTONE_SCONCE_BLOCK = registerBlockAndItem(LibBlockNames.SOURCESTONE_SCONCE, SconceBlock::new);
    public static BlockRegistryWrapper<SconceBlock> POLISHED_SCONCE_BLOCK = registerBlockAndItem(LibBlockNames.POLISHED_SCONCE, SconceBlock::new);
    public static BlockRegistryWrapper<SconceBlock> ARCHWOOD_SCONCE_BLOCK = registerBlockAndItem(LibBlockNames.ARCHWOOD_SCONCE, SconceBlock::new);


    public static BlockEntityTypeRegistryWrapper<SconceTile> SCONCE_TILE = new BlockEntityTypeRegistryWrapper<>(BLOCK_ENTITIES.register(LibBlockNames.SCONCE, () -> new BlockEntityType<>(SconceTile::new, GOLD_SCONCE_BLOCK.get(), SOURCESTONE_SCONCE_BLOCK.get(), POLISHED_SCONCE_BLOCK.get(), ARCHWOOD_SCONCE_BLOCK.get())));


    public static BlockRegistryWrapper<BasicSpellTurret> BASIC_SPELL_TURRET = registerBlockAndItem(LibBlockNames.BASIC_SPELL_TURRET, BasicSpellTurret::new, (reg) -> new RendererBlockItem(reg, defaultItemProperties()));
    public static BlockEntityTypeRegistryWrapper<BasicSpellTurretTile> BASIC_SPELL_TURRET_TILE = registerTile(LibBlockNames.BASIC_SPELL_TURRET, BasicSpellTurretTile::new, BASIC_SPELL_TURRET);



    public static BlockRegistryWrapper<ScryerCrystal> SCRYERS_CRYSTAL = registerBlockAndItem(LibBlockNames.SCRYERS_CRYSTAL, ScryerCrystal::new);
    public static BlockEntityTypeRegistryWrapper<ScryerCrystalTile> SCRYER_CRYSTAL_TILE = registerTile(LibBlockNames.SCRYERS_CRYSTAL, ScryerCrystalTile::new, SCRYERS_CRYSTAL);
    public static BlockRegistryWrapper<MobJar> MOB_JAR = registerBlockAndItem(LibBlockNames.MOB_JAR, MobJar::new, (reg) -> new MobJarItem(reg.get(), defaultItemProperties()));
    public static BlockEntityTypeRegistryWrapper<MobJarTile> MOB_JAR_TILE = registerTile(LibBlockNames.MOB_JAR, MobJarTile::new, MOB_JAR);
    public static BlockRegistryWrapper<ModBlock> MAGEBLOOM_BLOCK = registerBlockAndItem(LibBlockNames.MAGEBLOOM_BLOCK, () -> new ModBlock(BlockBehaviour.Properties.of().strength(0.1F).sound(SoundType.WOOL)));




    public static final BlockRegistryWrapper<RitualBrazierBlock> RITUAL_BLOCK = registerBlockAndItem(LibBlockNames.RITUAL_BRAZIER, RitualBrazierBlock::new);
    public static final BlockRegistryWrapper<ModBlock> SOURCEBERRY_SACK = registerBlockAndItem(LibBlockNames.SOURCEBERRY_SACK, () -> new ModBlock(BlockBehaviour.Properties.of().strength(0.1F).sound(SoundType.WOOL)));
    public static final BlockRegistryWrapper<StandingSignBlock> ARCHWOOD_SIGN = registerBlock(LibBlockNames.ARCHWOOD_SIGN, () -> new StandingSignBlock(ARCHWOOD, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN)));
    public static final BlockRegistryWrapper<CeilingHangingSignBlock> ARCHWOOD_HANGING_SIGN = registerBlock(LibBlockNames.ARCHWOOD_HANGING_SIGN, () -> new CeilingHangingSignBlock(ARCHWOOD, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN)));
    public static final BlockRegistryWrapper<WallSignBlock> ARCHWOOD_WALL_SIGN = registerBlock(LibBlockNames.ARCHWOOD_WALL_SIGN, () -> new WallSignBlock(ARCHWOOD, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_SIGN)));
    public static final BlockRegistryWrapper<WallHangingSignBlock> ARCHWOOD_HANGING_WALL_SIGN = registerBlock(LibBlockNames.ARCHWOOD_HANGING_WALL_SIGN, () -> new WallHangingSignBlock(ARCHWOOD, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN)));



    public static final BlockEntityTypeRegistryWrapper<RitualBrazierTile> RITUAL_TILE = registerTile(LibBlockNames.RITUAL_BRAZIER, RitualBrazierTile::new, RITUAL_BLOCK);



    public static void onBlocksRegistry() {
        for (String s : LibBlockNames.DECORATIVE_SOURCESTONE) {
            if (LibBlockNames.DIRECTIONAL_SOURCESTONE.contains(s)) {
                BLOCKS.register(s, () -> new DirectionalModBlock());
            } else {
                BLOCKS.register(s, () -> new ModBlock());
            }
        }
        for (String s : LibBlockNames.DECORATIVE_SLABS) {
            BLOCKS.register(s, () -> new SlabBlock(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).sound(SoundType.STONE)));
        }
        for (String s : LibBlockNames.DECORATIVE_SOURCESTONE) {
            BLOCKS.register(s + "_stairs", () -> new StairBlock(BuiltInRegistries.BLOCK.getValue(ArsNouveau.prefix(s)).defaultBlockState(), BlockBehaviour.Properties.of().strength(1.5F, 6.0F).sound(SoundType.STONE)));
        }
        BLOCKS.register(LibBlockNames.Pot(LibBlockNames.BLAZING_SAPLING), () -> createPottedBlock(() -> BLAZING_SAPLING.get()));
        BLOCKS.register(LibBlockNames.Pot(LibBlockNames.CASCADING_SAPLING), () -> createPottedBlock(() -> CASCADING_SAPLING.get()));
        BLOCKS.register(LibBlockNames.Pot(LibBlockNames.FLOURISHING_SAPLING), () -> createPottedBlock(() -> FLOURISHING_SAPLING.get()));
        BLOCKS.register(LibBlockNames.Pot(LibBlockNames.VEXING_SAPLING), () -> createPottedBlock(() -> VEXING_SAPLING.get()));

    }



    public static void onBlockItemsRegistry() {
        for (String s : LibBlockNames.DECORATIVE_SOURCESTONE) {
            ItemsRegistry.register(s, () -> getDefaultBlockItem(BuiltInRegistries.BLOCK.getValue(ArsNouveau.prefix(s))));
        }

        for (String s : LibBlockNames.DECORATIVE_STAIRS) {
            ItemsRegistry.register(s, () -> getDefaultBlockItem(BuiltInRegistries.BLOCK.getValue(ArsNouveau.prefix(s))));
        }

        for (String s : LibBlockNames.DECORATIVE_SLABS) {
            ItemsRegistry.register(s, () -> getDefaultBlockItem(BuiltInRegistries.BLOCK.getValue(ArsNouveau.prefix(s))));
        }
    }

    public static ModBlockItem getDefaultBlockItem(Block block) {
        return new ModBlockItem(block, defaultItemProperties());
    }

    private static Boolean allowsSpawnOnLeaves(BlockState state, BlockGetter reader, BlockPos pos, EntityType<?> entity) {
        return entity == EntityType.OCELOT || entity == EntityType.PARROT;
    }

    private static boolean isntSolid(BlockState state, BlockGetter reader, BlockPos pos) {
        return false;
    }


    public static Block getBlock(String s) {
        return BuiltInRegistries.BLOCK.getValue(ArsNouveau.prefix(s));
    }

    public static <T extends Block> BlockRegistryWrapper<T> registerBlock(String name, Supplier<T> blockSupp) {
        return new BlockRegistryWrapper<>(BLOCKS.register(name, blockSupp));
    }

    public static <T extends Block> BlockRegistryWrapper<T> registerBlockAndItem(String name, Supplier<T> blockSupp) {
        BlockRegistryWrapper<T> blockReg = new BlockRegistryWrapper<>(BLOCKS.register(name, blockSupp));
        ItemsRegistry.register(name, () -> getDefaultBlockItem(blockReg.get()));
        return blockReg;
    }

    public static <T extends Block> BlockRegistryWrapper<T> registerBlockAndItem(String name, Supplier<T> blockSupp, Function<BlockRegistryWrapper<T>, Item> blockItemFunc) {
        BlockRegistryWrapper<T> blockReg = new BlockRegistryWrapper<>(BLOCKS.register(name, blockSupp));
        ItemsRegistry.register(name, () -> blockItemFunc.apply(blockReg));
        return blockReg;
    }

    public static final Map<Supplier<Identifier>, FlowerPotBlock> flowerPots = new HashMap<>();

    public static FlowerPotBlock createPottedBlock(Supplier<? extends Block> block) {
        FlowerPotBlock pot = new FlowerPotBlock(block.get(), BlockBehaviour.Properties.of().instabreak().noOcclusion());
        flowerPots.put(() -> BuiltInRegistries.BLOCK.getKey(block.get()), pot);
        return pot;
    }

    public static <T extends BlockEntity> BlockEntityTypeRegistryWrapper<T> registerTile(String regName, BlockEntityType.BlockEntitySupplier<T> tile, BlockRegistryWrapper<? extends Block> block) {
        return new BlockEntityTypeRegistryWrapper<>(BLOCK_ENTITIES.register(regName, () -> new BlockEntityType<>(tile, block.get())));
    }
}
