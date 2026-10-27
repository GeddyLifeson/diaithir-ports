package net.diaithir.furnishings;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(DiaithirFurnishings.ID)
public final class DiaithirFurnishings {
    public static final String ID = "diaithirfurnishings";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    public static final DeferredBlock<RopeBlock> ROPE = BLOCKS.registerBlock("rope", RopeBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).sound(SoundType.WOOL).strength(0.2F)
            .noCollision().noOcclusion().pushReaction(PushReaction.DESTROY).ignitedByLava());
    public static final DeferredBlock<SackBlock> SACK = BLOCKS.registerBlock("sack", SackBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.SAND).sound(SoundType.WOOL).strength(0.8F)
            .noOcclusion().pushReaction(PushReaction.DESTROY).ignitedByLava());
    public static final DeferredBlock<SconceBlock> SCONCE = BLOCKS.registerBlock("sconce", SconceBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.LANTERN).strength(1.0F)
            .noCollision().noOcclusion().lightLevel(s -> 14).pushReaction(PushReaction.DESTROY));

    public static final DeferredItem<BlockItem> ROPE_ITEM = ITEMS.registerSimpleBlockItem(ROPE);
    public static final DeferredItem<BlockItem> SACK_ITEM = ITEMS.registerSimpleBlockItem(SACK);
    public static final DeferredItem<BlockItem> SCONCE_ITEM = ITEMS.registerSimpleBlockItem(SCONCE);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SackEntity>> SACK_ENTITY = ENTITIES.register("sack",
        () -> new BlockEntityType<>(SackEntity::new, SACK.get()));

    public static final DeferredBlock<JarBlock> CLAY_JAR = BLOCKS.registerBlock("clay_jar", JarBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).sound(SoundType.DECORATED_POT).strength(0.6F)
            .noOcclusion().pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<JarBlock> GLASS_JAR = BLOCKS.registerBlock("glass_jar", JarBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.NONE).sound(SoundType.GLASS).strength(0.3F)
            .noOcclusion().pushReaction(PushReaction.DESTROY));
    public static final DeferredItem<BlockItem> CLAY_JAR_ITEM = ITEMS.registerSimpleBlockItem(CLAY_JAR);
    public static final DeferredItem<BlockItem> GLASS_JAR_ITEM = ITEMS.registerSimpleBlockItem(GLASS_JAR);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<JarEntity>> JAR_ENTITY = ENTITIES.register("jar",
        () -> new BlockEntityType<>(JarEntity::new, CLAY_JAR.get(), GLASS_JAR.get()));

    public static final List<DeferredBlock<SignpostBlock>> SIGNPOSTS = new ArrayList<>();
    static {
        for (Woods.Wood w : Woods.ALL) {
            DeferredBlock<SignpostBlock> b = BLOCKS.registerBlock(w.id() + "_signpost", SignpostBlock::new,
                () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(1.5F)
                    .noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY));
            ITEMS.registerSimpleBlockItem(b);
            SIGNPOSTS.add(b);
        }
    }
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SignpostEntity>> SIGNPOST_ENTITY = ENTITIES.register("signpost",
        () -> new BlockEntityType<>(SignpostEntity::new, SIGNPOSTS.stream().map(DeferredBlock::get).toArray(net.minecraft.world.level.block.Block[]::new)));

    public DiaithirFurnishings(IEventBus bus) {
        bus.addListener(SetArmText::register);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
    }
}
