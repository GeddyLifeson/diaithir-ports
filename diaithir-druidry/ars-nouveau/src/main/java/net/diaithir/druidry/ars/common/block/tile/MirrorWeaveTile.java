package net.diaithir.druidry.ars.common.block.tile;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.diaithir.druidry.ars.api.spell.ILightable;
import net.diaithir.druidry.ars.api.spell.SpellContext;
import net.diaithir.druidry.ars.api.spell.SpellStats;
import net.diaithir.druidry.ars.common.block.MirrorWeave;
import net.diaithir.druidry.ars.common.spell.augment.AugmentDampen;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class MirrorWeaveTile extends ModdedTile implements ILightable {
    public BlockState mimicState;
    public BlockState nextState = BlockRegistry.MIRROR_WEAVE.defaultBlockState();

    public MirrorWeaveTile(BlockEntityType type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.mimicState = getDefaultBlockState();
    }

    public MirrorWeaveTile(BlockPos pos, BlockState state) {
        this(BlockRegistry.MIRROR_WEAVE_TILE.get(), pos, state);
    }





    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.put("mimic_state",  NbtUtils.writeBlockState(mimicState));
    }

    @Override
    protected void loadAdditional(ValueInput pTag) {
        super.loadAdditional(pTag);
        if(pTag.read("mimic_state", com.mojang.serialization.Codec.PASSTHROUGH).isPresent()) {
            HolderGetter<Block> holdergetter = this.level != null ? this.level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup();
            mimicState = NbtUtils.readBlockState(holdergetter, pTag.childOrEmpty("mimic_state"));
        }else{
            mimicState = getDefaultBlockState();
        }
    }


    public BlockState getDefaultBlockState(){
        return BlockRegistry.MIRROR_WEAVE.defaultBlockState();
    }

    @Override
    public void onLight(HitResult rayTraceResult, Level world, LivingEntity shooter, SpellStats stats, SpellContext spellContext) {
        if (rayTraceResult instanceof BlockHitResult) {
            BlockState state = world.getBlockState(((BlockHitResult) rayTraceResult).getBlockPos());
            world.setBlock(getBlockPos(), state.setValue(MirrorWeave.LIGHT_LEVEL, Math.min(Math.max(0, 15 - stats.getBuffCount(AugmentDampen.INSTANCE)), 15)), 3);
            world.sendBlockUpdated(((BlockHitResult) rayTraceResult).getBlockPos(), state,
                    state.setValue(MirrorWeave.LIGHT_LEVEL, Math.min(Math.max(0, 15 - stats.getBuffCount(AugmentDampen.INSTANCE)), 15)), 3);
        }
        updateBlock();
    }
}
