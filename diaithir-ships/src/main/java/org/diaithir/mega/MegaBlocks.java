package org.diaithir.mega;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.diaithir.mega.cleanroom.DrillHead;
import org.diaithir.mega.cleanroom.TrackDrive;

/**
 * Blocks and items that put the survey-mod classes in the world, registered beside the cab in MegaMod (no new jar).
 * Gantry blocks use mega.gantry (Aeronautics rules, MIT), laser drill uses mega.LaserDrill (Industrial Foregoing, MIT),
 * quarry/drill heads/track command are clean-room (mega.cleanroom).
 */
public final class MegaBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MegaMod.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MegaMod.MOD_ID);

    public static final DeferredBlock<GantryBlocks.Directional> GANTRY_SHAFT = BLOCKS.registerBlock("gantry_shaft", GantryBlocks.Directional::new,
            p -> p.mapColor(MapColor.METAL).strength(3f, 9f).sound(SoundType.METAL));
    public static final DeferredBlock<GantryBlocks.Directional> GANTRY_CARRIAGE = BLOCKS.registerBlock("gantry_carriage", GantryBlocks.Directional::new,
            p -> p.mapColor(MapColor.METAL).strength(3f, 9f).sound(SoundType.METAL));
    public static final DeferredBlock<GantryBlocks.Assembler> GANTRY_ASSEMBLER = BLOCKS.registerBlock("gantry_assembler", GantryBlocks.Assembler::new,
            p -> p.mapColor(MapColor.METAL).strength(3f, 9f).sound(SoundType.METAL));
    public static final DeferredBlock<QuarryBlock> QUARRY = BLOCKS.registerBlock("quarry", QuarryBlock::new,
            p -> p.mapColor(MapColor.METAL).strength(3f, 9f).sound(SoundType.METAL));
    public static final DeferredBlock<LaserDrillBlock> LASER_DRILL = BLOCKS.registerBlock("laser_drill", LaserDrillBlock::new,
            p -> p.mapColor(MapColor.METAL).strength(3f, 9f).sound(SoundType.METAL));

    public static final DeferredItem<BlockItem> GANTRY_SHAFT_ITEM = ITEMS.registerSimpleBlockItem(GANTRY_SHAFT);
    public static final DeferredItem<BlockItem> GANTRY_CARRIAGE_ITEM = ITEMS.registerSimpleBlockItem(GANTRY_CARRIAGE);
    public static final DeferredItem<BlockItem> GANTRY_ASSEMBLER_ITEM = ITEMS.registerSimpleBlockItem(GANTRY_ASSEMBLER);
    public static final DeferredItem<BlockItem> QUARRY_ITEM = ITEMS.registerSimpleBlockItem(QUARRY);
    public static final DeferredItem<BlockItem> LASER_DRILL_ITEM = ITEMS.registerSimpleBlockItem(LASER_DRILL);

    public static final Map<DrillHead, DeferredItem<Item>> HEADS = new EnumMap<>(DrillHead.class);
    static {
        for (DrillHead h : DrillHead.values()) HEADS.put(h, ITEMS.registerSimpleItem("drill_head_" + h.name().toLowerCase()));
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QuarryBlockEntity>> QUARRY_BE = BES.register("quarry",
            () -> new BlockEntityType<>(QuarryBlockEntity::new, QUARRY.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LaserDrillBlockEntity>> LASER_BE = BES.register("laser_drill",
            () -> new BlockEntityType<>(LaserDrillBlockEntity::new, LASER_DRILL.get()));

    private MegaBlocks() {}

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BES.register(bus);
        NeoForge.EVENT_BUS.addListener(MegaBlocks::commands);
        if (Boolean.getBoolean("megamachines.selftest")) NeoForge.EVENT_BUS.register(new MegaBlocksSelfTest());
    }

    public static DrillHead headOf(ItemStack s) {
        for (var e : HEADS.entrySet()) if (s.is(e.getValue().get())) return e.getKey();
        return null;
    }

    public static String itemId(ItemStack s) { return BuiltInRegistries.ITEM.getKey(s.getItem()).toString(); }

    /** /megamachines tracks SPEC AREA_M2: ground pressure, top speed and grade for a catalog machine on tracks (clean-room TrackDrive). */
    private static void commands(RegisterCommandsEvent ev) {
        ev.getDispatcher().register(Commands.literal("megamachines").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("tracks").then(Commands.argument("id", StringArgumentType.string())
                        .then(Commands.argument("area_m2", DoubleArgumentType.doubleArg(0.1)).executes(c -> {
                            var src = c.getSource();
                            MegaRates.Row r = MegaRates.get(StringArgumentType.getString(c, "id"));
                            if (r == null) { src.sendFailure(Component.literal("Unknown catalog id")); return 0; }
                            double area = DoubleArgumentType.getDouble(c, "area_m2");
                            TrackDrive t = trackDrive(r, area);
                            src.sendSuccess(() -> Component.literal(String.format("%s: %.0f kPa, top %.1f m/s, grade %.0f%%", r.id(),
                                    t.groundPressureKPa(), t.topSpeed(), t.maxGradient() * 100)), false);
                            return 1;
                        })))));
    }

    /** Track drive for a catalog machine: mass and power from the catalog, gauge 0.2 x sqrt(area), soil-grade traction and rolling factors. */
    public static TrackDrive trackDrive(MegaRates.Row r, double areaM2) {
        return new TrackDrive(r.massT() * 1000.0, r.powerMW() * 1e6, areaM2, 0.2 * Math.sqrt(areaM2) + 1.0, 0.6, 0.10);
    }
}
