package com.github.alexmodguy.alexscaves.server.misc;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.UUID;

public class ACDataSerializers {
    public static final DeferredRegister<EntityDataSerializer<?>> DEF_REG = DeferredRegister.create(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, AlexsCaves.MODID);

    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID = EntityDataSerializer.forValueType(ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC));
    public static final EntityDataSerializer<net.minecraft.nbt.CompoundTag> COMPOUND_TAG = EntityDataSerializer.forValueType(ByteBufCodecs.COMPOUND_TAG);
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<net.minecraft.nbt.CompoundTag>> COMPOUND_TAG_HOLDER = DEF_REG.register("compound_tag", () -> COMPOUND_TAG);
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Optional<UUID>>> OPTIONAL_UUID_HOLDER = DEF_REG.register("optional_uuid", () -> OPTIONAL_UUID);
}
