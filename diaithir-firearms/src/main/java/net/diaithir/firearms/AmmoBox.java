package net.diaithir.firearms;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

/**
 * Ammo box (survey part 2 munitions containers, clean-room): holds up to {@link #CAPACITY} rounds of one kind in a component.
 * Use with rounds in the off hand to load them in; use with an empty off hand to take out one stack. Guns draw from boxes in the
 * inventory after loose rounds, so a box works as a reserve without a menu.
 */
public final class AmmoBox extends Item {
    public static final int CAPACITY = 512;

    /** ammo is the item path inside this mod's namespace; empty when the box is empty. */
    public record Content(String ammo, int count) {
        public static final Content EMPTY = new Content("", 0);
        public static final Codec<Content> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("ammo").forGetter(Content::ammo), Codec.INT.fieldOf("count").forGetter(Content::count)).apply(i, Content::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Content> STREAM = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Content::ammo, ByteBufCodecs.VAR_INT, Content::count, Content::new);
    }

    public AmmoBox(Properties p) { super(p); }

    public static Content content(ItemStack box) {
        Content c = box.get(DiaithirFirearms.BOX.get());
        return c == null ? Content.EMPTY : c;
    }

    static boolean isRound(ItemStack s) {
        if (s.isEmpty()) return false;
        Identifier k = BuiltInRegistries.ITEM.getKey(s.getItem());
        return k.getNamespace().equals(DiaithirFirearms.ID) && Defs.AMMO.contains(k.getPath());
    }

    /** Rounds of the given kind stored in this box. */
    public static int count(ItemStack box, String ammo) { Content c = content(box); return c.ammo().equals(ammo) ? c.count() : 0; }

    /** Remove up to want rounds of the kind; returns how many were taken. */
    public static int take(ItemStack box, String ammo, int want) {
        Content c = content(box);
        if (!c.ammo().equals(ammo)) return 0;
        int t = Math.min(want, c.count());
        if (t <= 0) return 0;
        box.set(DiaithirFirearms.BOX.get(), t == c.count() ? Content.EMPTY : new Content(ammo, c.count() - t));
        return t;
    }

    /** Add rounds taken from the stack; returns how many fitted. */
    public static int put(ItemStack box, String ammo, int n) {
        Content c = content(box);
        if (!c.ammo().isEmpty() && !c.ammo().equals(ammo)) return 0;
        int t = Math.min(n, CAPACITY - c.count());
        if (t <= 0) return 0;
        box.set(DiaithirFirearms.BOX.get(), new Content(ammo, c.count() + t));
        return t;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack box = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack other = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (isRound(other)) {
            String ammo = BuiltInRegistries.ITEM.getKey(other.getItem()).getPath();
            int t = put(box, ammo, other.getCount());
            if (t > 0) { other.shrink(t); return InteractionResult.SUCCESS; }
            return InteractionResult.FAIL;
        }
        Content c = content(box);
        if (c.count() > 0) {
            Item it = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(DiaithirFirearms.ID, c.ammo()));
            int t = take(box, c.ammo(), 64);
            player.getInventory().placeItemBackInInventory(new ItemStack(it, t));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        Content c = content(stack);
        if (c.count() == 0) { out.accept(Component.translatable("diaithirfirearms.box.empty", CAPACITY)); return; }
        Item it = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(DiaithirFirearms.ID, c.ammo()));
        out.accept(Component.translatable("diaithirfirearms.box.holds", c.count(), CAPACITY, it.getName(new ItemStack(it))));
    }
}
