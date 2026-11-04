package org.diaithir.mega;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.diaithir.mega.site.*;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Mega construction and extraction vehicles, inside diaithir-ships (no new jar). Design: MEGAMACHINES.md. */
public final class MegaMod {
    public static final String MOD_ID = "megamachines";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);

    public static final DeferredBlock<MegaCabBlock> CAB = BLOCKS.registerBlock("cab", MegaCabBlock::new,
            p -> p.mapColor(MapColor.METAL).strength(3f, 9f).sound(SoundType.METAL));
    public static final DeferredItem<BlockItem> CAB_ITEM = ITEMS.registerSimpleBlockItem(CAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MegaCabBlockEntity>> CAB_BE = BLOCK_ENTITIES.register("cab",
            () -> new BlockEntityType<>(MegaCabBlockEntity::new, CAB.get()));

    public static final DeferredBlock<DigHeadBlock> DIG_HEAD = BLOCKS.registerBlock("dig_head", DigHeadBlock::new,
            p -> p.mapColor(MapColor.METAL).strength(5f, 12f).sound(SoundType.METAL));
    public static final DeferredItem<BlockItem> DIG_HEAD_ITEM = ITEMS.registerSimpleBlockItem(DIG_HEAD);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DigHeadBlockEntity>> DIG_HEAD_BE = BLOCK_ENTITIES.register("dig_head",
            () -> new BlockEntityType<>(DigHeadBlockEntity::new, DIG_HEAD.get()));
    public static final DeferredBlock<SiteOfficeBlock> OFFICE = BLOCKS.registerBlock("site_office", SiteOfficeBlock::new,
            p -> p.mapColor(MapColor.WOOD).strength(2f, 6f).sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> OFFICE_ITEM = ITEMS.registerSimpleBlockItem(OFFICE);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SiteOfficeBlockEntity>> OFFICE_BE = BLOCK_ENTITIES.register("site_office",
            () -> new BlockEntityType<>(SiteOfficeBlockEntity::new, OFFICE.get()));
    public static final DeferredBlock<DepotBlock> DEPOT = BLOCKS.registerBlock("depot", DepotBlock::new,
            p -> p.mapColor(MapColor.METAL).strength(2f, 6f).sound(SoundType.METAL));
    public static final DeferredItem<BlockItem> DEPOT_ITEM = ITEMS.registerSimpleBlockItem(DEPOT);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DepotBlockEntity>> DEPOT_BE = BLOCK_ENTITIES.register("depot",
            () -> new BlockEntityType<>(DepotBlockEntity::new, DEPOT.get()));

    private MegaMod() {}

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        bus.addListener(RegisterCapabilitiesEvent.class, e -> e.registerBlockEntity(Capabilities.Item.BLOCK, DEPOT_BE.get(), (be, side) -> be.intake));
        NeoForge.EVENT_BUS.addListener(MegaMod::commands);
        if (Boolean.getBoolean("megamachines.selftest")) {
            System.setProperty("megamachines.timescale", System.getProperty("megamachines.timescale", "20000"));
            NeoForge.EVENT_BUS.register(new MegaSelfTest());
        }
    }

    private static void commands(RegisterCommandsEvent ev) {
        ev.getDispatcher().register(Commands.literal("megamachines").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("setspec").then(Commands.argument("id", StringArgumentType.string())
                        .executes(c -> {
                            var src = c.getSource();
                            String id = StringArgumentType.getString(c, "id");
                            BlockPos pos = BlockPos.containing(src.getPosition()).below();
                            if (MegaCatalog.get(id) == null || !(src.getLevel().getBlockEntity(pos) instanceof MegaCabBlockEntity cab)) {
                                src.sendFailure(Component.literal("Stand on a mega cab and give a catalog id"));
                                return 0;
                            }
                            cab.setSpec(id);
                            src.sendSuccess(() -> Component.literal("Cab set to " + id), true);
                            return 1;
                        })))
                .then(Commands.literal("site")
                        .then(Commands.literal("tier").then(Commands.argument("age", StringArgumentType.word()).executes(c -> site(c.getSource(), o -> o.setTier(StringArgumentType.getString(c, "age"))))))
                        .then(Commands.literal("start").then(Commands.argument("id", StringArgumentType.string()).executes(c -> site(c.getSource(), o -> o.start(StringArgumentType.getString(c, "id"))))))
                        .then(Commands.literal("builders").then(Commands.argument("n", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, 64)).executes(c -> site(c.getSource(), o -> { o.setBuilders(com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "n")); return true; }))))
                        .then(Commands.literal("paid").then(Commands.argument("v", com.mojang.brigadier.arguments.BoolArgumentType.bool()).executes(c -> site(c.getSource(), o -> { o.setPaid(com.mojang.brigadier.arguments.BoolArgumentType.getBool(c, "v")); return true; }))))));
    }

    /** Runs an action on the site office the sender stands on. */
    private static int site(net.minecraft.commands.CommandSourceStack src, java.util.function.Predicate<SiteOfficeBlockEntity> act) {
        BlockPos pos = BlockPos.containing(src.getPosition()).below();
        if (!(src.getLevel().getBlockEntity(pos) instanceof SiteOfficeBlockEntity o)) { src.sendFailure(Component.literal("Stand on a site office")); return 0; }
        boolean ok = act.test(o);
        if (!ok) src.sendFailure(Component.literal("Refused: office too old, unknown id, or a site is already running"));
        return ok ? 1 : 0;
    }
}
