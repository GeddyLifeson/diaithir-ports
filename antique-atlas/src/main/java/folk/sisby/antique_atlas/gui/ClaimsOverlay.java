package folk.sisby.antique_atlas.gui;

import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.compat.FtbChunksCompat;
import folk.sisby.antique_atlas.util.Gfx;
import folk.sisby.surveyor.client.SurveyorClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * Draws FTB Chunks chunk claims and party members on the atlas.
 * Claims are tinted with the owner's claim colour, with a stronger edge where the owner changes.
 * Hovering a claim shows its owner. Toggled with the Claims bookmark.
 */
public class ClaimsOverlay implements AtlasOverlay {
	public static final Identifier ID = AntiqueAtlas.id("opac_claims");
	public static boolean visible = true;
	/** Claim manager mode: clicks on the map claim, unclaim, load or unload chunks. */
	public static boolean managing = false;
	private static final int MAX_CHUNKS = 30_000;

	@Override
	public void onScreenRender(AtlasScreenRenderContext ctx) {
		if (!visible || !FtbChunksCompat.available()) return;
		AtlasScreen screen = ctx.screen();
		Identifier dim = screen.dim().identifier();
		FtbChunksCompat.View claims = FtbChunksCompat.view(screen.dim());
		var g = ctx.context();

		int minCx = screen.screenXToWorldX(screen.bookX()) >> 4;
		int maxCx = screen.screenXToWorldX(screen.bookX() + screen.bookWidth()) >> 4;
		int minCz = screen.screenYToWorldZ(screen.bookY()) >> 4;
		int maxCz = screen.screenYToWorldZ(screen.bookY() + screen.bookHeight()) >> 4;
		int w = maxCx - minCx + 1;
		int h = maxCz - minCz + 1;
		float effectiveScale = (float) (screen.mapScale() / screen.guiScale());
		int size = screen.tilePixels() / screen.tileChunks();
		if (w > 0 && h > 0 && (long) w * h <= MAX_CHUNKS && size > 0) {
			FtbChunksCompat.Claim[][] grid = new FtbChunksCompat.Claim[w + 2][h + 2];
			for (int x = -1; x <= w; x++) for (int z = -1; z <= h; z++) grid[x + 1][z + 1] = claims == null ? null : claims.at(minCx + x, minCz + z);
			int line = Math.max(1, size / 8);
			for (int x = 0; x < w; x++) {
				for (int z = 0; z < h; z++) {
					FtbChunksCompat.Claim c = grid[x + 1][z + 1];
					if (c == null) continue;
					double sx = screen.worldXToScreenX((minCx + x) << 4) - screen.bookX();
					double sy = screen.worldZToScreenY((minCz + z) << 4) - screen.bookY();
					int rgb = c.argb() & 0xFFFFFF;
					int fill = ARGB.color(70, rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
					int edge = ARGB.color(190, rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
					g.pose().pushMatrix();
					g.pose().translate((float) sx, (float) sy);
					g.pose().scale(effectiveScale, effectiveScale);
					Gfx.fill(g, 0, 0, size, size, fill);
					if (!same(c, grid[x][z + 1])) Gfx.fill(g, 0, 0, line, size, edge);
					if (!same(c, grid[x + 2][z + 1])) Gfx.fill(g, size - line, 0, size, size, edge);
					if (!same(c, grid[x + 1][z])) Gfx.fill(g, 0, 0, size, line, edge);
					if (!same(c, grid[x + 1][z + 2])) Gfx.fill(g, 0, size - line, size, size, edge);
					g.pose().popMatrix();
				}
			}
			int mx = ctx.mouseX();
			int my = ctx.mouseY();
			if (screen.isMouseOverMap) {
				FtbChunksCompat.Claim hover = claims == null ? null : claims.at(screen.screenXToWorldX(mx) >> 4, screen.screenYToWorldZ(my) >> 4);
				if (hover != null && screen.hoveredLandmark == null) {
					g.setTooltipForNextFrame(Minecraft.getInstance().font, List.of(hover.name(), Component.translatable("gui.antique_atlas.claim").withStyle(ChatFormatting.GRAY)), java.util.Optional.empty(), mx, my);
				}
			}
		}

		if (managing) {
			FtbChunksCompat.Counts n = FtbChunksCompat.counts();
			Component head = n == null ? Component.translatable("gui.antique_atlas.claims.manage") : Component.translatable("gui.antique_atlas.claims.counts", n.claimed(), n.maxClaimed(), n.loaded(), n.maxLoaded());
			var font = Minecraft.getInstance().font;
			int hx = AtlasRenderer.MAP_BORDER_WIDTH + 4;
			int hy = AtlasRenderer.MAP_BORDER_HEIGHT + 3;
			Gfx.fill(g, hx - 2, hy - 2, hx + font.width(head) + 2, hy + 10, 0xA0000000);
			g.text(font, head, hx, hy, 0xFFFFFFFF, true);
			Component hint = Component.translatable("gui.antique_atlas.claims.hint");
			Gfx.fill(g, hx - 2, hy + 11, hx + font.width(hint) + 2, hy + 23, 0xA0000000);
			g.text(font, hint, hx, hy + 13, 0xFFCCCCCC, true);
		}

		for (FtbChunksCompat.Member m : FtbChunksCompat.teamMembers()) {
			double px = Mth.clamp(screen.worldXToScreenX(m.x()) - screen.bookX(), AtlasRenderer.MAP_BORDER_WIDTH, screen.mapWidth() + AtlasRenderer.MAP_BORDER_WIDTH);
			double py = Mth.clamp(screen.worldZToScreenY(m.z()) - screen.bookY(), AtlasRenderer.MAP_BORDER_HEIGHT, screen.mapHeight() + AtlasRenderer.MAP_BORDER_HEIGHT);
			Gfx.drawCenteredWithRotation(g, AtlasRenderer.PLAYER, px, py, screen.getEffectiveScale(), AtlasRenderer.PLAYER_ICON_WIDTH, AtlasRenderer.PLAYER_ICON_HEIGHT, 0, ARGB.color(255, 120, 220, 255));
			double dx = ctx.mouseX() - (px + screen.bookX());
			double dy = ctx.mouseY() - (py + screen.bookY());
			if (dx * dx + dy * dy < 36) {
				g.setTooltipForNextFrame(Minecraft.getInstance().font, Component.literal(m.name()).withStyle(ChatFormatting.AQUA), ctx.mouseX(), ctx.mouseY());
			}
		}
	}

	private static boolean same(FtbChunksCompat.Claim a, FtbChunksCompat.Claim b) {
		return b != null && a.team().equals(b.team());
	}
}
