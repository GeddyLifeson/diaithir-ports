package io.github.tr100000.trutils.api.gui;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;

public record ItemIcon(ItemStackTemplate template) implements Icon {
    public static final MapCodec<ItemIcon> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    ItemStackTemplate.CODEC.fieldOf("item").forGetter(ItemIcon::template)
            ).apply(instance, ItemIcon::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemIcon> PACKET_CODEC = ItemStackTemplate.STREAM_CODEC.map(
            ItemIcon::new,
            ItemIcon::template
    );

    public static final IconType<ItemIcon> TYPE = new IconType<>("item", CODEC, PACKET_CODEC);

    public ItemIcon(Item item) {
        this(new ItemStackTemplate(item));
    }

    public ItemIcon(ItemLike itemLike) {
        this(itemLike.asItem());
    }

    @Override
    public IconType<?> getType() {
        return TYPE;
    }
}
