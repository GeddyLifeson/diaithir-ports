package folk.sisby.antique_atlas.util;

import net.minecraft.client.renderer.Rect2i;
import org.joml.Vector2d;

public class MathUtil {
	/**
	 * Returns the nearest number to a multiple of a given number.
	 */
	public static int roundToBase(int a, int base) {
		return a - a % base;
	}

	/**
	 * Returns the nearest number to a multiple of a given number.
	 */
	public static int roundToBase(double a, double base) {
		return (int) (a - a % base);
	}

	public static double innerDistanceToEdge(Rect2i rect, Vector2d point) {
		var dx = Math.min(point.x - rect.getX(), rect.getX() + rect.getWidth() - point.x);
		var dy = Math.min(point.y - rect.getY(), rect.getY() + rect.getHeight() - point.y);
		return Math.min(dx, dy);
	}

	public static int hash3(int x, int y, int z) {
		long l = (long) (x * 3129871) ^ (long) z * 116129781L ^ (long) y;
		l = l * l * 42317861L + l * 11L;
		return (int) (l >> 16);
	}

	public static double distanceToSqr(double x1, double y1, double x2, double y2) {
		double dx = x1 - x2;
		double dy = y1 - y2;
		return dx * dx + dy * dy;
	}
}
