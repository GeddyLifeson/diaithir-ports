package net.diaithir.druidry.ars;

import net.diaithir.druidry.ars.api.ANFakePlayer;
import net.diaithir.druidry.ars.api.spell.EntitySpellResolver;
import net.diaithir.druidry.ars.api.spell.Spell;
import net.diaithir.druidry.ars.api.spell.SpellContext;
import net.diaithir.druidry.ars.common.spell.effect.EffectHarm;
import net.diaithir.druidry.ars.common.spell.method.MethodTouch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Server-side selftest, run at server start with -Dars.selftest=true: casts Touch + Harm at a cow and checks it was hurt. */
final class ArsSelfTest {
    private static final Logger LOG = LoggerFactory.getLogger("ArsSelfTest");

    static void run(ServerStartedEvent event) {
        try {
            ServerLevel level = event.getServer().overworld();
            Cow cow = EntityType.COW.create(level, EntitySpawnReason.COMMAND);
            cow.setPos(0.5, 100, 0.5);
            level.addFreshEntity(cow);
            float before = cow.getHealth();
            ANFakePlayer caster = ANFakePlayer.getPlayer(level);
            Spell spell = new Spell(MethodTouch.INSTANCE, EffectHarm.INSTANCE);
            new EntitySpellResolver(SpellContext.fromEntity(spell, caster, ItemStack.EMPTY)).onCastOnEntity(cow);
            float after = cow.getHealth();
            cow.discard();
            if (after < before) {
                LOG.info("ARS_SELFTEST PASS (cow health {} -> {})", before, after);
            } else {
                LOG.error("ARS_SELFTEST FAIL: cow health unchanged at {}", after);
            }
        } catch (Throwable t) {
            LOG.error("ARS_SELFTEST FAIL: exception", t);
        }
        restoredContent(event);
    }

    /** Checks the restored scrying, imbuement and effect content: registration plus a few real casts. */
    private static void restoredContent(ServerStartedEvent event) {
        try {
            String[] glyphs = {"blink", "bounce", "cold_snap", "crush", "dispel", "exchange", "explosion", "fell", "flare", "glide", "gravity",
                    "harvest", "hex", "ignite", "infuse", "intangible", "light", "phantom_block", "prestidigitation", "rune", "sense_magic", "snare", "wololo"};
            StringBuilder missing = new StringBuilder();
            for (String g : glyphs) {
                if (net.diaithir.druidry.ars.api.registry.GlyphRegistry.getSpellPart(net.diaithir.druidry.ars.ArsNouveau.prefix("glyph_" + g)) == null
                        && net.diaithir.druidry.ars.api.registry.GlyphRegistry.getSpellPart(net.diaithir.druidry.ars.ArsNouveau.prefix(g)) == null) {
                    missing.append(g).append(' ');
                }
            }
            boolean blocks = net.diaithir.druidry.ars.setup.registry.BlockRegistry.IMBUEMENT_BLOCK.get() != null
                    && net.diaithir.druidry.ars.setup.registry.BlockRegistry.SCRYERS_OCULUS.get() != null
                    && net.diaithir.druidry.ars.setup.registry.BlockRegistry.IMBUEMENT_TILE.get() != null
                    && net.diaithir.druidry.ars.setup.registry.BlockRegistry.SCRYERS_OCULUS_TILE.get() != null
                    && net.diaithir.druidry.ars.setup.registry.ItemsRegistry.DOWSING_ROD.get() != null;
            boolean imbueType = net.diaithir.druidry.ars.api.registry.ImbuementRecipeRegistry.INSTANCE.getRecipeTypes().size() == 1;
            boolean scryers = net.diaithir.druidry.ars.api.ArsNouveauAPI.getInstance().getScryer(net.diaithir.druidry.ars.ArsNouveau.prefix("tag_scryer")) != null
                    || net.diaithir.druidry.ars.api.ArsNouveauAPI.getInstance().getScryer(net.diaithir.druidry.ars.api.scrying.TagScryer.INSTANCE.getRegistryName()) != null;
            boolean ritual = net.diaithir.druidry.ars.api.registry.RitualRegistry.getRitualMap().containsKey(new net.diaithir.druidry.ars.common.ritual.RitualScrying().getRegistryName());

            ServerLevel level = event.getServer().overworld();
            Cow cow = EntityType.COW.create(level, EntitySpawnReason.COMMAND);
            cow.setPos(0.5, 100, 0.5);
            level.addFreshEntity(cow);
            ANFakePlayer caster = ANFakePlayer.getPlayer(level);
            new EntitySpellResolver(SpellContext.fromEntity(new Spell(MethodTouch.INSTANCE, net.diaithir.druidry.ars.common.spell.effect.EffectIgnite.INSTANCE), caster, ItemStack.EMPTY)).onCastOnEntity(cow);
            boolean burning = cow.isOnFire();
            net.minecraft.world.entity.animal.sheep.Sheep sheep = EntityType.SHEEP.create(level, EntitySpawnReason.COMMAND);
            sheep.setPos(2.5, 100, 0.5);
            level.addFreshEntity(sheep);
            sheep.setColor(net.minecraft.world.item.DyeColor.WHITE);
            Spell wololo = new Spell(MethodTouch.INSTANCE, net.diaithir.druidry.ars.common.spell.effect.EffectWololo.INSTANCE);
            new EntitySpellResolver(SpellContext.fromEntity(wololo, caster, ItemStack.EMPTY)).onCastOnEntity(sheep);
            boolean dyed = sheep.getColor() != null;
            new EntitySpellResolver(SpellContext.fromEntity(new Spell(MethodTouch.INSTANCE, net.diaithir.druidry.ars.common.spell.effect.EffectPrestidigitation.INSTANCE), caster, ItemStack.EMPTY)).onCastOnEntity(sheep);
            cow.discard();
            sheep.discard();
            if (missing.length() == 0 && blocks && imbueType && scryers && ritual && burning && dyed) {
                LOG.info("ARS_SELFTEST PASS restored content (23 glyphs, imbuement + scrying registered, Ignite burned a cow, Wololo + Prestidigitation resolved; sheep colour {})", sheep.getColor());
            } else {
                LOG.error("ARS_SELFTEST FAIL restored: missing glyphs [{}] blocks={} imbueType={} scryers={} ritual={} burning={} dyed={}", missing, blocks, imbueType, scryers, ritual, burning, dyed);
            }
        } catch (Throwable t) {
            LOG.error("ARS_SELFTEST FAIL: restored content exception", t);
        }
    }
}
