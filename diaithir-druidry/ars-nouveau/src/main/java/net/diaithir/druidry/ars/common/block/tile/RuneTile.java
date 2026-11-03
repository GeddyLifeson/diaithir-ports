package net.diaithir.druidry.ars.common.block.tile;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.ANFakePlayer;
import net.diaithir.druidry.ars.api.client.ITooltipProvider;
import net.diaithir.druidry.ars.api.source.ISpecialSourceProvider;
import net.diaithir.druidry.ars.api.spell.EntitySpellResolver;
import net.diaithir.druidry.ars.api.spell.Spell;
import net.diaithir.druidry.ars.api.spell.SpellContext;
import net.diaithir.druidry.ars.api.spell.wrapped_caster.RuneCaster;
import net.diaithir.druidry.ars.api.util.IWololoable;
import net.diaithir.druidry.ars.api.util.SourceUtil;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.common.block.ITickable;
import net.diaithir.druidry.ars.common.block.RuneBlock;
import net.diaithir.druidry.ars.common.spell.method.MethodTouch;
import net.diaithir.druidry.ars.common.util.ANCodecs;
import net.diaithir.druidry.ars.common.util.PortUtil;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.diaithir.druidry.ars.setup.registry.ModPotions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.UUID;


public class RuneTile extends ModdedTile implements ITickable, ITooltipProvider, IWololoable {
    public Spell spell = new Spell();
    public boolean isTemporary;
    public boolean disabled;
    public boolean isCharged;
    public boolean isSensitive;
    public int ticksUntilCharge;
    public UUID uuid;
    public Entity touchedEntity;

    public RuneTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.RUNE_TILE, pos, state);
        isCharged = true;
        isTemporary = false;
        disabled = false;
        ticksUntilCharge = 0;
    }

    public void setSpell(Spell spell) {
        this.spell = spell;
        updateBlock();
    }

    public void castSpell(Entity entity) {
        if (entity == null)
            return;
        if (!this.isCharged || spell.isEmpty() || !(level instanceof ServerLevel) || !(spell.get(0) instanceof MethodTouch))
            return;
        if (!this.isTemporary && this.disabled) return;
        try {

            Player playerEntity = uuid != null ? level.getPlayerByUUID(uuid) : ANFakePlayer.getPlayer((ServerLevel) level);
            playerEntity = !this.isSensitive || playerEntity == null ? ANFakePlayer.getPlayer((ServerLevel) level) : playerEntity;
            EntitySpellResolver resolver = new EntitySpellResolver(new SpellContext(entity.level(), spell, playerEntity, new RuneCaster(this, SpellContext.CasterType.RUNE)));
            resolver.onCastOnEntity(ItemStack.EMPTY, entity, InteractionHand.MAIN_HAND);
            if (this.isTemporary) {
                level.destroyBlock(worldPosition, false);
                return;
            }
            this.isCharged = false;

            level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).cycle(RuneBlock.POWERED));
            ticksUntilCharge = 20 * 2;
            updateBlock();
        } catch (Exception e) {
            PortUtil.sendMessage(entity, Component.translatable("ars_nouveau.rune.error"));
            e.printStackTrace();
            level.destroyBlock(worldPosition, false);
        }
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.put("spell", ANCodecs.encode(Spell.CODEC.codec(), spell));
        tag.putBoolean("charged", isCharged);
        tag.putBoolean("redstone", disabled);
        tag.putBoolean("temp", isTemporary);
        tag.putInt("cooldown", ticksUntilCharge);
        if (uuid != null)
            tag.store("uuid", net.minecraft.core.UUIDUtil.CODEC, uuid);
        tag.putBoolean("sensitive", isSensitive);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        this.spell = ANCodecs.decode(Spell.CODEC.codec(), tag.childOrEmpty("spell"));
        this.isCharged = tag.getBooleanOr("charged", false);
        this.disabled = tag.getBooleanOr("redstone", false);
        this.isTemporary = tag.getBooleanOr("temp", false);
        this.ticksUntilCharge = tag.getIntOr("cooldown", 0);
        if (tag.read("uuid", com.mojang.serialization.Codec.PASSTHROUGH).isPresent())
            this.uuid = tag.read("uuid", net.minecraft.core.UUIDUtil.CODEC).orElse(null);
        this.isSensitive = tag.getBooleanOr("sensitive", false);
    }

    @Override
    public void tick() {

        if (level == null)
            return;
        if (!level.isClientSide()) {
            if (ticksUntilCharge > 0) {
                ticksUntilCharge -= 1;
                return;
            }
        }
        if (this.isCharged)
            return;
        if (!level.isClientSide() && this.isTemporary) {
            level.destroyBlock(this.worldPosition, false);
        }
        if (!level.isClientSide()) {
            ISpecialSourceProvider provider = SourceUtil.takeSourceWithParticles(worldPosition, level, 10, 100);
            if (provider != null) {
                this.isCharged = true;
                level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).cycle(RuneBlock.POWERED));
                updateBlock();
            } else {
                ticksUntilCharge = 20 * 3;
                updateBlock();
            }
        }
    }





    @Override
    public void getTooltip(List<Component> tooltip) {
        if (ArsNouveau.proxy.getPlayer().hasEffect(ModPotions.MAGIC_FIND_EFFECT)) {
            tooltip.add(Component.literal(spell.getDisplayString()));
        }
    }

    @Override
    public void setColor(ParticleColor color) {
        this.spell = spell.withColor(color);
        updateBlock();
    }

    @Override
    public ParticleColor getColor() {
        return spell.color();
    }
}