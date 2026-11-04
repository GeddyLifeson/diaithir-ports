package net.diaithir.druidry.ars.common.block;

import net.diaithir.druidry.ars.api.spell.ILightable;
import net.diaithir.druidry.ars.api.spell.SpellContext;
import net.diaithir.druidry.ars.api.spell.SpellStats;
import net.diaithir.druidry.ars.common.spell.augment.AugmentDampen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class SourceLamp extends CopperBulbBlock implements ILightable {

    public static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light_level", 0, 15);

    public SourceLamp() {
        super(net.diaithir.druidry.ars.setup.registry.BlockRegistry.of().lightLevel((p) -> p.getValue(LIT) ? p.getValue(LIGHT_LEVEL) : 0));
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, Boolean.valueOf(false)).setValue(LIGHT_LEVEL, 15).setValue(POWERED, Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(LIT, POWERED, LIGHT_LEVEL);
    }

    @Override
    public void onLight(HitResult rayTraceResult, Level world, LivingEntity shooter, SpellStats augments, SpellContext spellContext) {
        if(rayTraceResult instanceof BlockHitResult blockHitResult){
            var state = world.getBlockState(blockHitResult.getBlockPos()).setValue(LIGHT_LEVEL, Math.min(Math.max(0, 15 - augments.getBuffCount(AugmentDampen.INSTANCE)), 15));
            world.setBlock(blockHitResult.getBlockPos(), state, 3);
        }
    }

    @Override
    protected int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos, net.minecraft.core.Direction direction) {
        return pLevel.getBlockState(pPos).getValue(LIT) ? pState.getValue(LIGHT_LEVEL) : 0;
    }
}
