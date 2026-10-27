package com.diaithir.addons.oreexcavation.item;

import com.diaithir.addons.oreexcavation.recipe.OreRecipes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import com.diaithir.addons.oreexcavation.CreateOreExcavation;
import com.diaithir.addons.oreexcavation.Registration;
import com.diaithir.addons.oreexcavation.components.OreVeinAtlasDataComponent.OreVeinAtlasData;
import com.diaithir.addons.oreexcavation.components.OreVeinAtlasDataComponent.OreVeinData;
import com.diaithir.addons.oreexcavation.menu.OreVeinAtlasMenu;
import com.diaithir.addons.oreexcavation.network.OreVeinAtlasClickPacket.Option;
import com.diaithir.addons.oreexcavation.recipe.VeinRecipe;
import com.diaithir.addons.oreexcavation.util.DimChunkPos;
import com.diaithir.addons.oreexcavation.util.PlatformMenuProvider;

public class OreVeinAtlasItem extends Item implements PlatformMenuProvider {
	public static final String DISCOVERED = "discovered";
	public static final String EXCLUDE = "exclude";
	public static final String VEINS = "veins";
	public static final String TARGET = "veinTarget";
	public static final String SIZE = "size";
	public static final String DIMENSION = "dim";
	public static final String POS_X = "x";
	public static final String POS_Z = "z";
	public static final String VEIN_ID = "id";
	public static final String HIDE = "hide";

	public OreVeinAtlasItem(Properties p_41383_) {
		super(p_41383_);
	}

	public void addVein(Player player, ItemStack is, RecipeHolder<VeinRecipe> vein, DimChunkPos pos, float randomMul) {
		player.sendSystemMessage(Component.translatable("chat.coe.sampleDrill.addedToAtlas"));

		OreVeinAtlasData comp = new OreVeinAtlasData(is.get(CreateOreExcavation.ORE_VEIN_ATLAS_DATA_COMPONENT));
		if (!comp.discovered().contains(OreRecipes.idOf(vein))) {
			comp.addDiscovered(OreRecipes.idOf(vein));
		}

		var vn = comp.veins().get(pos);
		if (vn == null || !vn.id().equals(OreRecipes.idOf(vein))) {
			comp.addVein(pos, new OreVeinData(OreRecipes.idOf(vein), randomMul, false));
		}

		if (comp.isEdited()) {
			is.set(CreateOreExcavation.ORE_VEIN_ATLAS_DATA_COMPONENT, comp.finish());
		}
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		if (!world.isClientSide()) {
			player.openMenu(this);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
		ItemStack heldItem = pPlayer.getMainHandItem();
		return new OreVeinAtlasMenu(Registration.VEIN_ATLAS_MENU.get(), pContainerId, pPlayerInventory, heldItem);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("item." + CreateOreExcavation.MODID + ".vein_atlas");
	}

	public void menuClicked(ItemStack is, Option opt, Identifier id) {
		OreVeinAtlasData comp = new OreVeinAtlasData(is.get(CreateOreExcavation.ORE_VEIN_ATLAS_DATA_COMPONENT));

		switch (opt) {
		case ADD_EXCLUDE:
			comp.addExclude(id);
			break;

		case REMOVE_EXCLUDE:
			comp.removeExclude(id);
			break;

		case SET_TARGET:
			comp.setTarget(id);
			comp.removeExclude(id);
			break;

		default:
			break;
		}

		if (comp.isEdited()) {
			is.set(CreateOreExcavation.ORE_VEIN_ATLAS_DATA_COMPONENT, comp.finish());
		}
	}

	public void menuClicked2(ItemStack is, Option opt, int id) {
		OreVeinAtlasData comp = new OreVeinAtlasData(is.get(CreateOreExcavation.ORE_VEIN_ATLAS_DATA_COMPONENT));

		switch (opt) {
		case TOGGLE_HIDE:
			comp.toggleHide(id);
			break;

		case REMOVE_TARGET:
			comp.setTarget(null);
			break;

		default:
			break;
		}

		if (comp.isEdited()) {
			is.set(CreateOreExcavation.ORE_VEIN_ATLAS_DATA_COMPONENT, comp.finish());
		}
	}
}
