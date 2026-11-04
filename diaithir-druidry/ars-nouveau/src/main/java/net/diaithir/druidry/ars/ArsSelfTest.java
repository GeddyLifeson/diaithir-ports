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
    }
}
