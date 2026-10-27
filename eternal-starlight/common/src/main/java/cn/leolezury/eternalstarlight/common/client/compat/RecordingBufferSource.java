package cn.leolezury.eternalstarlight.common.client.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A MultiBufferSource for code written against the old immediate-mode API. Everything written is recorded
 * and handed to the {@link SubmitNodeCollector} as custom geometry when {@link #flush} is called.
 */
public class RecordingBufferSource implements MultiBufferSource {
	private record Entry(RenderType type, RecordingBuffer buffer) {
	}

	private final List<Entry> entries = new ArrayList<>();
	private RenderType lastType;
	private RecordingBuffer last;

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		if (last != null && lastType == renderType) {
			return last;
		}
		last = new RecordingBuffer();
		lastType = renderType;
		entries.add(new Entry(renderType, last));
		return last;
	}

	public void flush(SubmitNodeCollector collector, PoseStack poseStack) {
		for (Entry e : entries) {
			if (e.buffer.isEmpty()) continue;
			collector.submitCustomGeometry(poseStack, e.type, (pose, vc) -> e.buffer.replay(vc));
		}
		entries.clear();
		last = null;
		lastType = null;
	}

	/** Runs legacy rendering code and submits whatever it wrote. */
	public static void submit(SubmitNodeCollector collector, PoseStack poseStack, Consumer<MultiBufferSource> body) {
		RecordingBufferSource source = new RecordingBufferSource();
		body.accept(source);
		source.flush(collector, poseStack);
	}
}
