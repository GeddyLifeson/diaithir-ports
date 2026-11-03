package net.diaithir.upgrades;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

final class Registry {
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DiaithirUpgrades.ID);
    static final DeferredRegister<BlockEntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DiaithirUpgrades.ID);

    static final DeferredBlock<Block> BEACON = BLOCKS.registerBlock("beacon", BeaconBlock::new,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5F, 12F)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops());
    static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BeaconBlockEntity>> BEACON_ENTITY =
            ENTITIES.register("beacon", () -> new BlockEntityType<>(BeaconBlockEntity::new, BEACON.get()));

    private Registry() {}

    static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ENTITIES.register(bus);
        Modules.ITEMS.registerSimpleBlockItem("beacon", BEACON);
        Modules.ITEMS.registerItem("planner", p -> new PlannerItem(p.stacksTo(1)));
    }
}
