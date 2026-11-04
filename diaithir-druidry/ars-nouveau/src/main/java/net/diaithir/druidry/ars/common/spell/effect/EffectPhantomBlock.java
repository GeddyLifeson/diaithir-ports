package net.diaithir.druidry.ars.common.spell.effect;


import net.diaithir.druidry.ars.api.ANFakePlayer;
import net.diaithir.druidry.ars.api.spell.*;
import net.diaithir.druidry.ars.api.util.BlockUtil;
import net.diaithir.druidry.ars.api.util.SpellUtil;
import net.diaithir.druidry.ars.common.block.MageBlock;
import net.diaithir.druidry.ars.common.block.tile.MageBlockTile;
import net.diaithir.druidry.ars.common.items.curios.ShapersFocus;
import net.diaithir.druidry.ars.common.lib.GlyphLib;
import net.diaithir.druidry.ars.common.spell.augment.*;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class EffectPhantomBlock extends AbstractEffect {
    public static EffectPhantomBlock INSTANCE = new EffectPhantomBlock();

    private EffectPhantomBlock() {
        super(GlyphLib.EffectPhantomBlockID, "Conjure Mageblock");
    }


    @Override
    public void onResolveBlock(BlockHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        ANFakePlayer fakePlayer = ANFakePlayer.getPlayer((ServerLevel) world);
        for (BlockPos pos : SpellUtil.calcAOEBlocks(shooter, rayTraceResult.getBlockPos(), rayTraceResult, spellStats)) {
            pos = rayTraceResult.isInside() ? pos : pos.relative((rayTraceResult).getDirection());
            if(!world.isInWorldBounds(pos))
                continue;
            if (!BlockUtil.destroyRespectsClaim(getPlayer(shooter, (ServerLevel) world), world, pos))
                continue;
            BlockState state = world.getBlockState(pos);
            if (state.canBeReplaced() && world.isUnobstructed(BlockRegistry.MAGE_BLOCK.get().defaultBlockState(), pos, CollisionContext.of(fakePlayer))) {

                world.setBlockAndUpdate(pos, BlockRegistry.MAGE_BLOCK.get().defaultBlockState().setValue(MageBlock.TEMPORARY, !spellStats.hasBuff(AugmentAmplify.INSTANCE)));
                if (world.getBlockEntity(pos) instanceof MageBlockTile tile) {
                    tile.color = spellContext.getColors();
                    tile.lengthModifier = spellStats.getDurationMultiplier();
                    tile.isPermanent = spellStats.hasBuff(AugmentAmplify.INSTANCE);
                    world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 2);
                    ShapersFocus.tryPropagateBlockSpell(new BlockHitResult(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                            rayTraceResult.getDirection(), pos, false), world, shooter, spellContext, resolver);
                }
            }
        }
    }

    @Override
    protected void addDefaultAugmentLimits(Map<Identifier, Integer> defaults) {
        defaults.put(AugmentAmplify.INSTANCE.getRegistryName(), 1);
    }

    @Override
    public int getDefaultManaCost() {
        return 5;
    }

   @NotNull
    @Override
    public Set<AbstractAugment> getCompatibleAugments() {
        return augmentSetOf(AugmentAOE.INSTANCE, AugmentPierce.INSTANCE, AugmentAmplify.INSTANCE, AugmentExtendTime.INSTANCE, AugmentDurationDown.INSTANCE);
    }

    @Override
    public String getBookDescription() {
        return "Creates a temporary block that will disappear after a short time. Amplify will cause the block to be permanent. Dispelling this block will destroy it instantly.";
    }

   @NotNull
    @Override
    public Set<SpellSchool> getSchools() {
        return setOf(SpellSchools.CONJURATION);
    }
}
