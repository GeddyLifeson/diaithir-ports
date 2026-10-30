package net.diaithir.goety;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Item data: souls held by a totem, focus loaded into a wand. */
public final class GoetyComponents {
    public static final DeferredRegister.DataComponents TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Goety.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SOULS =
        TYPES.registerComponentType("souls", b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Identifier>> FOCUS =
        TYPES.registerComponentType("focus", b -> b.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC));

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }

    private GoetyComponents() {}
}
