package net.diaithir.potions;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.UseCooldown;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

/** Potion reforms: potions stack to 8 and drink faster behind a short cooldown, stews stack to 16, coal fuels the stand, lingering costs a glow ink sac, honeycomb lengthens and amethyst strengthens. */
@Mod(DiaithirPotions.ID)
public final class DiaithirPotions {
    public static final String ID = "diaithirpotions";
    static final int POTION_STACK = 8;
    static final int STEW_STACK = 16;
    static final float DRINK_SECONDS = 1.0F;
    static final float COOLDOWN_SECONDS = 2.0F;

    public DiaithirPotions(IEventBus bus) {
        bus.addListener(ModifyDefaultComponentsEvent.class, DiaithirPotions::components);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> PotionSelfTest.register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(RegisterBrewingRecipesEvent.class,
            DiaithirPotions::brewing);
    }

    /** Overworld materials reach what vanilla keeps behind Nether trips: honeycomb for duration, amethyst for potency. */
    private static void brewing(RegisterBrewingRecipesEvent e) {
        var b = e.getBuilder();
        b.addContainerRecipe(Items.SPLASH_POTION, Items.GLOW_INK_SAC, Items.LINGERING_POTION);
        b.addMix(Potions.NIGHT_VISION, Items.HONEYCOMB, Potions.LONG_NIGHT_VISION);
        b.addMix(Potions.INVISIBILITY, Items.HONEYCOMB, Potions.LONG_INVISIBILITY);
        b.addMix(Potions.LEAPING, Items.HONEYCOMB, Potions.LONG_LEAPING);
        b.addMix(Potions.FIRE_RESISTANCE, Items.HONEYCOMB, Potions.LONG_FIRE_RESISTANCE);
        b.addMix(Potions.SWIFTNESS, Items.HONEYCOMB, Potions.LONG_SWIFTNESS);
        b.addMix(Potions.SLOWNESS, Items.HONEYCOMB, Potions.LONG_SLOWNESS);
        b.addMix(Potions.WATER_BREATHING, Items.HONEYCOMB, Potions.LONG_WATER_BREATHING);
        b.addMix(Potions.POISON, Items.HONEYCOMB, Potions.LONG_POISON);
        b.addMix(Potions.REGENERATION, Items.HONEYCOMB, Potions.LONG_REGENERATION);
        b.addMix(Potions.STRENGTH, Items.HONEYCOMB, Potions.LONG_STRENGTH);
        b.addMix(Potions.WEAKNESS, Items.HONEYCOMB, Potions.LONG_WEAKNESS);
        b.addMix(Potions.SLOW_FALLING, Items.HONEYCOMB, Potions.LONG_SLOW_FALLING);
        b.addMix(Potions.LEAPING, Items.AMETHYST_SHARD, Potions.STRONG_LEAPING);
        b.addMix(Potions.SWIFTNESS, Items.AMETHYST_SHARD, Potions.STRONG_SWIFTNESS);
        b.addMix(Potions.SLOWNESS, Items.AMETHYST_SHARD, Potions.STRONG_SLOWNESS);
        b.addMix(Potions.HEALING, Items.AMETHYST_SHARD, Potions.STRONG_HEALING);
        b.addMix(Potions.HARMING, Items.AMETHYST_SHARD, Potions.STRONG_HARMING);
        b.addMix(Potions.POISON, Items.AMETHYST_SHARD, Potions.STRONG_POISON);
        b.addMix(Potions.REGENERATION, Items.AMETHYST_SHARD, Potions.STRONG_REGENERATION);
        b.addMix(Potions.STRENGTH, Items.AMETHYST_SHARD, Potions.STRONG_STRENGTH);
    }

    private static void components(ModifyDefaultComponentsEvent e) {
        e.modify(Items.POTION, (c, ctx, item) -> {
            c.set(DataComponents.MAX_STACK_SIZE, POTION_STACK);
            Consumable base = c.build().get(DataComponents.CONSUMABLE);
            if (base != null) {
                c.set(DataComponents.CONSUMABLE, Consumable.builder().consumeSeconds(DRINK_SECONDS).animation(base.animation())
                    .sound(base.sound()).hasConsumeParticles(base.hasConsumeParticles()).build());
            }
            c.set(DataComponents.USE_COOLDOWN, new UseCooldown(COOLDOWN_SECONDS));
        });
        for (var stew : new net.minecraft.world.item.Item[] {Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.BEETROOT_SOUP, Items.SUSPICIOUS_STEW}) {
            e.modify(stew, (c, ctx, item) -> c.set(DataComponents.MAX_STACK_SIZE, STEW_STACK));
        }
    }
}
