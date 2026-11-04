package vazkii.botania.slice;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Botania port, first playable slice: mana pool and mana powder under the {@code botania} namespace (ids match
 * diaithir-units magic_fuel.csv). Mana is stored as joules (100 J per mana) and exposed through the NeoForge energy capability.
 * Age V (magic.alchemy) gate lives in age_gates.json.
 */
@Mod("diaithir_druidry")
public final class BotaniaSlice {
    public static final String NS = "botania";
    public static final int J_PER_MANA = 100;
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NS);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NS);
    public static final DeferredRegister<BlockEntityType<?>> BE = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NS);

    public static final DeferredBlock<ManaPoolBlock> MANA_POOL = BLOCKS.registerBlock("mana_pool", ManaPoolBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0F, 10.0F).requiresCorrectToolForDrops().noOcclusion());
    public static final DeferredItem<net.minecraft.world.item.BlockItem> MANA_POOL_ITEM = ITEMS.registerSimpleBlockItem(MANA_POOL);
    public static final DeferredItem<Item> MANA_POWDER = ITEMS.registerSimpleItem("mana_powder");
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ManaPoolBlockEntity>> MANA_POOL_BE = BE.register("mana_pool",
            () -> new BlockEntityType<>(ManaPoolBlockEntity::new, MANA_POOL.get()));

    public BotaniaSlice(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BE.register(bus);
        bus.addListener(RegisterCapabilitiesEvent.class, e ->
                e.registerBlockEntity(Capabilities.Energy.BLOCK, MANA_POOL_BE.get(), (be, side) -> be.energy()));
    }

    /** Mana in a stored joule count. */
    public static int mana(int joules) { return joules / J_PER_MANA; }
}
