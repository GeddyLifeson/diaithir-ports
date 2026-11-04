package me.desht.pneumaticcraft.client.model.custom;

import me.desht.pneumaticcraft.common.block.AbstractCamouflageBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.List;

/**
 * Wraps the normal model of a camouflageable block; when the block entity reports a camouflage state, the model
 * of that state is rendered instead.
 */
public class CamoBlockStateModel implements DynamicBlockStateModel {
    private final BlockStateModel original;

    public CamoBlockStateModel(BlockStateModel original) {
        this.original = original;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        ModelData data = level.getModelData(pos);
        BlockState camo = data == null ? null : data.get(AbstractCamouflageBlock.CAMO_STATE);
        if (camo != null && !camo.isAir()) {
            BlockStateModel m = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(camo);
            m.collectParts(level, pos, camo, random, parts);
        } else {
            original.collectParts(level, pos, state, random, parts);
        }
    }

    @Override
    public Material.Baked particleMaterial() {
        return original.particleMaterial();
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        return original.materialFlags();
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        ModelData data = level.getModelData(pos);
        BlockState camo = data == null ? null : data.get(AbstractCamouflageBlock.CAMO_STATE);
        if (camo != null && !camo.isAir()) {
            return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(camo).materialFlags();
        }
        return original.materialFlags();
    }
}
