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
import net.neoforged.neoforge.common.NeoForge;
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

    private MegaMod() {}

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        NeoForge.EVENT_BUS.addListener(MegaMod::commands);
        if (Boolean.getBoolean("megamachines.selftest")) NeoForge.EVENT_BUS.register(new MegaSelfTest());
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
                        }))));
    }
}
