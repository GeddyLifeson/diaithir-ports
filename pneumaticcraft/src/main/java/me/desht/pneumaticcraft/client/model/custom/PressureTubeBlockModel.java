package me.desht.pneumaticcraft.client.model.custom;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.desht.pneumaticcraft.common.block.entity.tube.PressureTubeBlockEntity;
import me.desht.pneumaticcraft.common.util.DirectionUtil;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of the original PneumaticCraft pressure tube geometry loader: the tube is composed at render time from a
 * per-direction piece (disconnected face, connected arm or closed arm), chosen from the block entity's model data.
 */
public class PressureTubeBlockModel implements DynamicBlockStateModel {
    // DUNSWE order, matching the rotations used by the original geometry
    private static final int[][] ROT = {{180, 0}, {0, 0}, {90, 0}, {90, 180}, {90, 270}, {90, 90}};

    private final BlockStateModel[] disconnected;
    private final BlockStateModel[] connected;
    private final BlockStateModel[] closed;

    PressureTubeBlockModel(BlockStateModel[] disconnected, BlockStateModel[] connected, BlockStateModel[] closed) {
        this.disconnected = disconnected;
        this.connected = connected;
        this.closed = closed;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        ModelData data = level.getModelData(pos);
        Short props = data == null ? null : data.get(PressureTubeBlockEntity.CONNECTION_PROPERTY);
        byte packedClosed = props == null ? 0 : (byte) (props & 0xFF);
        byte packedConnected = props == null ? 0 : (byte) ((props & 0xFF00) >> 8);
        long seed = random.nextLong();
        for (Direction dir : DirectionUtil.VALUES) {
            int d = dir.get3DDataValue();
            BlockStateModel m;
            if (DirectionUtil.getDirectionBit(packedClosed, dir)) m = closed[d];
            else if (DirectionUtil.getDirectionBit(packedConnected, dir)) m = connected[d];
            else m = disconnected[d];
            random.setSeed(seed);
            m.collectParts(level, pos, state, random, parts);
        }
    }

    @Override
    public Material.Baked particleMaterial() {
        return disconnected[1].particleMaterial();
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        int flags = 0;
        for (BlockStateModel m : disconnected) flags |= m.materialFlags();
        for (BlockStateModel m : connected) flags |= m.materialFlags();
        for (BlockStateModel m : closed) flags |= m.materialFlags();
        return flags;
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return materialFlags();
    }

    public record Unbaked(String prefix) implements CustomUnbakedBlockStateModel {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                com.mojang.serialization.Codec.STRING.optionalFieldOf("prefix", "").forGetter(Unbaked::prefix)
        ).apply(i, Unbaked::new));

        private List<BlockStateModel.Unbaked> parts(String suffix) {
            List<BlockStateModel.Unbaked> list = new ArrayList<>();
            String pfx = prefix.isEmpty() ? "" : prefix + "_";
            for (int[] r : ROT) {
                JsonObject o = new JsonObject();
                o.addProperty("model", "pneumaticcraft:block/" + pfx + "pressure_tube_" + suffix);
                if (r[0] != 0) o.addProperty("x", r[0]);
                if (r[1] != 0) o.addProperty("y", r[1]);
                list.add(BlockStateModel.Unbaked.CODEC.parse(JsonOps.INSTANCE, o).getOrThrow());
            }
            return list;
        }

        @Override
        public BlockStateModel bake(ModelBaker baker) {
            return new PressureTubeBlockModel(bakeAll(parts("disconnected"), baker), bakeAll(parts("connected"), baker), bakeAll(parts("closed"), baker));
        }

        private static BlockStateModel[] bakeAll(List<BlockStateModel.Unbaked> l, ModelBaker baker) {
            return l.stream().map(u -> u.bake(baker)).toArray(BlockStateModel[]::new);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            for (String s : new String[]{"disconnected", "connected", "closed"}) {
                for (BlockStateModel.Unbaked u : parts(s)) u.resolveDependencies(resolver);
            }
        }

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return MAP_CODEC;
        }
    }
}
