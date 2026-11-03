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
 * vehicle once; from then on that vehicle burns any item in the tag {@code diaithircarts:magic_fuel} instead of coal: each item's joules go into a bank on
 * the vehicle and every fill of fire is paid from it. The tag holds the mana, aura, source and similar items of the magic mods (optional entries, so absent mods cost nothing); the bridge
 * in diaithir-units prices magic at 100 J per mana, 1000 J per Source, and the magic thread tunes the tag.
 */
public final class MagicFuel {
    private MagicFuel() {}

    public static final TagKey<Item> FUEL = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DiaithirCarts.ID, "magic_fuel"));
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DiaithirCarts.ID);
    /** True on a vehicle that has been fitted with a mana firebox or mana cell. */
    public static final Supplier<AttachmentType<Boolean>> FITTED = ATTACHMENTS.register("magic_fitted", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL.fieldOf("on")).build());

    /** Joules per item: the magic thread's figures (mana x 100 J; ports PR #266 magic_fuel.csv). Coal is 24 MJ, so one reagent is a small fraction of a coal. Unknown ids are 0 J. */
    private static final java.util.Map<String, Long> JOULES_OF = java.util.Map.of(
            "botania:mana_powder", 4_000L, "botania:manasteel_ingot", 16_000L, "botania:mana_pearl", 40_000L, "botania:mana_diamond", 80_000L,
            "ars_nouveau:source_gem", 10_000L, "ars_nouveau:source_gem_block", 90_000L, "theurgy:sal_ammoniac_crystal", 16_000L, "minecraft:amethyst_shard", 8_000L);
    /** A coal burns 3600 ticks (180 s) in a furnace cart; the arcane motor turns magic into shaft work at 0.60 (the units fuel table). */
    private static final int FILL_SECONDS = 180;
    private static final double ARCANE_EFFICIENCY = 0.60;

    public static long joules(ItemStack stack) {
        return JOULES_OF.getOrDefault(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), 0L);
    }

    /** Joules of magic one fill of fire costs this vehicle: its rated power for a coal's burn time, over the motor's efficiency. */
    public static long fillCost(AbstractMinecart cart) {
        return (long) (Math.max(1000, cart.getData(GaugeData.RATED)) * (double) FILL_SECONDS / ARCANE_EFFICIENCY);
    }

    /** Adds joules to a fitted vehicle's bank (items, or an energy bridge block at 1 FE = 1 J). */
    public static void charge(AbstractMinecart cart, long joules) {
        cart.setData(GaugeData.JOULES, cart.getData(GaugeData.JOULES) + joules);
    }

    /** Every tick of a fitted locomotive: when its fire has gone out and the bank covers a fill, pay for one and relight it. */
    public static boolean tick(AbstractMinecart cart) {
        if (!(cart instanceof net.minecraft.world.entity.vehicle.minecart.MinecartFurnace f) || !fitted(cart) || lit(f)) return false;
        long bank = cart.getData(GaugeData.JOULES), cost = fillCost(cart);
        if (bank < cost) return false;
        cart.setData(GaugeData.JOULES, bank - cost);
        return f.addFuel(f.position(), new ItemStack(Items.COAL));
    }

    /** True while the furnace cart has fuel burning (its display block is a lit furnace). */
    public static boolean lit(net.minecraft.world.entity.vehicle.minecart.MinecartFurnace f) {
        return f.getDefaultDisplayBlockState().getValue(net.minecraft.world.level.block.FurnaceBlock.LIT);
    }

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
            long j = joules(held);
            if (j <= 0) { player.sendOverlayMessage(Component.literal("That holds no usable magic")); return InteractionResult.FAIL; }
            charge(cart, j);
            held.shrink(1);
            player.sendOverlayMessage(Component.literal("Banked " + cart.getData(GaugeData.JOULES) / 1000 + " kJ"));
            return InteractionResult.SUCCESS;
        }
        return null;
    }
}
