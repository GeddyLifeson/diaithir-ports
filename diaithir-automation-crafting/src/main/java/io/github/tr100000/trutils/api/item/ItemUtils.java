package io.github.tr100000.trutils.api.item;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import org.jspecify.annotations.Nullable;

public final class ItemUtils {
    private ItemUtils() {}

    public static Item fromPacket(FriendlyByteBuf buf) {
        return BuiltInRegistries.ITEM.getOptional(buf.readIdentifier()).orElseThrow();
    }

    public static void writePacket(Item item, FriendlyByteBuf buf) {
        buf.writeIdentifier(BuiltInRegistries.ITEM.getKey(item));
    }

    public static Tag itemStackToNbt(ItemStack stack, Provider registryLookup) {
        return stack.isEmpty() ? new CompoundTag() : ItemStack.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE, registryLookup), stack).getOrThrow();
    }

    public static boolean isNullOrAir(@Nullable Item item) {
        return item == null || item == Items.AIR;
    }

    public static boolean isNullOrEmpty(@Nullable ItemStack stack) {
        return stack == null || stack.isEmpty();
    }

    public static boolean itemHasUseAction(Item item, ItemUseAnimation action) {
        Consumable consumable = item.components().get(DataComponents.CONSUMABLE);
        return consumable != null && consumable.animation() == action;
    }

    public static boolean isEdibleItem(Item item) {
        return item.components().get(DataComponents.FOOD) != null
                || itemHasUseAction(item, ItemUseAnimation.EAT);
    }

    public static boolean isDrinkableItem(Item item) {
        return itemHasUseAction(item, ItemUseAnimation.DRINK);
    }

    public static String getPotionTranslationKey(Holder<Potion> potion) {
        return String.format("item.minecraft.potion.effect.%s", potion.value().name());
    }

    public static ItemStack getPotionStack(Holder<Potion> potion) {
        ItemStack stack = Items.POTION.getDefaultInstance();
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return stack;
    }
}
