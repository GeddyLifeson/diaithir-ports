package net.diaithir.elevators;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

/** Elevators: floor blocks beside a shaft, a controller above it, and a cabin plate that glides between the floors. */
@Mod(ElevatorsMod.MOD)
public final class ElevatorsMod {
    public static final String MOD = "diaithirelevators";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD);

    public static final DeferredBlock<Block> FLOOR = BLOCKS.registerBlock("elevator_floor", FloorBlock::new, ElevatorsMod::steel);
    public static final DeferredBlock<Block> CONTROLLER = BLOCKS.registerBlock("elevator_controller", ControllerBlock::new, ElevatorsMod::steel);
    public static final DeferredBlock<Block> PLATE = BLOCKS.registerBlock("cabin_plate", PlateBlock::new, () -> steel().noOcclusion().noLootTable());
    public static final DeferredItem<BlockItem> FLOOR_ITEM = ITEMS.registerSimpleBlockItem(FLOOR);
    public static final DeferredItem<BlockItem> CONTROLLER_ITEM = ITEMS.registerSimpleBlockItem(CONTROLLER);
    public static final Supplier<BlockEntityType<ControllerEntity>> CONTROLLER_ENTITY = ENTITIES.register("controller",
            () -> new BlockEntityType<>(ControllerEntity::new, CONTROLLER.get()));

    private static BlockBehaviour.Properties steel() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    public ElevatorsMod(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        bus.addListener(RegisterCapabilitiesEvent.class, e ->
                e.registerBlockEntity(Capabilities.Energy.BLOCK, CONTROLLER_ENTITY.get(), (be, side) -> be.energy()));
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                e.accept(FLOOR_ITEM.get());
                e.accept(CONTROLLER_ITEM.get());
            }
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> register(e.getDispatcher()));
    }

    private static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("diaithirelevators")
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    List<String> fails = SelfTest.run(c.getSource().getServer());
                    String line = fails.isEmpty() ? "Elevators self test PASS" : "Elevators self test FAIL: " + String.join("; ", fails);
                    c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return fails.isEmpty() ? 1 : 0;
                })));
    }
}
