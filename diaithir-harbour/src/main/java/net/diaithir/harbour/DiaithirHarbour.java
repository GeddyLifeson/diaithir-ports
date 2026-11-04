package net.diaithir.harbour;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
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

    public DiaithirHarbour(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) { e.accept(BOLLARD_ITEM.get()); e.accept(OFFICE_ITEM.get()); }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> e.getDispatcher().register(Commands.literal("diaithirharbour")
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
}
