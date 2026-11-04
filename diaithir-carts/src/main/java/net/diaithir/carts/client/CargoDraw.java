package net.diaithir.carts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.diaithir.freight.Freight;
import net.diaithir.freight.LoadType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws the loads standing on a rail wagon (client only). The wagon's synced view string lists each load's type and cell; each load is the block of its
 * type (a pallet, barrel, crate or chest block, or a container drawn as one solid steel, reefer or tank block) scaled to its cells. The loads are drawn in the
 * frame the cart draws its own display block in: x along the rail, z across it, y up.
 */
public final class CargoDraw {
    private CargoDraw() {}

    /** One load to draw: its resolved model, where it starts and how big it is, in blocks, in the cart's frame. */
    public record Piece(BlockModelRenderState model, float x, float y, float z, float sx, float sy, float sz) {}

    /** Cells are drawn at this size: container wagons smaller, since a container is four cells wide. */
    static float cell(int w) { return w >= 4 ? 0.24F : 0.32F; }

    /** Floor of the cart bed, in the cart's frame. */
    private static final float BED = -0.12F;
    private static final int MAX_PIECES = 96;

    private static Block blockOf(LoadType t) {
        return switch (t.kind()) {
            case CONTAINER -> Freight.WALL.get();
            case REEFER -> Freight.REEFER_UNIT.get();
            case TANK -> Freight.TANK_VESSEL.get();
            default -> Freight.LOADS.get(t.id()).get();
        };
    }

    /** Parses the synced view ("w,h,l|type,x,y,z,turned;...") into pieces; {@code classic} is the older minecart movement, which draws the cart turned half a circle. */
    public static List<Piece> pieces(String view, boolean classic, BlockModelResolver resolver) {
        List<Piece> out = new ArrayList<>();
        int bar = view.indexOf('|');
        if (bar < 0 || bar == view.length() - 1) return out;
        String[] dims = view.substring(0, bar).split(",");
        int w, l;
        try {
            w = Integer.parseInt(dims[0]);
            l = Integer.parseInt(dims[2]);
        } catch (RuntimeException e) {
            return out;
        }
        float s = cell(w);
        for (String entry : view.substring(bar + 1).split(";")) {
            if (entry.isEmpty() || out.size() >= MAX_PIECES) continue;
            try {
                String[] f = entry.split(",");
                LoadType t = LoadType.byId(f[0]);
                if (t == null) continue;
                int x = Integer.parseInt(f[1]), y = Integer.parseInt(f[2]), z = Integer.parseInt(f[3]);
                boolean turned = f[4].equals("1");
                int lw = turned ? t.l() : t.w(), ll = turned ? t.w() : t.l();
                int cz = classic ? l - z - ll : z, cx = classic ? w - x - lw : x;
                BlockModelRenderState m = new BlockModelRenderState();
                BlockState state = blockOf(t).defaultBlockState();
                resolver.update(m, state, AbstractMinecartRenderer.BLOCK_DISPLAY_CONTEXT);
                out.add(new Piece(m, (cz - l / 2.0F) * s, BED + y * s, (cx - w / 2.0F) * s, ll * s, t.h() * s, lw * s));
            } catch (RuntimeException e) {
                // a malformed entry is skipped, never a crash in the render loop
            }
        }
        return out;
    }

    public static void submit(List<Piece> pieces, PoseStack pose, SubmitNodeCollector collector, int light, int outline) {
        for (Piece p : pieces) {
            pose.pushPose();
            pose.translate(p.x(), p.y(), p.z());
            pose.scale(p.sx(), p.sy(), p.sz());
            p.model().submit(pose, collector, light, OverlayTexture.NO_OVERLAY, outline);
            pose.popPose();
        }
    }
}
