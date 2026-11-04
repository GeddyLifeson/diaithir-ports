package net.diaithir.combat.survey;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BlocksAttacks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/**
 * Sword blocking (survey part 2), clean-room from the behaviour of Sword Blocking Mechanics: holding use with a sword raises it and takes
 * half of an incoming hit from the front. Done with the vanilla {@code blocks_attacks} component, so the raise animation, block sound and
 * durability cost come from the game itself and no mixin is needed. Swords are found by name ({@code *sword}, rapier, sabre, falchion ...)
 * in any namespace, so Spartan Weaponry and diaithir-arms swords qualify; items that already block (shields) are left alone.
 */
public final class SwordBlocking {
    /** Share of damage taken while blocking, 0.5 = half. */
    public static final float TAKEN = 0.5F;
    /** Frontal arc in degrees that the raised sword covers. */
    public static final float ARC = 180.0F;
    static final List<String> SUFFIXES = List.of("sword", "rapier", "sabre", "saber", "falchion", "scimitar", "katana", "cutlass", "gladius", "spatha", "claymore", "estoc", "foil", "kopesh", "khopesh");

    private SwordBlocking() {}

    public static void register(IEventBus bus) { bus.addListener(SwordBlocking::modify); }

    public static boolean isSwordName(String path) {
        for (String s : SUFFIXES) if (path.endsWith(s)) return true;
        return false;
    }

    private static void modify(ModifyDefaultComponentsEvent e) {
        e.modifyMatching((item, comps) -> !comps.has(DataComponents.BLOCKS_ATTACKS) && comps.has(DataComponents.MAX_DAMAGE)
                        && isSwordName(BuiltInRegistries.ITEM.getKey(item).getPath()),
                (b, ctx, item) -> b.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                        0.0F, 1.0F,
                        List.of(new BlocksAttacks.DamageReduction(ARC, Optional.empty(), 0.0F, 1.0F - TAKEN)),
                        new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                        Optional.of(ctx.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                        Optional.of(SoundEvents.SHIELD_BLOCK), Optional.of(SoundEvents.SHIELD_BREAK))));
    }
}
