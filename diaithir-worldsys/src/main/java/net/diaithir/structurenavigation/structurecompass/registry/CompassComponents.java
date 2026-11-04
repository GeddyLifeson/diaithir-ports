// Ported from Structure Compass (MIT) https://github.com/Mrbysco/StructureCompass
// Modifications: Renamed from StructureComponents, uses the diaithirstructurenavigation namespace.
package net.diaithir.structurenavigation.structurecompass.registry;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.diaithir.structurenavigation.structurecompass.CompassIds;
import net.diaithir.structurenavigation.structurecompass.component.StructureInfo;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CompassComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CompassIds.MOD_ID);

    public static final Supplier<DataComponentType<Identifier>> STRUCTURE = DATA_COMPONENT_TYPES.registerComponentType("structure", builder ->
            builder
                    .persistent(Identifier.CODEC)
                    .networkSynchronized(Identifier.STREAM_CODEC)
    );

    public static final Supplier<DataComponentType<Boolean>> IS_TAG = DATA_COMPONENT_TYPES.registerComponentType("is_tag", builder ->
            builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
    );

    public static final Supplier<DataComponentType<StructureInfo>> STRUCTURE_INFO = DATA_COMPONENT_TYPES.registerComponentType("structure_info", builder ->
            builder
                    .persistent(StructureInfo.CODEC)
                    .networkSynchronized(StructureInfo.STREAM_CODEC)
    );

    private CompassComponents() {}
}
