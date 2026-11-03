package net.diaithir.crafting.foundry;

import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * Foundry parts of the automation and crafting module: the Operator Seat and the seven research packs that feed the Age gates
 * (see data/researcher research nodes in diaithir-data). Our own clean-room design.
 */
@Mod(DiaithirCrafting.ID)
public final class DiaithirCrafting {
    public static final String ID = "diaithircrafting";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    public static final DeferredBlock<OperatorSeatBlock> OPERATOR_SEAT = BLOCKS.registerBlock("foundry_operator_seat", OperatorSeatBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.5F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
                    .lightLevel(s -> s.getValue(OperatorSeatBlock.STAFFED) ? 7 : 0));
    public static final DeferredItem<BlockItem> OPERATOR_SEAT_ITEM = ITEMS.registerSimpleBlockItem(OPERATOR_SEAT);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OperatorSeatBlockEntity>> OPERATOR_SEAT_BE = BLOCK_ENTITIES.register("foundry_operator_seat",
            () -> new BlockEntityType<>(OperatorSeatBlockEntity::new, OPERATOR_SEAT.get()));

    /** Research packs 1 to 7, in the order the Age gates consume them. */
    public static final List<DeferredItem<Item>> PACKS = new ArrayList<>();
    static {
        for (int i = 1; i <= 7; i++) PACKS.add(ITEMS.registerSimpleItem("foundry_research_pack_" + i));
    }

    public DiaithirCrafting(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(RegisterCapabilitiesEvent.class, e ->
                e.registerBlockEntity(Capabilities.Item.BLOCK, OPERATOR_SEAT_BE.get(), (be, side) -> be.food.view()));
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) e.accept(OPERATOR_SEAT_ITEM.get());
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) for (DeferredItem<Item> p : PACKS) e.accept(p.get());
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> e.getDispatcher().register(Commands.literal(ID)
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    List<String> fails = CraftingSelfTest.run(c.getSource().getServer());
                    String line = fails.isEmpty() ? "Crafting self test PASS" : "Crafting self test FAIL: " + String.join("; ", fails);
                    c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return fails.isEmpty() ? 1 : 0;
                }))));
    }
}
