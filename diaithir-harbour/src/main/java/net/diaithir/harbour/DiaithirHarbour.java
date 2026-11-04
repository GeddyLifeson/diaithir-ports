package net.diaithir.harbour;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Harbour works for boats. Clean-room original (MIT). */
@Mod(DiaithirHarbour.ID)
public final class DiaithirHarbour {
    public static final String ID = "diaithirharbour";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

    public static final DeferredBlock<Bollard> BOLLARD = BLOCKS.registerBlock("mooring_bollard", Bollard::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
    public static final DeferredItem<BlockItem> BOLLARD_ITEM = ITEMS.registerSimpleBlockItem(BOLLARD);

    public static final DeferredBlock<Office> OFFICE = BLOCKS.registerBlock("harbour_office", Office::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(3.0F, 6.0F).sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> OFFICE_ITEM = ITEMS.registerSimpleBlockItem(OFFICE);

    public static final DeferredItem<Item> CARGO_CRATE = ITEMS.registerSimpleItem("cargo_crate");

    public DiaithirHarbour(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        Cargo.ATTACHMENTS.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) { e.accept(BOLLARD_ITEM.get()); e.accept(OFFICE_ITEM.get()); e.accept(CARGO_CRATE.get()); }
        });
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract.class, DiaithirHarbour::interact);
        NeoForge.EVENT_BUS.addListener(EntityLeaveLevelEvent.class, e -> {
            if (e.getEntity() instanceof AbstractBoat b && b.level() instanceof ServerLevel l && b.getRemovalReason() != null && b.getRemovalReason().shouldDestroy()) Cargo.spill(l, b);
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> e.getDispatcher().register(Commands.literal("diaithirharbour")
                .then(Commands.literal("cargotest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    String line = CargoTest.run(c.getSource().getLevel());
                    c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return 1;
                }))
                .then(Commands.literal("age").requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                        .then(Commands.argument("age", IntegerArgumentType.integer(-1, 99)).executes(c -> {
                            int a = IntegerArgumentType.getInteger(c, "age");
                            Ages.override = a < 0 ? null : a;
                            c.getSource().sendSuccess(() -> Component.literal(a < 0 ? "Age override cleared" : "Harbour Age override " + a), false);
                            return 1;
                        })))));
        NeoForge.EVENT_BUS.addListener(EntityTickEvent.Pre.class, e -> {
            if (e.getEntity() instanceof AbstractBoat b && !b.level().isClientSide()) Bollard.hold(b);
        });
    }

    /** A crate used on a boat adds a hold row (up to the harbour office's limit); sneak with an empty hand opens the hold. */
    private static void interact(PlayerInteractEvent.EntityInteract e) {
        if (!(e.getTarget() instanceof AbstractBoat boat)) return;
        boolean crate = e.getItemStack().is(CARGO_CRATE.get());
        boolean open = e.getItemStack().isEmpty() && e.getEntity().isShiftKeyDown() && Cargo.crates(boat) > 0;
        if (!crate && !open) return;
        e.setCancellationResult(InteractionResult.SUCCESS);
        e.setCanceled(true);
        if (!(e.getLevel() instanceof ServerLevel level)) return;
        if (open) {
            Cargo.open(e.getEntity(), boat);
        } else if (Cargo.addCrate(boat, Office.crateLimit(level, boat.blockPosition()))) {
            if (!e.getEntity().isCreative()) e.getItemStack().shrink(1);
            e.getEntity().sendOverlayMessage(Component.literal("Cargo crate fitted: " + Cargo.crates(boat) + " of " + Office.crateLimit(level, boat.blockPosition()) + "."));
        } else {
            e.getEntity().sendOverlayMessage(Component.literal("This boat can carry no more crates here (a harbour office raises the limit)."));
        }
    }
}
