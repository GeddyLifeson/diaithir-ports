package com.kyanite.deeperdarker.world.otherside;

import com.google.common.collect.ImmutableSet;
import com.kyanite.deeperdarker.DeeperDarker;
import com.kyanite.deeperdarker.content.DDBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class OthersideDimension {
    public static final ResourceKey<Level> OTHERSIDE_LEVEL = ResourceKey.create(Registries.DIMENSION, DeeperDarker.rl("otherside"));
    public static final ResourceKey<DimensionType> OTHERSIDE = ResourceKey.create(Registries.DIMENSION_TYPE, DeeperDarker.rl("otherside"));
    public static final Identifier OTHERSIDE_EFFECTS = DeeperDarker.rl("otherside_effects");

    public static final DeferredRegister<PoiType> POI = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, DeeperDarker.MOD_ID);
    public static final DeferredHolder<PoiType, PoiType> OTHERSIDE_PORTAL = POI.register("otherside_portal", () -> new PoiType(ImmutableSet.copyOf(DDBlocks.OTHERSIDE_PORTAL.get().getStateDefinition().getPossibleStates()), 0, 1));
}
