package org.diaithir.diesel.registries;

import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import com.zurrtum.create.infrastructure.fluids.FluidEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.diaithir.diesel.CreateDieselGenerators;

import java.util.LinkedHashMap;
import java.util.Map;

/** Fluids, registered on Create's own FlowableFluid so pipes, tanks and the renderer treat them like Create's. */
public class CDGFluids {
    public static final Map<String, FluidEntry> ENTRIES = new LinkedHashMap<>();
    public static final String[] NAMES = {"crude_oil", "diesel", "gasoline", "biodiesel", "ethanol", "plant_oil"};
    private static final int[] VISCOSITY = {1500, 1500, 1500, 1500, 1500, 1500};
    private static final int[] DENSITY = {100, 500, 500, 500, 500, 500};

    /** Called from RegisterEvent for the matching registry key; each call registers that registry's part for all fluids. */
    private static void ensure() {
        if (!ENTRIES.isEmpty()) return;
        for (int i = 0; i < NAMES.length; i++) {
            String n = NAMES[i];
            FluidEntry e = new FluidEntry();
            e.type = new FluidType(FluidType.Properties.create().descriptionId("fluid." + CreateDieselGenerators.MODID + "." + n)
                .viscosity(VISCOSITY[i]).density(DENSITY[i]));
            ENTRIES.put(n, e);
        }
    }

    public static void registerTypes() {
        ensure();
        ENTRIES.forEach((n, e) -> Registry.register(NeoForgeRegistries.FLUID_TYPES, id(n), e.type));
    }

    public static void registerFluids() {
        ensure();
        ENTRIES.forEach((n, e) -> {
            e.still = new FlowableFluid.Still(e);
            e.flowing = new FlowableFluid.Flowing(e);
            Registry.register(BuiltInRegistries.FLUID, id(n), e.still);
            Registry.register(BuiltInRegistries.FLUID, id("flowing_" + n), e.flowing);
        });
    }

    public static void registerBlocks() {
        ENTRIES.forEach((n, e) -> {
            ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id(n));
            e.block = Registry.register(BuiltInRegistries.BLOCK, key, new FluidBlock(e.still, BlockBehaviour.Properties.of()
                .setId(key).mapColor(MapColor.COLOR_BLACK).replaceable().noCollision().strength(100f).pushReaction(PushReaction.DESTROY)
                .noLootTable().liquid().sound(net.minecraft.world.level.block.SoundType.EMPTY)));
        });
    }

    public static void registerItems() {
        ENTRIES.forEach((n, e) -> {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(n + "_bucket"));
            e.bucket = Registry.register(BuiltInRegistries.ITEM, key, new BucketItem(e.still,
                new Item.Properties().setId(key).craftRemainder(Items.BUCKET).stacksTo(1)));
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(CreateDieselGenerators.MODID, path);
    }
}
