package net.diaithir.potions;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.UseCooldown;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

/** Potion reforms: potions stack to 8 and drink faster behind a short cooldown, stews stack to 16, coal fuels the stand, lingering costs a glow ink sac. */
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
            e -> e.getBuilder().addContainerRecipe(Items.SPLASH_POTION, Items.GLOW_INK_SAC, Items.LINGERING_POTION));
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
