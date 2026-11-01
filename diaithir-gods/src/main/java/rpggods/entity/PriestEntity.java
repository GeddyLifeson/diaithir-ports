package rpggods.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rpggods.RGEvents;
import rpggods.RPGGods;
import rpggods.deity.DeityHelper;
import rpggods.favor.IFavor;
import rpggods.util.Nbt;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * A villager-like priest (a Mob, not a Villager). It stays near the closest deity altar. Right-click with an empty hand
 * to hear the deity's name and an offering it likes; right-click holding an item to make an offering through the normal offering code.
 */
public class PriestEntity extends PathfinderMob {
    public static final double SEARCH_RANGE = 24.0D;
    public static final double LEASH_RANGE = 8.0D;

    public PriestEntity(final EntityType<? extends PriestEntity> type, final Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder registerAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new StayNearAltarGoal(this, 0.5D));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.4D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    /** The closest altar entity that has a deity, or empty **/
    public Optional<AltarEntity> findAltar() {
        List<AltarEntity> list = this.level().getEntitiesOfClass(AltarEntity.class,
                this.getBoundingBox().inflate(SEARCH_RANGE), a -> a.getDeity().isPresent());
        return list.stream().min(Comparator.comparingDouble(a -> a.distanceToSqr(this)));
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 vec) {
        if (hand != InteractionHand.MAIN_HAND || !this.isAlive() || player.isSpectator()) {
            return super.interact(player, hand, vec);
        }
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Optional<AltarEntity> altar = findAltar();
        if (altar.isEmpty()) {
            Nbt.msg(player, Component.translatable("entity.diaithir_gods.priest.no_altar"), false);
            return InteractionResult.CONSUME;
        }
        Identifier deity = altar.get().getDeity().get();
        Optional<IFavor> favor = RPGGods.getFavor(player);
        ItemStack held = player.getItemInHand(hand);
        if (favor.isPresent() && !held.isEmpty() && favor.get().getFavor(deity).isEnabled()) {
            Optional<ItemStack> result = RGEvents.onOffering(altar, deity, player, favor.get(), held, false);
            if (result.isPresent()) {
                player.setItemInHand(hand, result.get());
                return InteractionResult.CONSUME;
            }
        }
        // tell the player which deity this is and what it likes
        DeityHelper helper = RPGGods.DEITY_HELPER.get(deity);
        Component name = DeityHelper.getName(deity);
        Component msg;
        if (helper != null && !helper.offeringMap.isEmpty()) {
            Identifier itemId = helper.offeringMap.keySet().stream().sorted().findFirst().get();
            Component itemName = BuiltInRegistries.ITEM.getValue(itemId).getDefaultInstance().getHoverName();
            msg = Component.translatable("entity.diaithir_gods.priest.speak", name, itemName);
        } else {
            msg = Component.translatable("entity.diaithir_gods.priest.speak_none", name);
        }
        Nbt.msg(player, msg, false);
        return InteractionResult.CONSUME;
    }

    /** Walk back toward the nearest altar when farther than LEASH_RANGE from it **/
    private static class StayNearAltarGoal extends Goal {
        private final PriestEntity priest;
        private final double speed;
        private BlockPos target;
        private int cooldown;

        StayNearAltarGoal(PriestEntity priest, double speed) {
            this.priest = priest;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (--cooldown > 0) return false;
            cooldown = 40;
            Optional<AltarEntity> altar = priest.findAltar();
            if (altar.isEmpty() || altar.get().distanceToSqr(priest) <= LEASH_RANGE * LEASH_RANGE) return false;
            target = altar.get().blockPosition();
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return !priest.getNavigation().isDone();
        }

        @Override
        public void start() {
            priest.getNavigation().moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 2.5D, speed);
        }
    }
}
