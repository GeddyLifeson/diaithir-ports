package net.diaithir.carts;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Magical fuel for vehicles. A mana firebox (fitted to a steam locomotive) or a mana cell (fitted to a diesel or electric one) is used on a furnace-cart
 * vehicle once; from then on that vehicle burns any item in the tag {@code diaithircarts:magic_fuel} instead of coal. One item stands for one coal's
 * burn time. The tag holds the mana, aura, source and similar items of the magic mods (optional entries, so absent mods cost nothing); the bridge
 * in diaithir-units prices magic at 100 J per mana, 1000 J per Source, and the magic thread tunes the tag.
 */
public final class MagicFuel {
    private MagicFuel() {}

    public static final TagKey<Item> FUEL = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DiaithirCarts.ID, "magic_fuel"));
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DiaithirCarts.ID);
    /** True on a vehicle that has been fitted with a mana firebox or mana cell. */
    public static final Supplier<AttachmentType<Boolean>> FITTED = ATTACHMENTS.register("magic_fitted", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL.fieldOf("on")).build());

    public static boolean fitted(AbstractMinecart cart) {
        return cart.getData(FITTED);
    }

    /** Called from the cart-interact event. Returns null when the held item is not ours to handle. */
    public static InteractionResult use(AbstractMinecart cart, Player player, InteractionHand hand, ItemStack held) {
        if (held.is(DiaithirCarts.MANA_FIREBOX.get()) || held.is(DiaithirCarts.MANA_CELL.get())) {
            if (!(cart.getType() == net.minecraft.world.entity.EntityType.FURNACE_MINECART)) { player.sendOverlayMessage(Component.literal("Only a locomotive can be fitted")); return InteractionResult.FAIL; }
            if (fitted(cart)) { player.sendOverlayMessage(Component.literal("Already fitted")); return InteractionResult.FAIL; }
            cart.setData(FITTED, true);
            cart.setData(GaugeData.ENGINE, "arcane_motor");
            held.shrink(1);
            player.sendOverlayMessage(Component.literal("Fitted: burns magic fuel"));
            return InteractionResult.SUCCESS;
        }
        if (held.is(FUEL) && fitted(cart)) {
            // the furnace cart only takes coal; hand it one coal's worth in place of the magic item
            ItemStack magic = held.copy();
            player.setItemInHand(hand, new ItemStack(Items.COAL));
            InteractionResult r = cart.interact(player, hand, cart.position());
            player.setItemInHand(hand, magic);
            if (r.consumesAction()) magic.shrink(1);
            return r;
        }
        return null;
    }
}
