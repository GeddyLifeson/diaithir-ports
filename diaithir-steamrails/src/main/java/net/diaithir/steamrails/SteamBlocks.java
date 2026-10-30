package net.diaithir.steamrails;

import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.content.trains.bogey.AllBogeySizes;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/** Every registry entry of the mod. */
public final class SteamBlocks {
    private SteamBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SteamRails.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SteamRails.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SteamRails.ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, SteamRails.ID);

    private static BlockBehaviour.Properties iron() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0F, 9.0F).sound(SoundType.COPPER).requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<BoilerBlock> BOILER = BLOCKS.registerBlock("boiler_firebox", BoilerBlock::new,
            () -> iron().lightLevel(s -> s.getValue(BoilerBlock.LIT) ? 10 : 0));
    public static final DeferredBlock<ShellBlock> COPPER_SHELL = BLOCKS.registerBlock("copper_boiler_shell",
            p -> new ShellBlock(p, SteamRails.COPPER_RATING), () -> iron().mapColor(MapColor.COLOR_ORANGE));
    public static final DeferredBlock<ShellBlock> STEEL_SHELL = BLOCKS.registerBlock("steel_boiler_shell",
            p -> new ShellBlock(p, SteamRails.STEEL_RATING), () -> iron().strength(5.0F, 12.0F).mapColor(MapColor.COLOR_GRAY));
    public static final DeferredBlock<SmokestackBlock> SMOKESTACK = BLOCKS.registerBlock("smokestack", SmokestackBlock::new,
            () -> iron().noOcclusion());
    public static final DeferredBlock<CylinderBlock> CYLINDER = BLOCKS.registerBlock("steam_cylinder", CylinderBlock::new, () -> iron().noOcclusion());
    public static final DeferredBlock<WhistleBlock> WHISTLE = BLOCKS.registerBlock("steam_whistle", WhistleBlock::new, () -> iron().noOcclusion());
    public static final DeferredBlock<TenderBlock> TENDER = BLOCKS.registerBlock("fuel_tender", TenderBlock::new, () -> iron().strength(3.5F, 8.0F));

    /** The bogeys have no item: Create makes a bogey from a railway casing and the wrench picks its style, as for the standard ones. */
    public static final DeferredBlock<SteamBogeyBlock> DRIVER_BOGEY = BLOCKS.registerBlock("driver_bogey",
            p -> new SteamBogeyBlock(p, AllBogeySizes.LARGE, "driver", 2.0, 12.5),
            () -> BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_BLACK)
                    .sound(SoundType.NETHERITE_BLOCK).noOcclusion());
    public static final DeferredBlock<SteamBogeyBlock> PONY_BOGEY = BLOCKS.registerBlock("pony_bogey",
            p -> new SteamBogeyBlock(p, AllBogeySizes.SMALL, "pony", 2.0, 6.0),
            () -> BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.GOLD_BLOCK).mapColor(MapColor.COLOR_BLACK)
                    .sound(SoundType.NETHERITE_BLOCK).noOcclusion());

    public static final List<DeferredBlock<? extends Block>> ITEM_BLOCKS = List.of(BOILER, COPPER_SHELL, STEEL_SHELL, SMOKESTACK, CYLINDER, WHISTLE, TENDER);
    public static final List<DeferredItem<BlockItem>> BLOCK_ITEMS = ITEM_BLOCKS.stream().map(b -> ITEMS.registerSimpleBlockItem(b)).toList();

    public static final DeferredItem<Item> RIVETED_PLATE = ITEMS.registerSimpleItem("riveted_plate");
    public static final DeferredItem<Item> FLUE_TUBE = ITEMS.registerSimpleItem("flue_tube");
    public static final DeferredItem<Item> PISTON_ROD = ITEMS.registerSimpleItem("piston_rod");
    public static final List<DeferredItem<Item>> PARTS = List.of(RIVETED_PLATE, FLUE_TUBE, PISTON_ROD);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoilerBlockEntity>> BOILER_BE = BLOCK_ENTITIES.register("boiler",
            () -> new BlockEntityType<>(BoilerBlockEntity::new, BOILER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TenderBlockEntity>> TENDER_BE = BLOCK_ENTITIES.register("tender",
            () -> new BlockEntityType<>(TenderBlockEntity::new, TENDER.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamBogeyBlockEntity>> BOGEY_BE = BLOCK_ENTITIES.register("steam_bogey",
            () -> new BlockEntityType<>(SteamBogeyBlockEntity::new, DRIVER_BOGEY.get(), PONY_BOGEY.get()));

    public static final DeferredHolder<SoundEvent, SoundEvent> WHISTLE_SOUND = SOUNDS.register("steam_whistle",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(SteamRails.ID, "steam_whistle")));

    static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        SOUNDS.register(bus);
    }

    /** Adds the steam styles to Create's bogey registry. Run once, after the blocks exist. */
    static void registerStyles() {
        if (AllBogeyStyles.BOGEY_STYLES.containsKey(Identifier.fromNamespaceAndPath(SteamRails.ID, "driver"))) return;
        new BogeyStyle.Builder(Identifier.fromNamespaceAndPath(SteamRails.ID, "driver"), AllBogeyStyles.STANDARD_CYCLE_GROUP)
                .displayName(Component.translatable("bogey.diaithirsteamrails.driver"))
                .smokeParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE)
                .size(AllBogeySizes.LARGE, DRIVER_BOGEY.get())
                .build();
        new BogeyStyle.Builder(Identifier.fromNamespaceAndPath(SteamRails.ID, "pony"), AllBogeyStyles.STANDARD_CYCLE_GROUP)
                .displayName(Component.translatable("bogey.diaithirsteamrails.pony"))
                .smokeParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE)
                .size(AllBogeySizes.SMALL, PONY_BOGEY.get())
                .build();
    }
}
