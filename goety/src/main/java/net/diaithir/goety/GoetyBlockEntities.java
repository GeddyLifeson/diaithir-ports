package net.diaithir.goety;

import java.util.HashSet;
import java.util.Set;
import net.diaithir.goety.rite.AltarBlock;
import net.diaithir.goety.rite.AltarBlockEntity;
import net.diaithir.goety.rite.PedestalBlock;
import net.diaithir.goety.rite.PedestalBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Block entity types: the altar and the pedestals of the rites. */
public final class GoetyBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Goety.MOD_ID);

    private static Set<Block> of(Class<?> c) {
        Set<Block> s = new HashSet<>();
        GoetyBlocks.ALL.values().forEach(b -> { if (c.isInstance(b.get())) s.add(b.get()); });
        return s;
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltarBlockEntity>> ALTAR =
        TYPES.register("dark_altar", () -> new BlockEntityType<>(AltarBlockEntity::new, of(AltarBlock.class)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PedestalBlockEntity>> PEDESTAL =
        TYPES.register("pedestal", () -> new BlockEntityType<>(PedestalBlockEntity::new, of(PedestalBlock.class)));

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }

    private GoetyBlockEntities() {}
}
