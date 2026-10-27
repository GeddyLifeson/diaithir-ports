package cn.leolezury.eternalstarlight.common.item.combat;

import cn.leolezury.eternalstarlight.common.util.ESConventionalTags;
import cn.leolezury.eternalstarlight.common.util.ESTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/** PORT: Tier no longer exists in 26.1; this enum now wraps a ToolMaterial and keeps the old accessor names. */
public enum ESItemTiers {
	AETHERSENT(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1750, 12.0F, 1.0F, 22, ESConventionalTags.Items.INGOTS_AETHERSENT),
	THERMAL_SPRINGSTONE(BlockTags.INCORRECT_FOR_IRON_TOOL, 400, 7.0F, 2.0F, 10, ESConventionalTags.Items.INGOTS_THERMAL_SPRINGSTONE),
	GLACITE(BlockTags.INCORRECT_FOR_IRON_TOOL, 500, 7.0F, 2.5F, 10, ESConventionalTags.Items.GEMS_GLACITE),
	STARLIT_DIAMOND(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 2500, 9.0F, 4.0F, 20, ESConventionalTags.Items.GEMS_STARLIT_DIAMOND),
	DEEPSILVER(BlockTags.INCORRECT_FOR_IRON_TOOL, 800, 12.0F, 2.0F, 10, ESConventionalTags.Items.INGOTS_DEEPSILVER),
	UNREALIUM(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1200, 15.0F, 4.0F, 22, ESConventionalTags.Items.INGOTS_UNREALIUM),
	MALARITE(BlockTags.INCORRECT_FOR_IRON_TOOL, 400, 6.0F, 2.0F, 10, ESConventionalTags.Items.GEMS_MALARITE),
	PUNGENCY_FRUIT(BlockTags.INCORRECT_FOR_IRON_TOOL, 600, 8.0F, 2.5F, 12, ESTags.Items.REPAIR_TIER_PUNGENCY_FRUIT),
	STARFIRE(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1000, 8.5F, 2.5F, 15, ESTags.Items.REPAIR_TIER_STARFIRE),
	FLOWGLAZE(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1000, 8.5F, 2.5F, 15, ESTags.Items.REPAIR_TIER_FLOWGLAZE),
	AMARAMBER(BlockTags.INCORRECT_FOR_IRON_TOOL, 250, 6.0F, 2.0F, 14, ESConventionalTags.Items.INGOTS_AMARAMBER),
	AURORA_DEER_ANTLER(BlockTags.INCORRECT_FOR_IRON_TOOL, 400, 6.0F, 1.0F, 22, ESTags.Items.REPAIR_TIER_NONE),
	TENTACLE_SPIKE(BlockTags.INCORRECT_FOR_IRON_TOOL, 200, 6.0F, 1.5F, 14, ESTags.Items.REPAIR_TIER_NONE),
	TOOTH_OF_HUNGER(BlockTags.INCORRECT_FOR_IRON_TOOL, 250, 6.0F, 2.5F, 14, ESTags.Items.REPAIR_TIER_TOOTH_OF_HUNGER),
	GLISTERING(BlockTags.INCORRECT_FOR_IRON_TOOL, 8000, 6.0F, 2.0F, 14, ESConventionalTags.Items.INGOTS_DEEPSILVER),
	GOLEM_STEEL(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1000, 7.5F, 3.0F, 22, ESConventionalTags.Items.INGOTS_GOLEM_STEEL),
	PETAL(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1500, 7.5F, 3.5F, 22, ESTags.Items.REPAIR_TIER_PETAL),
	DOOMEDEN(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 2000, 7.5F, 2.5F, 10, ESTags.Items.REPAIR_TIER_DOOMEDEN);

	private final ToolMaterial material;

	ESItemTiers(TagKey<Block> incorrect, int uses, float speed, float damage, int enchantmentValue, TagKey<Item> repairItems) {
		this.material = new ToolMaterial(incorrect, uses, speed, damage, enchantmentValue, repairItems);
	}

	public ToolMaterial material() {
		return material;
	}

	public int getUses() {
		return material.durability();
	}

	public float getSpeed() {
		return material.speed();
	}

	public float getAttackDamageBonus() {
		return material.attackDamageBonus();
	}

	

	public TagKey<Item> getRepairItems() {
		return material.repairItems();
	}

	/** Durability, repairability and enchantability (what TieredItem used to do). */
	public Item.Properties common(Item.Properties properties) {
		return properties.durability(material.durability()).repairable(material.repairItems()).enchantable(material.enchantmentValue());
	}

	/** What SwordItem used to add: the tool component (cobweb etc.) and weapon component. Attributes are left to the caller. */
	public Item.Properties sword(Item.Properties properties) {
		HolderGetter<Block> lookup = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
		return common(properties)
			.component(DataComponents.TOOL, new Tool(List.of(
				Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F),
				Tool.Rule.overrideSpeed(lookup.getOrThrow(BlockTags.SWORD_INSTANTLY_MINES), Float.MAX_VALUE),
				Tool.Rule.overrideSpeed(lookup.getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)
			), 1.0F, 2, false))
			.component(DataComponents.WEAPON, new Weapon(1));
	}

	public Tool createToolProperties(TagKey<Block> minesEfficiently) {
		HolderGetter<Block> lookup = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
		return new Tool(List.of(
			Tool.Rule.deniesDrops(lookup.getOrThrow(material.incorrectBlocksForDrops())),
			Tool.Rule.minesAndDrops(lookup.getOrThrow(minesEfficiently), material.speed())
		), 1.0F, 1, true);
	}
}
