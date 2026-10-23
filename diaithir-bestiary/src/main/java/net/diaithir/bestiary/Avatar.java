package net.diaithir.bestiary;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/** A boss with a boss bar. Below the phase two health share it hits and moves harder and sends out a shockwave every five seconds. */
public class Avatar extends Creature {
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(Avatar.class, EntityDataSerializers.INT);
    private static final Identifier WRATH = Identifier.fromNamespaceAndPath(DiaithirBestiary.ID, "phase_two");
    private final ServerBossEvent bossEvent = new ServerBossEvent(UUID.randomUUID(), getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);

    public Avatar(EntityType<? extends Avatar> type, Level level) {
        super(type, level);
        bossEvent.setDarkenScreen(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(PHASE, 1);
    }

    public int phase() {
        return entityData.get(PHASE);
    }

    public ServerBossEvent bossEvent() {
        return bossEvent;
    }

    @Override
    protected void onKindSet() {
        super.onKindSet();
        bossEvent.setName(getDisplayName());
        if (def() != null) bossEvent.setColor(def().bossOrDefault().color());
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        bossEvent.setProgress(getHealth() / getMaxHealth());
        if (def() == null) return;
        if (phase() == 1 && getHealth() <= getMaxHealth() * def().bossOrDefault().phaseTwoAt()) enterPhaseTwo();
        if (phase() == 2 && tickCount % 100 == 0) shockwave(level);
    }

    private void enterPhaseTwo() {
        entityData.set(PHASE, 2);
        getAttribute(Attributes.ATTACK_DAMAGE).addOrReplacePermanentModifier(new AttributeModifier(WRATH, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        getAttribute(Attributes.MOVEMENT_SPEED).addOrReplacePermanentModifier(new AttributeModifier(WRATH, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        bossEvent.setColor(BossEvent.BossBarColor.RED);
        playSound(SoundEvents.WITHER_SPAWN, 2F, 1F);
        triggerAnim("main", "attack");
    }

    private void shockwave(ServerLevel level) {
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F;
        for (Player player : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(6), p -> !p.isSpectator() && !p.isCreative())) {
            player.hurtServer(level, damageSources().mobAttack(this), damage);
            Vec3 push = player.position().subtract(position()).normalize().scale(1.2);
            player.push(push.x, 0.5, push.z);
            player.hurtMarked = true;
        }
        playSound(SoundEvents.GENERIC_EXPLODE.value(), 1F, 0.6F);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(net.minecraft.network.chat.Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Phase", phase());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(PHASE, input.getIntOr("Phase", 1));
        if (hasCustomName()) bossEvent.setName(getDisplayName());
    }
}
