package net.diaithir.druidry.ars.common.block.tile;

import net.diaithir.druidry.ars.api.ANFakePlayer;
import net.diaithir.druidry.ars.api.client.ITooltipProvider;
import net.diaithir.druidry.ars.api.item.IWandable;
import net.diaithir.druidry.ars.api.spell.EntitySpellResolver;
import net.diaithir.druidry.ars.api.spell.Spell;
import net.diaithir.druidry.ars.api.spell.SpellCaster;
import net.diaithir.druidry.ars.api.spell.SpellContext;
import net.diaithir.druidry.ars.api.spell.wrapped_caster.TileCaster;
import net.diaithir.druidry.ars.api.util.SourceUtil;
import net.diaithir.druidry.ars.common.block.BasicSpellTurret;
import net.diaithir.druidry.ars.common.block.ITickable;
import net.diaithir.druidry.ars.common.entity.EntityFollowProjectile;
import net.diaithir.druidry.ars.common.util.PortUtil;
import net.diaithir.druidry.ars.setup.registry.AttachmentsRegistry;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.diaithir.druidry.ars.setup.registry.CapabilityRegistry;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.*;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BasicSpellTurretTile extends ModdedTile implements ITooltipProvider, IAnimationListener, ITickable, ICapabilityProvider<BasicSpellTurretTile, Void, SpellCaster>, IWandable {
    boolean playRecoil;
    protected SpellCaster spellCaster = new SpellCaster(0, null, false, null, 1);
    @Nullable UUID uuid = null;

    public BasicSpellTurretTile(BlockEntityType<?> p_i48289_1_, BlockPos pos, BlockState state) {
        super(p_i48289_1_, pos, state);
    }

    public BasicSpellTurretTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.BASIC_SPELL_TURRET_TILE, pos, state);
    }

    public int getManaCost() {
        return this.spellCaster.getSpell().getCost();
    }

    public void setPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public void setSpell(Spell spell) {
        this.spellCaster = this.spellCaster.setSpell(spell, 0);
    }

    public void shootSpell() {
        BlockPos pos = this.getBlockPos();

        if (spellCaster.getSpell().isEmpty() || !(this.level instanceof ServerLevel level))
            return;
        int manaCost = getManaCost();
        if (manaCost > 0) {
            var sp = this.getLinkedSourceProvider();
            if (this.level instanceof ServerLevel serverLevel && sp != null) {
                var cap = this.level.getCapability(CapabilityRegistry.SOURCE_CAPABILITY, sp.first(), sp.second().orElse(null));
                if (cap == null) {
                    this.setLinkedSourceProvider(null);
                    return;
                }

                if (cap.extractSource(manaCost, true) < manaCost) {
                    return;
                }

                cap.extractSource(manaCost, false);
                EntityFollowProjectile.spawn(serverLevel, sp.first(), this.worldPosition);
            } else if (SourceUtil.takeSourceMultipleWithParticles(pos, level, 10, manaCost) == null) {
                return;
            }
        }

        Position iposition = BasicSpellTurret.getDispensePosition(pos, level.getBlockState(pos).getValue(BasicSpellTurret.FACING));
        Direction direction = level.getBlockState(pos).getValue(BasicSpellTurret.FACING);
        FakePlayer fakePlayer = uuid != null
                ? FakePlayerFactory.get(level, new GameProfile(uuid, ""))
                : ANFakePlayer.getPlayer(level);
        fakePlayer.setPos(pos.getX(), pos.getY(), pos.getZ());
        EntitySpellResolver resolver = new EntitySpellResolver(new SpellContext(level, spellCaster.getSpell(), fakePlayer, new TileCaster(this, SpellContext.CasterType.TURRET)));
        if (resolver.castType != null && BasicSpellTurret.TURRET_BEHAVIOR_MAP.containsKey(resolver.castType)) {
            BasicSpellTurret.TURRET_BEHAVIOR_MAP.get(resolver.castType).onCast(resolver, level, pos, fakePlayer, iposition, direction);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("spell_caster", SpellCaster.CODEC.codec(), spellCaster);
        if (uuid != null) {
            output.store("uuid", UUIDUtil.CODEC, uuid);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("spell_caster", SpellCaster.CODEC.codec()).ifPresent(c -> this.spellCaster = c);
        uuid = input.read("uuid", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    public void getTooltip(List<Component> tooltip) {
        tooltip.add(Component.translatable("ars_nouveau.spell_turret.casting"));
        if (!spellCaster.getSpellName().isEmpty()) {
            tooltip.add(Component.literal(spellCaster.getSpellName()));
        }
        tooltip.add(Component.literal(spellCaster.getSpell().getDisplayString()));
        var sp = this.getLinkedSourceProvider();
        if (sp != null) {
            tooltip.add(Component.translatable("ars_nouveau.spell_turret.linked_source_provider", sp.first().toShortString()));
        }
    }

    @Override
    public void startAnimation(int arg) {
        this.playRecoil = true;
    }

    @Override
    public @Nullable SpellCaster getCapability(BasicSpellTurretTile object, Void context) {
        return spellCaster;
    }

    @Override
    public Result onLastConnection(@Nullable GlobalPos storedPos, @Nullable Direction face, @Nullable LivingEntity storedEntity, Player playerEntity) {
        if (this.level == null || storedPos == null) {
            return Result.FAIL;
        }

        ResourceKey<Level> dim = storedPos.dimension();
        if (!this.level.dimension().equals(dim)) {
            PortUtil.sendMessageNoSpam(playerEntity, Component.translatable("ars_nouveau.connections.dimension_mismatch"));
            return Result.FAIL;
        }

        var diff = this.worldPosition.subtract(storedPos.pos());
        if (Math.abs(diff.getX()) > 10 || Math.abs(diff.getY()) > 10 || Math.abs(diff.getZ()) > 10) {
            PortUtil.sendMessageNoSpam(playerEntity, Component.translatable("ars_nouveau.connection.range", 10));
            return Result.FAIL;
        }

        var cap = this.level.getCapability(CapabilityRegistry.SOURCE_CAPABILITY, storedPos.pos(), face);
        if (cap == null || cap.getMaxExtract() <= 0) {
            return Result.FAIL;
        }

        var sp = this.getLinkedSourceProvider();
        if (sp != null && sp.first().equals(storedPos.pos()) && sp.second().equals(Optional.ofNullable(face))) {
            PortUtil.sendMessageNoSpam(playerEntity, Component.translatable("ars_nouveau.connections.remove"));
            this.setLinkedSourceProvider(null);
            return Result.SUCCESS;
        }

        this.setData(AttachmentsRegistry.LINKED_SOURCE_PROVIDER, Pair.of(storedPos.pos(), Optional.ofNullable(face)));
        PortUtil.sendMessageNoSpam(playerEntity, Component.translatable("ars_nouveau.connections.turret.take_from", storedPos.pos().toShortString()));

        return Result.SUCCESS;
    }

    @Nullable
    public Pair<BlockPos, Optional<Direction>> getLinkedSourceProvider() {
        return this.getExistingDataOrNull(AttachmentsRegistry.LINKED_SOURCE_PROVIDER);
    }

    public void setLinkedSourceProvider(@Nullable Pair<BlockPos, Optional<Direction>> sourceProvider) {
        if (sourceProvider == null) {
            this.removeData(AttachmentsRegistry.LINKED_SOURCE_PROVIDER);
            return;
        }

        this.setData(AttachmentsRegistry.LINKED_SOURCE_PROVIDER, sourceProvider);
    }

    public void setLinkedSourceProvider(BlockPos pos, @Nullable Direction face) {
        this.setLinkedSourceProvider(Pair.of(pos, Optional.ofNullable(face)));
    }
}
