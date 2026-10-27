package com.diaithir.addons.oreexcavation.menu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeHolder;

import com.mojang.datafixers.util.Pair;

import com.diaithir.addons.oreexcavation.CreateOreExcavation;
import com.diaithir.addons.oreexcavation.components.OreVeinAtlasDataComponent;
import com.diaithir.addons.oreexcavation.components.OreVeinAtlasDataComponent.OreVeinData;
import com.diaithir.addons.oreexcavation.network.NetworkHandler;
import com.diaithir.addons.oreexcavation.network.OreVeinAtlasClickPacket;
import com.diaithir.addons.oreexcavation.network.OreVeinAtlasClickPacket.Option;
import com.diaithir.addons.oreexcavation.recipe.OreRecipes;
import com.diaithir.addons.oreexcavation.recipe.VeinRecipe;
import com.diaithir.addons.oreexcavation.util.DimChunkPos;
import com.diaithir.addons.oreexcavation.util.NumberFormatter;
import com.diaithir.addons.oreexcavation.util.ThreeState;

/**
 * Ore vein atlas. Left: veins found with a sample drill (click to inspect, [H] hides).
 * Right: vein types discovered ([T] target, [X] exclude from the Ore Vein Finder).
 * Plain drawn panels (no sprite sheet) to match the machine-shop look of the pack.
 */
public class OreVeinAtlasScreen extends AbstractContainerScreen<OreVeinAtlasMenu> {
	private static final int ROW = 12, ROWS = 11;
	private static final int INK = 0xFFE8E0C8, DIM = 0xFF9A9482, PANEL = 0xFF2B2A26, EDGE = 0xFF6B5B3A, HOT = 0xFFF2B33D;

	private final List<Vein> veins = new ArrayList<>();
	private final List<RecipeHolder<VeinRecipe>> types = new ArrayList<>();
	private final Set<Identifier> excluded = new HashSet<>();
	private Identifier target;
	private Vein selected;
	private boolean showHidden;
	private int veinScroll, typeScroll;

	public OreVeinAtlasScreen(OreVeinAtlasMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, 282, 182);
	}

	@Override
	protected void init() {
		super.init();
		fillLists();
	}

	private void fillLists() {
		veins.clear();
		types.clear();
		excluded.clear();
		OreVeinAtlasDataComponent tag = menu.getHeldItem().get(CreateOreExcavation.ORE_VEIN_ATLAS_DATA_COMPONENT);
		if (tag == null)
			return;
		target = tag.target().orElse(null);
		var level = Minecraft.getInstance().level;
		for (Identifier v : tag.discovered()) {
			var h = OreRecipes.vein(level, v);
			if (h != null)
				types.add(h);
		}
		for (Pair<DimChunkPos, OreVeinData> e : tag.veins()) {
			var h = OreRecipes.vein(level, e.getSecond().id());
			if (h != null)
				veins.add(new Vein(e.getFirst(), h, e.getSecond().size(), e.getSecond().hide()));
		}
		excluded.addAll(tag.exclude());
	}

	private List<Vein> shownVeins() {
		return veins.stream().filter(v -> v.hidden == showHidden).toList();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mx, int my, float a) {
		super.extractBackground(g, mx, my, a);
		g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, EDGE);
		g.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, PANEL);
	}

	@Override
	public void extractContents(GuiGraphicsExtractor g, int mx, int my, float a) {
		g.text(font, title, leftPos + 20, topPos + 10, INK, false);
		Component tt = Component.translatable("info.coe.atlas.vein_types");
		g.text(font, tt, leftPos + 150, topPos + 10, INK, false);
		g.text(font, Component.literal(showHidden ? "[H] hidden" : "[H] shown"), leftPos + 100, topPos + 10, showHidden ? HOT : DIM, false);

		if (selected == null) {
			List<Vein> l = shownVeins();
			for (int i = 0; i < ROWS && i + veinScroll < l.size(); i++) {
				Vein v = l.get(i + veinScroll);
				g.text(font, v.recipe.value().getName(), leftPos + 20, topPos + 24 + i * ROW, v.hidden ? DIM : INK, false);
			}
		} else {
			VeinRecipe r = selected.recipe.value();
			g.item(r.getIcon(), leftPos + 20, topPos + 24);
			g.text(font, r.getName(), leftPos + 42, topPos + 28, INK, false);
			long size = veinSize();
			Component t = size == 0 ? Component.translatable("info.coe.atlas.vein_size.infinite") : Component.literal("~" + NumberFormatter.formatNumber(size));
			g.text(font, Component.translatable("info.coe.atlas.vein_size", t), leftPos + 20, topPos + 50, INK, false);
			g.text(font, Component.translatable("info.coe.atlas.location"), leftPos + 20, topPos + 62, INK, false);
			g.text(font, Component.translatable("info.coe.atlas.location2", Math.round((selected.pos.x() * 16 + 8) / 10f) * 10, Math.round((selected.pos.z() * 16 + 8) / 10f) * 10), leftPos + 30, topPos + 74, INK, false);
			g.text(font, Component.translatable("info.coe.atlas.dimension", selected.pos.dimension().identifier().toString()), leftPos + 20, topPos + 86, INK, false);
			g.text(font, Component.literal(selected.hidden ? "[H] unhide" : "[H] hide"), leftPos + 20, topPos + 104, HOT, false);
			g.text(font, Component.literal("[<] back"), leftPos + 20, topPos + 150, HOT, false);
		}

		for (int i = 0; i < ROWS && i + typeScroll < types.size(); i++) {
			var h = types.get(i + typeScroll);
			Identifier id = OreRecipes.idOf(h);
			boolean tgt = id.equals(target), ex = excluded.contains(id);
			g.text(font, Component.literal(tgt ? "[T]" : "[ ]"), leftPos + 150, topPos + 24 + i * ROW, tgt ? HOT : DIM, false);
			g.text(font, Component.literal(ex ? "[X]" : "[ ]"), leftPos + 170, topPos + 24 + i * ROW, ex ? 0xFFD0503A : DIM, false);
			g.text(font, h.value().getName(), leftPos + 190, topPos + 24 + i * ROW, ex ? DIM : INK, false);
		}
	}

	private long veinSize() {
		VeinRecipe r = selected.recipe.value();
		if (r.isFinite() != ThreeState.NEVER) {
			if (r.isFinite() == ThreeState.DEFAULT && menu.isDefaultInfinite())
				return 0L;
			double mul = (r.getMaxAmount() - r.getMinAmount()) * selected.size + r.getMinAmount();
			return Math.round(mul * menu.getFiniteBase());
		}
		return 0L;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent ev, boolean dbl) {
		int mx = (int) ev.x() - leftPos, my = (int) ev.y() - topPos;
		if (mx >= 98 && mx < 150 && my >= 8 && my < 20 && selected == null) {
			showHidden = !showHidden;
			veinScroll = 0;
			return true;
		}
		int row = (my - 24) / ROW;
		boolean inRows = my >= 24 && row >= 0 && row < ROWS;
		if (selected == null && inRows && mx >= 20 && mx < 140) {
			List<Vein> l = shownVeins();
			if (row + veinScroll < l.size()) {
				selected = l.get(row + veinScroll);
				return true;
			}
		} else if (selected != null) {
			if (mx >= 20 && mx < 80 && my >= 102 && my < 114) {
				selected.hidden = !selected.hidden;
				NetworkHandler.sendDataToServer(new OreVeinAtlasClickPacket(Option.TOGGLE_HIDE, veins.indexOf(selected)));
				return true;
			}
			if (mx >= 20 && mx < 80 && my >= 148 && my < 160) {
				selected = null;
				return true;
			}
		}
		if (inRows && mx >= 150 && row + typeScroll < types.size()) {
			var h = types.get(row + typeScroll);
			Identifier id = OreRecipes.idOf(h);
			if (mx < 170) {
				if (id.equals(target)) {
					target = null;
					NetworkHandler.sendDataToServer(new OreVeinAtlasClickPacket(Option.REMOVE_TARGET, (Identifier) null));
				} else {
					target = id;
					excluded.remove(id);
					NetworkHandler.sendDataToServer(new OreVeinAtlasClickPacket(Option.SET_TARGET, id));
				}
			} else if (mx < 190) {
				if (excluded.remove(id)) {
					NetworkHandler.sendDataToServer(new OreVeinAtlasClickPacket(Option.REMOVE_EXCLUDE, id));
				} else {
					excluded.add(id);
					if (id.equals(target))
						target = null;
					NetworkHandler.sendDataToServer(new OreVeinAtlasClickPacket(Option.ADD_EXCLUDE, id));
				}
			}
			return true;
		}
		return super.mouseClicked(ev, dbl);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double sx, double sy) {
		int mx = (int) x - leftPos;
		if (mx >= 150) {
			typeScroll = Math.max(0, Math.min(Math.max(0, types.size() - ROWS), typeScroll - (int) Math.signum(sy)));
		} else if (selected == null) {
			veinScroll = Math.max(0, Math.min(Math.max(0, shownVeins().size() - ROWS), veinScroll - (int) Math.signum(sy)));
		}
		return true;
	}

	private static class Vein {
		final DimChunkPos pos;
		final RecipeHolder<VeinRecipe> recipe;
		final float size;
		boolean hidden;

		Vein(DimChunkPos pos, RecipeHolder<VeinRecipe> recipe, float size, boolean hidden) {
			this.pos = pos;
			this.recipe = recipe;
			this.size = size;
			this.hidden = hidden;
		}
	}
}
