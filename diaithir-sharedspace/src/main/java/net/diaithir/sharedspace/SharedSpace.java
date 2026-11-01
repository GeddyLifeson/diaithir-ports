package net.diaithir.sharedspace;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Lets several small blocks share one block space. Clean-room: written from how the idea behaves in play and from the
 * tag and lang names the original mod ships, no code or art taken from it. Inspired by Olaf's Relatively Placed Blocks
 * by LukeolafP (CC0-1.0 / Apache-2.0).
 */
@Mod(SharedSpace.ID)
public final class SharedSpace {
    public static final String ID = "diaithirsharedspace";

    /** Blocks that never share a space, on top of the size test (pots with plants, redstone dust, pressure plates). */
    public static final TagKey<Block> DENIED = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ID, "denied"));
    /** Blocks that always count as small even when their shape is a little wider than half a block. */
    public static final TagKey<Block> ALLOWED = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ID, "allowed"));

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    public static final DeferredBlock<ClusterBlock> CLUSTER = BLOCKS.registerBlock("cluster", ClusterBlock::new,
        () -> BlockBehaviour.Properties.of().sound(SoundType.LANTERN).strength(0.3F).noOcclusion()
            .pushReaction(PushReaction.DESTROY).lightLevel(s -> s.getValue(ClusterBlock.LIGHT)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClusterBlockEntity>> CLUSTER_ENTITY = ENTITIES.register("cluster",
        () -> new BlockEntityType<>(ClusterBlockEntity::new, CLUSTER.get()));

    public SharedSpace(IEventBus bus) {
        BLOCKS.register(bus);
        ENTITIES.register(bus);
    }
}
