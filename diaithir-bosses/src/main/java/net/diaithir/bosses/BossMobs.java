package net.diaithir.bosses;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * The body kinds our own bosses wear. Each is a small subclass of a vanilla creature (so its vanilla model, renderer and
 * baseline melee come for free); all boss behaviour is added by BossController from the profile.
 */
public final class BossMobs {
    public record Kind(String id, EntityType.EntityFactory<? extends Mob> factory, Supplier<AttributeSupplier.Builder> attrs, float w, float h) {}

    public static final class BZombie extends Zombie {
        public BZombie(EntityType<? extends Zombie> t, Level l) { super(t, l); }
        @Override protected boolean isSunSensitive() { return false; }
        @Override protected boolean convertsInWater() { return false; }
    }

    public static final class BIllager extends Vindicator {
        public BIllager(EntityType<? extends Vindicator> t, Level l) { super(t, l); }
    }

    public static final class BGolem extends IronGolem {
        public BGolem(EntityType<? extends IronGolem> t, Level l) { super(t, l); }
        @Override protected void registerGoals() {
            super.registerGoals();
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }
    }

    public static final class BSpider extends Spider {
        public BSpider(EntityType<? extends Spider> t, Level l) { super(t, l); }
    }

    public static final class BRavager extends Ravager {
        public BRavager(EntityType<? extends Ravager> t, Level l) { super(t, l); }
    }

    public static final class BBlaze extends Blaze {
        public BBlaze(EntityType<? extends Blaze> t, Level l) { super(t, l); }
    }

    public static final class BSilverfish extends Silverfish {
        public BSilverfish(EntityType<? extends Silverfish> t, Level l) { super(t, l); }
    }

    public static final class BPhantom extends Phantom {
        public BPhantom(EntityType<? extends Phantom> t, Level l) { super(t, l); }
    }

    public static final class BGuardian extends Guardian {
        public BGuardian(EntityType<? extends Guardian> t, Level l) { super(t, l); }
    }

    public static final class BHoglin extends Hoglin {
        public BHoglin(EntityType<? extends Hoglin> t, Level l) { super(t, l); setImmuneToZombification(true); }
    }

    public static final Map<String, Kind> KINDS = new LinkedHashMap<>();

    private static <T extends Mob> void k(String id, EntityType.EntityFactory<T> f, Supplier<AttributeSupplier.Builder> a, float w, float h) {
        KINDS.put(id, new Kind(id, f, a, w, h));
    }

    static {
        k("zombie", BZombie::new, Zombie::createAttributes, 0.6F, 1.95F);
        k("illager", BIllager::new, Vindicator::createAttributes, 0.6F, 1.95F);
        k("golem", BGolem::new, IronGolem::createAttributes, 1.4F, 2.7F);
        k("spider", BSpider::new, Spider::createAttributes, 1.4F, 0.9F);
        k("ravager", BRavager::new, Ravager::createAttributes, 1.95F, 2.2F);
        k("blaze", BBlaze::new, Blaze::createAttributes, 0.6F, 1.8F);
        k("silverfish", BSilverfish::new, Silverfish::createAttributes, 0.4F, 0.3F);
        k("phantom", BPhantom::new, net.minecraft.world.entity.monster.Monster::createMonsterAttributes, 0.9F, 0.5F);
        k("guardian", BGuardian::new, Guardian::createAttributes, 0.85F, 0.85F);
        k("hoglin", BHoglin::new, Hoglin::createAttributes, 1.3965F, 1.4F);
    }

    private BossMobs() {}
}
