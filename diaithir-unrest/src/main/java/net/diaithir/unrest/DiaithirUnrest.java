package net.diaithir.unrest;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Unrest: a rolling per-chunk score from machine activity that replaces pollution (plans/FACTORIO_TECH.md 2f). Machines feed it through
 * the noisy_* block tags or {@link Unrest#add}; it spreads, decays, is read with the Meter and is absorbed by nests, which post
 * {@link NestWaveEvent} for the creature module. Clean-room: written from the plan only; no pollution mod was read.
 */
@Mod(DiaithirUnrest.ID)
public final class DiaithirUnrest {
    public static final String ID = "diaithirunrest";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    public static final DeferredBlock<MeterBlock> METER = BLOCKS.registerBlock("meter", MeterBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 6.0F).sound(SoundType.METAL));
    public static final DeferredBlock<NestBlock> NEST = BLOCKS.registerBlock("nest", NestBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(6.0F, 12.0F).sound(SoundType.MUD));
    public static final DeferredItem<BlockItem> METER_ITEM = ITEMS.registerSimpleBlockItem(METER);
    public static final DeferredItem<BlockItem> NEST_ITEM = ITEMS.registerSimpleBlockItem(NEST);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NestBlockEntity>> NEST_BE = BLOCK_ENTITIES.register("nest",
            () -> new BlockEntityType<>(NestBlockEntity::new, NEST.get()));

    static TagKey<Block> tag(String n) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ID, n));
    }

    public DiaithirUnrest(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) e.accept(METER_ITEM.get());
        });
        NeoForge.EVENT_BUS.addListener(LevelTickEvent.Post.class, e -> {
            if (!(e.getLevel() instanceof ServerLevel sl)) return;
            long t = sl.getGameTime();
            if (t % Config.SCAN_TICKS.getAsInt() == 0) scan(sl);
            if (t % Config.STEP_TICKS.getAsInt() == 0) {
                Unrest.step(sl);
                for (ServerPlayer p : sl.players()) {
                    ChunkPos c = p.chunkPosition();
                    int r = Config.NEST_RADIUS.getAsInt() + Config.SCAN_RADIUS.getAsInt();
                    for (int x = c.x() - r; x <= c.x() + r; x++)
                        for (int z = c.z() - r; z <= c.z() + r; z++) {
                            LevelChunk ch = sl.getChunkSource().getChunkNow(x, z);
                            if (ch == null) continue;
                            for (BlockEntity be : ch.getBlockEntities().values()) if (be instanceof NestBlockEntity n) n.step(sl);
                        }
                }
            }
        });
        NeoForge.EVENT_BUS.addListener(ExplosionEvent.Detonate.class, e -> {
            if (e.getLevel() instanceof ServerLevel sl) Unrest.add(sl, net.minecraft.core.BlockPos.containing(e.getExplosion().center()), e.getExplosion().radius() * Config.EXPLOSION.getAsDouble());
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> UnrestCommands.register(e.getDispatcher()));
    }

    /** Machines near players add their weight; quiet blocks take theirs away. Only block entities are counted, so the scan stays cheap. */
    static void scan(ServerLevel sl) {
        TagKey<Block> low = tag("noisy_low"), mid = tag("noisy_mid"), high = tag("noisy_high"), quiet = tag("quiet");
        int r = Config.SCAN_RADIUS.getAsInt();
        java.util.Set<Long> seen = new java.util.HashSet<>();
        for (ServerPlayer p : sl.players()) {
            ChunkPos c = p.chunkPosition();
            for (int x = c.x() - r; x <= c.x() + r; x++)
                for (int z = c.z() - r; z <= c.z() + r; z++) {
                    if (!seen.add(Field.key(x, z))) continue;
                    LevelChunk ch = sl.getChunkSource().getChunkNow(x, z);
                    if (ch == null) continue;
                    double sum = 0;
                    for (BlockEntity be : ch.getBlockEntities().values()) {
                        var st = be.getBlockState();
                        if (st.is(high)) sum += Config.HIGH.getAsDouble();
                        else if (st.is(mid)) sum += Config.MID.getAsDouble();
                        else if (st.is(low)) sum += Config.LOW.getAsDouble();
                        if (st.is(quiet)) sum -= Config.QUIET.getAsDouble();
                    }
                    if (sum != 0) Unrest.field(sl).add(x, z, sum);
                }
        }
    }
}
