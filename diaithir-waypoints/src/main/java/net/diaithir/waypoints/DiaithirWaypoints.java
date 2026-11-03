package net.diaithir.waypoints;

import java.util.List;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;

@Mod(DiaithirWaypoints.ID)
public final class DiaithirWaypoints {
    public static final String ID = "diaithirwaypoints";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACH = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ID);

    public static final DeferredBlock<WaypointStoneBlock> STONE = BLOCKS.registerBlock("waypoint_stone", WaypointStoneBlock::new,
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(3.0F, 1200.0F).requiresCorrectToolForDrops().lightLevel(s -> 6));
    public static final DeferredItem<BlockItem> STONE_ITEM = ITEMS.registerSimpleBlockItem(STONE);
    public static final DeferredItem<ScrollItem> SCROLL = ITEMS.registerItem("waypoint_scroll", ScrollItem::new, () -> new Item.Properties().stacksTo(16));

    /** The waypoints a player has lit, newest last. Survives death. */
    public static final Supplier<AttachmentType<List<Known>>> KNOWN = ATTACH.register("known",
        () -> AttachmentType.<List<Known>>builder(() -> List.of()).serialize(Known.LIST.fieldOf("v")).copyOnDeath().build());

    public DiaithirWaypoints(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ATTACH.register(bus);
        bus.addListener((net.neoforged.fml.event.lifecycle.InterModEnqueueEvent e) -> e.enqueueWork(() ->
            net.neoforged.fml.InterModComms.sendTo("diaithirlookat", "block/" + ID + ":waypoint_stone", () ->
                (java.util.function.BiFunction<net.minecraft.world.level.Level, net.minecraft.core.BlockPos, java.util.List<String>>)
                    (level, pos) -> java.util.List.of(Names.of(pos)))));
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> Travel.commands(e.getDispatcher()));
    }
}
