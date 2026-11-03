package net.diaithir.yards;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Registers the rail yard controllers, cranes, parts and rolling stock from yards.json. Rolling stock items are the finished vehicles. */
public final class YardContent {
    public static final String ID = "diaithircarts";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);

    /** Crane block to {lift kg, work speed}. Filled once the blocks exist. */
    public static final Map<Block, double[]> CRANE_STATS = new IdentityHashMap<>();
    private static final List<DeferredBlock<Block>> CRANE_BLOCKS = new ArrayList<>();
    private static final List<DeferredBlock<YardBlock>> YARD_BLOCKS = new ArrayList<>();
    private static final List<DeferredItem<? extends Item>> ALL = new ArrayList<>();

    static {
        YardData.load(ID);
    }

    public static final List<DeferredBlock<YardBlock>> CONTROLLERS = YARD_BLOCKS;

    static {
        for (YardData.Controller c : YardData.CONTROLLERS) {
            DeferredBlock<YardBlock> b = BLOCKS.registerBlock(c.id(), p -> new YardBlock(p, c.tier()), () -> props(3.5F));
            YARD_BLOCKS.add(b);
            ALL.add(ITEMS.registerSimpleBlockItem(b));
        }
        for (YardData.Crane c : YardData.CRANES) {
            DeferredBlock<Block> b = BLOCKS.registerSimpleBlock(c.id(), () -> props(4.0F));
            CRANE_BLOCKS.add(b);
            ALL.add(ITEMS.registerSimpleBlockItem(b));
        }
        for (YardData.Named p : YardData.PARTS) ALL.add(ITEMS.registerSimpleItem(p.id()));
        for (YardData.Vehicle v : YardData.VEHICLES) ALL.add(ITEMS.registerItem(v.id(), q -> new RollingStockItem(q, v.name(), v.category()), q -> q.stacksTo(1)));
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<YardBlockEntity>> YARD_BE = BLOCK_ENTITIES.register("yard",
            () -> new BlockEntityType<>(YardBlockEntity::new, YARD_BLOCKS.stream().map(DeferredBlock::get).toArray(Block[]::new)));

    private YardContent() {}

    private static BlockBehaviour.Properties props(float strength) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(strength, 8.0F).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) ALL.forEach(i -> e.accept(i.get()));
        });
        bus.addListener(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent.class, e -> {
            for (int i = 0; i < YardData.CRANES.size(); i++) {
                YardData.Crane c = YardData.CRANES.get(i);
                CRANE_STATS.put(CRANE_BLOCKS.get(i).get(), new double[] {c.liftKg(), c.speed()});
            }
        });
    }
}
