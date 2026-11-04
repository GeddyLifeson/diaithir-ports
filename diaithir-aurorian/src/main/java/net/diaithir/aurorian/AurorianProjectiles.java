package net.diaithir.aurorian;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

/** Cerulean arrow, crystal arrow (upstream stats) and the crystalline sword beam. */
public final class AurorianProjectiles {
    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(DiaithirAurorian.ID);
    public static DeferredHolder<EntityType<?>, EntityType<Arrow>> CERULEAN;
    public static DeferredHolder<EntityType<?>, EntityType<Arrow>> CRYSTAL;
    public static DeferredHolder<EntityType<?>, EntityType<Beam>> BEAM;
    /** Beam damage and speed are upstream's defaults (config values 8 and 2). */
    static final float BEAM_DAMAGE = 8F;
    static final float BEAM_VELOCITY = 2F;

    public static class Arrow extends AbstractArrow {
        public Arrow(EntityType<? extends Arrow> t, Level l) {
            super(t, l);
            setBaseDamage(cerulean() ? 1.5 : 3.0);
        }

        public Arrow(EntityType<? extends Arrow> t, Level l, @Nullable LivingEntity owner, ItemStack pickup, @Nullable ItemStack weapon) {
            super(t, owner, l, pickup, weapon);
            setBaseDamage(cerulean() ? 1.5 : 3.0);
        }

        /** Note: AbstractArrow's constructor calls getDefaultPickupItem, so nothing here may depend on fields of this class. */
        private boolean cerulean() {
            return getType() == CERULEAN.get();
        }

        @Override
        protected double getDefaultGravity() {
            return cerulean() ? 0.05 : 0.25; // upstream: vanilla gravity plus a weight of 0 (cerulean) or 0.2 (crystal)
        }

        @Override
        protected ItemStack getDefaultPickupItem() {
            return new ItemStack(DiaithirAurorian.item(getType() == CERULEAN.get() ? "cerulean_arrow" : "crystal_arrow"));
        }
    }

    /** Crystalline sword beam: flies straight, hurts the first thing it meets, vanishes on blocks, water and after 30 s. */
    public static class Beam extends AbstractArrow {
        public Beam(EntityType<? extends Beam> t, Level l) {
            super(t, l);
            setNoGravity(true);
            setBaseDamage(BEAM_DAMAGE / BEAM_VELOCITY);
            pickup = Pickup.DISALLOWED;
        }

        @Override
        public void tick() {
            super.tick();
            if (!level().isClientSide() && (tickCount > 600 || isInWater() || isInGround())) {
                discard();
            }
        }

        @Override
        protected void onHitEntity(EntityHitResult r) {
            super.onHitEntity(r);
            discard();
        }

        @Override
        protected void onHitBlock(BlockHitResult r) {
            if (!level().isClientSide()) {
                level().playSound(null, getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH, getSoundSource(), 1F, 1F);
                discard();
            }
        }

        @Override
        protected ItemStack getDefaultPickupItem() {
            return new ItemStack(net.minecraft.world.item.Items.ARROW); // never picked up (Pickup.DISALLOWED), but an empty stack cannot be saved
        }
    }

    static void init(IEventBus bus) {
        CERULEAN = ENTITIES.registerEntityType("cerulean_arrow", Arrow::new, MobCategory.MISC, b -> b.sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(4).updateInterval(20));
        CRYSTAL = ENTITIES.registerEntityType("crystal_arrow", Arrow::new, MobCategory.MISC, b -> b.sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(4).updateInterval(20));
        BEAM = ENTITIES.registerEntityType("crystalline_beam", Beam::new, MobCategory.MISC, b -> b.sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(4).updateInterval(5));
        ENTITIES.register(bus);
    }

    private AurorianProjectiles() {}
}
