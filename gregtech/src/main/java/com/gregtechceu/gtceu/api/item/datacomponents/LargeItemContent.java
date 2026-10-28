package com.gregtechceu.gtceu.api.item.datacomponents;

import com.gregtechceu.gtceu.utils.codec.CodecUtils;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.With;

import java.util.Optional;

public record LargeItemContent(@With ItemStack stored, @With long amount) { return null; /* ported stub */ }
