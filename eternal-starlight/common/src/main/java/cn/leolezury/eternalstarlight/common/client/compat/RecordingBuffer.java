package cn.leolezury.eternalstarlight.common.client.compat;

import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * A VertexConsumer that records vertices so they can be replayed later into the buffer handed out by
 * {@link net.minecraft.client.renderer.SubmitNodeCollector#submitCustomGeometry}.
 */
public class RecordingBuffer implements VertexConsumer {
	private static final int F_COLOR = 1, F_UV = 2, F_UV1 = 4, F_UV2 = 8, F_NORMAL = 16, F_LINE = 32;

	private static final class Vtx {
		float x, y, z, u, v, nx, ny, nz, lw;
		int color, uv1a, uv1b, uv2a, uv2b, flags;
	}

	private final List<Vtx> vertices = new ArrayList<>();
	private Vtx current;

	public boolean isEmpty() {
		return vertices.isEmpty();
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		current = new Vtx();
		current.x = x;
		current.y = y;
		current.z = z;
		vertices.add(current);
		return this;
	}

	@Override
	public VertexConsumer setColor(int r, int g, int b, int a) {
		return setColor((a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255));
	}

	@Override
	public VertexConsumer setColor(int argb) {
		if (current != null) {
			current.color = argb;
			current.flags |= F_COLOR;
		}
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		if (current != null) {
			current.u = u;
			current.v = v;
			current.flags |= F_UV;
		}
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		if (current != null) {
			current.uv1a = u;
			current.uv1b = v;
			current.flags |= F_UV1;
		}
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		if (current != null) {
			current.uv2a = u;
			current.uv2b = v;
			current.flags |= F_UV2;
		}
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		if (current != null) {
			current.nx = x;
			current.ny = y;
			current.nz = z;
			current.flags |= F_NORMAL;
		}
		return this;
	}

	@Override
	public VertexConsumer setLineWidth(float width) {
		if (current != null) {
			current.lw = width;
			current.flags |= F_LINE;
		}
		return this;
	}

	/** Replays the recorded vertices (already transformed by the pose stack at record time). */
	public void replay(VertexConsumer out) {
		for (Vtx v : vertices) {
			out.addVertex(v.x, v.y, v.z);
			if ((v.flags & F_COLOR) != 0) out.setColor(v.color);
			if ((v.flags & F_UV) != 0) out.setUv(v.u, v.v);
			if ((v.flags & F_UV1) != 0) out.setUv1(v.uv1a, v.uv1b);
			if ((v.flags & F_UV2) != 0) out.setUv2(v.uv2a, v.uv2b);
			if ((v.flags & F_NORMAL) != 0) out.setNormal(v.nx, v.ny, v.nz);
			if ((v.flags & F_LINE) != 0) out.setLineWidth(v.lw);
		}
	}
}
