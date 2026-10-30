package net.diaithir.furnishings;

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

    public DiaithirFurnishings(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
    }
}
