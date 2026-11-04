package net.diaithir.arms;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Historical throwables and sling-type launchers: one data-driven item and one projectile entity (historical_thrown.json). */
public final class Thrown {
    /** One entry of historical_thrown.json (written by tools/gen_historical_thrown.py). */
    public record Design(String id, String kind, float damage, float power, int fuse, float speed, int effectTicks, int effectAmp,
                         int durability, int stack, int cooldown, String ammo) {}

    public static final List<Design> DESIGNS = load();
    private static final Design SIEGE = new Design("siege", "siege", 0, 0, 0, 0, 0, 0, 0, 1, 0, null);

    private static List<Design> load() {
        try (var r = new InputStreamReader(Thrown.class.getResourceAsStream("/data/" + DiaithirArms.ID + "/historical_thrown.json"))) {
            return new Gson().fromJson(r, new TypeToken<List<Design>>() {}.getType());
        } catch (Exception e) {
            throw new IllegalStateException("historical_thrown.json missing", e);
        }
    }

    static Design byId(String id) {
        return DESIGNS.stream().filter(d -> d.id().equals(id)).findFirst().orElseThrow();
    }

    static Item.Properties props(Design d) {
        return d.ammo() != null ? new Item.Properties().durability(d.durability()) : new Item.Properties().stacksTo(d.stack());
    }

    /** Throws itself, or fires ammo found in the inventory when the design names an ammo tag. */
    public static final class ThrownItem extends Item {
        private final Design design;

        public ThrownItem(Properties p, Design design) {
            super(p);
            this.design = design;
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack held = player.getItemInHand(hand);
            ItemStack shown = held;
            ItemStack ammo = ItemStack.EMPTY;
            if (design.ammo() != null) {
                TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DiaithirArms.ID, design.ammo()));
                for (ItemStack s : player.getInventory().getNonEquipmentItems()) {
                    if (s.is(tag)) { ammo = s; break; }
                }
                if (ammo.isEmpty() && !player.hasInfiniteMaterials()) return InteractionResult.FAIL;
                shown = ammo.isEmpty() ? new ItemStack(Items.FLINT) : ammo.copyWithCount(1);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            if (level instanceof ServerLevel sl) {
                Projectile.spawnProjectileFromRotation((lvl, src, stack) -> new Shot(DiaithirArms.THROWN.get(), lvl, src, stack, design.id()),
                    sl, shown, player, 0.0F, design.speed(), 1.0F);
            }
            if (design.ammo() != null) {
                if (!ammo.isEmpty() && !player.hasInfiniteMaterials()) ammo.shrink(1);
                held.hurtAndBreak(1, player, hand);
            } else {
                held.consume(1, player);
            }
            player.getCooldowns().addCooldown(held, design.cooldown());
            return InteractionResult.SUCCESS;
        }
    }

    /** The projectile. Fused kinds stick where they land and go off when the fuse runs out; the rest act on impact. */
    public static final class Shot extends ThrowableItemProjectile {
        private String designId = "grenado";
        private int fuse;
        private float sPower, sDamage;
        private boolean sBreaks, sFire;

        public Shot(EntityType<? extends Shot> type, Level level) {
            super(type, level);
        }

        public Shot(EntityType<? extends Shot> type, Level level, LivingEntity owner, ItemStack shown, String designId) {
            super(type, owner, level, shown);
            this.designId = designId;
            this.fuse = byId(designId).fuse();
        }

        /** A shot from a siege engine: impact damage and an explosion of the given radius (0 for a plain bolt). */
        public Shot(EntityType<? extends Shot> type, Level level, LivingEntity owner, ItemStack shown, float damage, float power, boolean breaks, boolean fire) {
            super(type, owner, level, shown);
            this.designId = "siege";
            this.sDamage = damage; this.sPower = power; this.sBreaks = breaks; this.sFire = fire;
        }

        private boolean siege() { return designId.equals("siege"); }

        private Design design() { return siege() ? SIEGE : byId(designId); }

        @Override
        protected Item getDefaultItem() { return Items.FLINT; }

        private boolean fused() { return design().fuse() > 0; }

        @Override
        public void tick() {
            super.tick();
            if (fused() && !level().isClientSide() && --fuse <= 0) detonate();
        }

        @Override
        protected void onHitEntity(EntityHitResult hit) {
            Design d = design();
            if (fused()) return;
            Entity e = hit.getEntity();
            if (siege()) {
                e.hurt(damageSources().thrown(this, getOwner()), sDamage);
                if (sFire) e.igniteForSeconds(5);
                if (!level().isClientSide()) { if (sPower > 0) detonate(); else discard(); }
                return;
            }
            DamageSource src = damageSources().thrown(this, getOwner());
            if (d.damage() > 0) e.hurt(src, d.damage());
            if (e instanceof LivingEntity le && d.effectTicks() > 0) {
                var effect = d.kind().equals("blowgun") ? MobEffects.POISON : MobEffects.SLOWNESS;
                le.addEffect(new MobEffectInstance(effect, d.effectTicks(), d.effectAmp()));
            }
            if (!level().isClientSide()) {
                if (d.kind().equals("molotov")) detonate(); else discard();
            }
        }

        @Override
        protected void onHitBlock(BlockHitResult hit) {
            super.onHitBlock(hit);
            if (level().isClientSide()) return;
            if (siege()) {
                if (sPower > 0) detonate(); else discard();
            } else if (fused()) {
                setDeltaMovement(getDeltaMovement().scale(0.1));
            } else if (design().kind().equals("molotov")) {
                detonate();
            } else {
                discard();
            }
        }

        @Override
        protected void onHit(HitResult hit) {
            if (fused() && hit.getType() == HitResult.Type.ENTITY) {
                setDeltaMovement(getDeltaMovement().scale(-0.2));
                return;
            }
            super.onHit(hit);
        }

        private void detonate() {
            if (!(level() instanceof ServerLevel sl)) return;
            Design d = design();
            if (siege()) {
                sl.explode(this, getX(), getY(), getZ(), sPower, sFire, sBreaks ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
            } else if (d.kind().equals("molotov")) {
                for (Entity e : sl.getEntities(this, new AABB(blockPosition()).inflate(2.5))) e.igniteForSeconds(8);
                for (BlockPos p : BlockPos.betweenClosed(blockPosition().offset(-1, -1, -1), blockPosition().offset(1, 1, 1))) {
                    var fire = Blocks.FIRE.defaultBlockState();
                    if (sl.isEmptyBlock(p) && fire.canSurvive(sl, p)) sl.setBlockAndUpdate(p.immutable(), fire);
                }
            } else {
                sl.explode(this, getX(), getY(), getZ(), d.power(),
                    d.kind().equals("charge") ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE);
            }
            discard();
        }

        @Override
        protected void addAdditionalSaveData(ValueOutput out) {
            super.addAdditionalSaveData(out);
            out.putString("design", designId);
            out.putInt("fuse", fuse);
            out.putFloat("sPower", sPower); out.putFloat("sDamage", sDamage); out.putBoolean("sBreaks", sBreaks); out.putBoolean("sFire", sFire);
        }

        @Override
        protected void readAdditionalSaveData(ValueInput in) {
            super.readAdditionalSaveData(in);
            designId = in.getStringOr("design", "grenado");
            fuse = in.getIntOr("fuse", 0);
            sPower = in.getFloatOr("sPower", 0); sDamage = in.getFloatOr("sDamage", 0); sBreaks = in.getBooleanOr("sBreaks", false); sFire = in.getBooleanOr("sFire", false);
        }
    }

    private Thrown() {}
}
