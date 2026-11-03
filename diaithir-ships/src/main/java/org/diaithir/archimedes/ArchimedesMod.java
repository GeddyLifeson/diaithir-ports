package org.diaithir.archimedes;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.diaithir.archimedes.block.BalloonBlock;
import org.diaithir.archimedes.block.CrateBlock;
import org.diaithir.archimedes.block.PlainBlock;
import org.diaithir.archimedes.block.ShipMarkerBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archimedes' Ships (Ckathode, MIT) ported to NeoForge 26.1.2 inside diaithir-ships. Block-built ships with a helm,
 * floaters, balloons, engines and crates, no Valkyrien Skies dependency.
 */
public final class ArchimedesMod {
    public static final String MOD_ID = "archimedes";
    public static final Logger LOG = LoggerFactory.getLogger("Archimedes");

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredBlock<ShipMarkerBlock> MARKER = BLOCKS.registerBlock("marker", ShipMarkerBlock::new,
            p -> p.mapColor(MapColor.METAL).strength(2f, 6f).sound(SoundType.COPPER));
    public static final DeferredBlock<PlainBlock> FLOATER = BLOCKS.registerBlock("floater", PlainBlock::new,
            p -> p.mapColor(MapColor.COLOR_ORANGE).strength(1f).sound(SoundType.WOOD));
    public static final DeferredBlock<PlainBlock> SEAT = BLOCKS.registerBlock("seat", PlainBlock::new,
            p -> p.mapColor(MapColor.COLOR_GRAY).strength(1f).sound(SoundType.WOOL));
    public static final DeferredBlock<PlainBlock> BUFFER = BLOCKS.registerBlock("buffer", PlainBlock::new,
            p -> p.mapColor(MapColor.COLOR_YELLOW).strength(1f).sound(SoundType.WOOD));
    public static final DeferredBlock<PlainBlock> ENGINE = BLOCKS.registerBlock("engine", PlainBlock::new,
            p -> p.mapColor(MapColor.METAL).strength(3f, 6f).sound(SoundType.IRON).requiresCorrectToolForDrops());
    public static final DeferredBlock<CrateBlock> CRATE = BLOCKS.registerBlock("crate", CrateBlock::new,
            p -> p.mapColor(MapColor.COLOR_BROWN).strength(1.5f).sound(SoundType.WOOD));
    public static final Map<DyeColor, DeferredBlock<BalloonBlock>> BALLOONS = new EnumMap<>(DyeColor.class);

    public static final DeferredItem<BlockItem> MARKER_ITEM = ITEMS.registerSimpleBlockItem(MARKER);
    public static final DeferredItem<BlockItem> FLOATER_ITEM = ITEMS.registerSimpleBlockItem(FLOATER);
    public static final DeferredItem<BlockItem> SEAT_ITEM = ITEMS.registerSimpleBlockItem(SEAT);
    public static final DeferredItem<BlockItem> BUFFER_ITEM = ITEMS.registerSimpleBlockItem(BUFFER);
    public static final DeferredItem<BlockItem> ENGINE_ITEM = ITEMS.registerSimpleBlockItem(ENGINE);
    public static final DeferredItem<BlockItem> CRATE_ITEM = ITEMS.registerSimpleBlockItem(CRATE);

    static {
        for (DyeColor c : DyeColor.values()) {
            var b = BLOCKS.registerBlock("balloon_" + c.getName(), BalloonBlock::new,
                    p -> p.mapColor(c.getMapColor()).strength(0.35f).sound(SoundType.WOOL));
            BALLOONS.put(c, b);
            ITEMS.registerSimpleBlockItem(b);
        }
    }

    public static final DeferredHolder<EntityType<?>, EntityType<ShipEntity>> SHIP = ENTITIES.register("ship",
            id -> EntityType.Builder.<ShipEntity>of(ShipEntity::new, MobCategory.MISC)
                    .sized(1f, 1f).clientTrackingRange(16).updateInterval(1).noSummon()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrateBlock.Entity>> CRATE_BE = BLOCK_ENTITIES.register("crate",
            () -> new BlockEntityType<>(CrateBlock.Entity::new, CRATE.get()));
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.archimedes"))
                    .icon(() -> new ItemStack(MARKER_ITEM.get()))
                    .displayItems((params, out) -> ITEMS.getEntries().forEach(e -> out.accept(e.get())))
                    .build());

    private ArchimedesMod() {}

    public static void init(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        BLOCK_ENTITIES.register(bus);
        TABS.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, ArchimedesConfig.SPEC, "archimedes-server.toml");
        NeoForge.EVENT_BUS.addListener(ArchimedesMod::commands);
        if (Boolean.getBoolean("archimedes.selftest")) NeoForge.EVENT_BUS.register(new ArchimedesSelfTest());
    }

    private static void commands(RegisterCommandsEvent ev) {
        ev.getDispatcher().register(Commands.literal("archimedes").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("disassemble")
                        .executes(c -> disassemble(c.getSource(), 8))
                        .then(Commands.argument("radius", DoubleArgumentType.doubleArg(1, 128))
                                .executes(c -> disassemble(c.getSource(), DoubleArgumentType.getDouble(c, "radius")))))
                .then(Commands.literal("info").executes(c -> {
                    var src = c.getSource();
                    var ships = src.getLevel().getEntitiesOfClass(ShipEntity.class, new AABB(src.getPosition(), src.getPosition()).inflate(64));
                    src.sendSuccess(() -> Component.literal(ships.size() + " ship(s) within 64 blocks"), false);
                    for (ShipEntity s : ships) {
                        var ch = s.chunk();
                        src.sendSuccess(() -> Component.literal(String.format("%s blocks=%d mass=%.1f floaters=%d balloons=%d engines=%d flying=%s",
                                s.blockPosition().toShortString(), ch.blocks.size(), ch.mass, ch.floaters, ch.balloons, ch.engines, s.isFlying())), false);
                    }
                    return ships.size();
                })));
    }

    private static int disassemble(net.minecraft.commands.CommandSourceStack src, double radius) {
        var ships = src.getLevel().getEntitiesOfClass(ShipEntity.class, new AABB(src.getPosition(), src.getPosition()).inflate(radius));
        ships.forEach(s -> s.disassemble(src.getLevel()));
        src.sendSuccess(() -> Component.literal("Disassembled " + ships.size() + " ship(s)"), true);
        return ships.size();
    }
}
