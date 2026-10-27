package cn.leolezury.eternalstarlight.common.client.posteffect;

/**
 * The seeking eye star field was a full screen legacy post chain fed with per-star uniform arrays.
 * That mechanism does not exist in 26.1, so the screen draws its stars with plain GUI primitives instead
 * and this overlay is a no-op kept for call compatibility.
 */
public class SeekingEyeOverlay {
	public static void render(float[] starPositions, float[] starRadii, float[] starColors, float[] starAlphas, float[] starPhases, float[] starRotations, float[] starRayCounts, float[] starRayLengths, int starCount, int hoveredIndex, float fade, float time, float partialTicks) {
	}

	public static void release() {
	}
}
