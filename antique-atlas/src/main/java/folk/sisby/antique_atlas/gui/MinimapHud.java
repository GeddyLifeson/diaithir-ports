package folk.sisby.antique_atlas.gui;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.AntiqueAtlasMod;
import folk.sisby.antique_atlas.AntiqueAtlasServerConfig;
import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.antique_atlas.WorldAtlasData;
import folk.sisby.antique_atlas.util.Gfx;
import folk.sisby.antique_atlas.util.MinimapGeometry;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.GuiLayer;

import java.util.Map;

/**
 * Small circular map in a corner of the HUD, centred on the player.
 * Drawn only while no screen is open (so it never covers inventories or menus), the GUI is not hidden,
 * the server allows it, and the player carries a Compass Map in hand or inventory.
 */
public class MinimapHud implements GuiLayer {
	public static final Identifier ID = AntiqueAtlas.id("minimap");
	public static boolean hidden = false;
	private static final int BANDS = 14;
	private static final int FRAME = 0xFF3B2A1A;
	private static final int FRAME_LIGHT = 0xFFC9A66B;

	public static boolean hasCompassMap(Player player) {
		if (isCompassMap(player.getOffhandItem()) || isCompassMap(player.getMainHandItem())) return true;
		for (ItemStack s : player.getInventory().getNonEquipmentItems()) if (isCompassMap(s)) return true;
		return false;
	}

	private static boolean isCompassMap(ItemStack s) {
		return s.is(AntiqueAtlasMod.COMPASS_MAP.get());
	}

	/** The single place that decides whether the minimap may draw this frame. */
	public static boolean shouldDraw(Minecraft mc) {
		return mc.screen == null && mc.player != null && mc.level != null && !mc.options.hideGui && !hidden
			&& AntiqueAtlasServerConfig.minimapAllowed() && hasCompassMap(mc.player);
	}

	@Override
	public void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		if (!shouldDraw(mc)) return;
		WorldAtlasData data = WorldAtlasData.WORLDS.get(mc.level.dimension());
		int size = AntiqueAtlas.CONFIG.minimapSize;
		int pad = 8;
		int sw = g.guiWidth();
		int sh = g.guiHeight();
		int corner = AntiqueAtlas.CONFIG.minimapCorner;
		int x = (corner == 0 || corner == 2) ? sw - size - pad : pad;
		int y = (corner == 0 || corner == 1) ? pad : sh - size - pad;
		double guiScale = mc.getWindow().getGuiScale();
		int tilePixels = Integer.highestOneBit(Math.max(4, Math.min(16, AntiqueAtlas.CONFIG.minimapChunkPixels)));

		// backdrop (parchment) so unexplored area is not see-through
		for (int row = 0; row < size; row++) {
			int[] span = MinimapGeometry.rowSpan(size, row);
			if (span != null) Gfx.fill(g, x + span[0], y + row, x + span[1], y + row + 1, 0xFFD9C9A0);
		}

		if (data != null) {
			MinimapRenderer r = new MinimapRenderer(mc.player, data, mc.level.dimension(), x, y, size, tilePixels, guiScale);
			Gfx.tint(1, 1, 1, 1);
			for (int[] b : MinimapGeometry.bands(size, BANDS)) {
				g.enableScissor(x + b[0], y + b[1], x + b[2], y + b[3]);
				r.renderTiles(g, AtlasRenderer.MAX_LIGHT);
				g.disableScissor();
			}
			drawMarkers(g, r, data, x, y, size);
		}

		// frame: a dark pixel ring that follows the circle
		for (int row = 0; row < size; row++) {
			int[] span = MinimapGeometry.rowSpan(size, row);
			if (span == null) continue;
			int[] up = row > 0 ? MinimapGeometry.rowSpan(size, row - 1) : null;
			int[] down = row < size - 1 ? MinimapGeometry.rowSpan(size, row + 1) : null;
			if (up == null || down == null) {
				Gfx.fill(g, x + span[0], y + row, x + span[1], y + row + 1, FRAME);
				continue;
			}
			int leftEnd = Math.max(span[0], Math.max(up[0], down[0])) + 1;
			int rightStart = Math.min(span[1], Math.min(up[1], down[1])) - 1;
			Gfx.fill(g, x + span[0] - 1, y + row, x + leftEnd, y + row + 1, FRAME);
			Gfx.fill(g, x + rightStart, y + row, x + span[1] + 1, y + row + 1, FRAME);
		}

		// player arrow and north marker
		float yaw = mc.player.getYRot() + 180f;
		Gfx.drawCenteredWithRotation(g, AtlasRenderer.PLAYER, x + size / 2.0, y + size / 2.0, 1.0f, AtlasRenderer.PLAYER_ICON_WIDTH, AtlasRenderer.PLAYER_ICON_HEIGHT, yaw, 0xFFFFFFFF);
		g.text(mc.font, "N", x + size / 2 - mc.font.width("N") / 2, y + 3, FRAME_LIGHT, true);
		String coords = "%d, %d".formatted(mc.player.getBlockX(), mc.player.getBlockZ());
		g.text(mc.font, coords, x + size / 2 - mc.font.width(coords) / 2, y + size + 2, 0xFFFFFFFF, true);
	}

	private void drawMarkers(GuiGraphicsExtractor g, MinimapRenderer r, WorldAtlasData data, int x, int y, int size) {
		double cx = x + size / 2.0;
		double cy = y + size / 2.0;
		double rad = size / 2.0 - 3;
		for (Map.Entry<Landmark, MarkerTexture> e : data.getAllMarkers(1).entrySet()) {
			BlockPos pos = e.getKey().get(LandmarkComponentTypes.POS);
			if (pos == null) continue;
			double sx = r.worldXToScreenX(pos.getX());
			double sy = r.worldZToScreenY(pos.getZ());
			double dx = sx - cx;
			double dy = sy - cy;
			double dist = Math.sqrt(dx * dx + dy * dy);
			if (dist > rad) continue;
			Integer color = e.getKey().get(LandmarkComponentTypes.COLOR);
			int rgb = color == null ? 0xFFFFFF : color & 0xFFFFFF;
			int px = (int) Math.round(sx);
			int py = (int) Math.round(sy);
			Gfx.fill(g, px - 2, py - 2, px + 3, py + 3, FRAME);
			Gfx.fill(g, px - 1, py - 1, px + 2, py + 2, ARGB.color(255, rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF));
		}
	}
}
