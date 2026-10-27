package com.github.talrey.createdeco;

import java.util.List;
import java.util.Map;


import com.github.talrey.createdeco.blocks.*;
import com.github.talrey.createdeco.items.*;
import com.zurrtum.create.content.decoration.MetalLadderBlock;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;

/** All blocks and items. Ids here must match tools/gen_deco.py, which writes the matching JSON. */
public class Content {
  public static final List<String> METALS = List.of("andesite", "zinc", "copper", "brass", "iron", "industrial_iron");
  public static final List<String> COIN_METALS = List.of("zinc", "copper", "brass", "iron", "industrial_iron", "gold", "netherite");
  public static final List<String> BRICK_COLORS = List.of("blue", "dean", "dusk", "pearl", "scarlet", "verdant", "umber", "red");
  public static final List<String> BRICK_TYPES = List.of("", "short", "tiled", "long", "corner", "cracked", "mossy");
  public static final List<String> DECALS = List.of("warning", "creeper", "skull", "flow", "ice", "radioactive",
      "top_left", "up", "top_right", "left", "cross", "right", "down_left", "down", "down_right",
      "fluid", "fire", "electrical", "fire_diamond", "no_entry");
  public static final Map<String, DyeColor> LAMP_COLORS = Map.of("yellow", DyeColor.YELLOW, "red", DyeColor.RED, "green", DyeColor.GREEN, "blue", DyeColor.BLUE);

  private static Block first;
  private static Block firstBrick;

  static Properties metal(String m) {
    MapColor c = switch (m) {
      case "zinc" -> MapColor.COLOR_LIGHT_GRAY;
      case "copper" -> MapColor.COLOR_ORANGE;
      case "brass" -> MapColor.GOLD;
      case "iron" -> MapColor.METAL;
      case "industrial_iron" -> MapColor.COLOR_GRAY;
      default -> MapColor.STONE;
    };
    return Properties.of().mapColor(c).strength(5, 6).requiresCorrectToolForDrops().noOcclusion().sound(SoundType.NETHERITE_BLOCK);
  }

  static String cap(String s) {
    StringBuilder b = new StringBuilder();
    for (String w : s.split("_")) b.append(w.isEmpty() ? "" : Character.toUpperCase(w.charAt(0)) + w.substring(1)).append(' ');
    return b.toString().trim();
  }

  public static void register() {
    CreateDecoMod.LOGGER.info("Registering Create Deco content");
    // items first (sheets, nuggets, ingot)
    for (String s : List.of("andesite_sheet", "zinc_sheet", "industrial_iron_sheet", "industrial_iron_nugget", "industrial_iron_ingot"))
      Reg.item(s, Item::new, new Item.Properties());
    for (String m : COIN_METALS) {
      Item coin = Reg.item(m + "_coin", Item::new, new Item.Properties());
      ItemRegistry.COINS.put(cap(m), coin);
    }
    for (String m : METALS) {
      String M = cap(m);
      if (!m.equals("iron")) Reg.block(m + "_bars", IronBarsBlock::new, metal(m));
      Reg.block(m + "_bars_overlay", IronBarsBlock::new, metal(m));
      Reg.block(m + "_mesh_fence", MeshFenceBlock::new, metal(m).sound(SoundType.CHAIN));
      Reg.block(m + "_catwalk", CatwalkBlock::new, metal(m), (b, p) -> new CatwalkBlockItem(b, p), new Item.Properties());
      Reg.block(m + "_catwalk_stairs", p -> new CatwalkStairBlock(p, M), metal(m), (b, p) -> new CatwalkStairBlockItem(b, p), new Item.Properties());
      Block rail = Reg.block(m + "_catwalk_railing", CatwalkRailingBlock::new, metal(m), (b, p) -> new RailingBlockItem(b, p), new Item.Properties());
      BlockRegistry.CATWALK_RAILINGS.put(M, rail);
      Reg.block(m + "_support", SupportBlock::new, metal(m));
      Reg.block(m + "_support_wedge", SupportWedgeBlock::new, metal(m));
      Reg.block(m + "_hull", HullBlock::new, metal(m));
      Reg.block(m + "_sheet_metal", RotatedPillarBlock::new, metal(m).noOcclusion().isViewBlocking((s, g, p) -> true));
      if (m.equals("iron") || m.equals("zinc") || m.equals("industrial_iron"))
        Reg.block(m + "_ladder", MetalLadderBlock::new, Properties.ofFullCopy(Blocks.LADDER).sound(SoundType.COPPER));
      if (!m.equals("iron")) {
        if (!m.equals("copper")) {
          Reg.block(m + "_door", p -> new DoorBlock(BlockSetType.COPPER, p), metal(m).strength(5, 5));
          Reg.block(m + "_trapdoor", p -> new TrapDoorBlock(BlockSetType.COPPER, p), metal(m).strength(5, 5));
        }
        Reg.block("locked_" + m + "_door", p -> new DoorBlock(BlockSetType.IRON, p), metal(m).strength(5, 5));
      }
      Reg.block(m + "_window", p -> new TransparentBlock(p), Properties.ofFullCopy(Blocks.GLASS));
      Reg.block(m + "_window_pane", IronBarsBlock::new, Properties.ofFullCopy(Blocks.GLASS_PANE));
      for (var e : LAMP_COLORS.entrySet()) {
        DyeColor dc = e.getValue();
        Reg.block(e.getKey() + "_" + m + "_lamp", p -> new CageLampBlock(p, dc.getTextureDiffuseColor()),
            Properties.of().noOcclusion().strength(0.5f).sound(SoundType.LANTERN).lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? 15 : 0));
      }
    }
    for (String m : COIN_METALS) {
      String M = cap(m);
      Item stack = Reg.item(m + "_coinstack", p -> new CoinStackItem(p, M), new Item.Properties());
      ItemRegistry.COINSTACKS.put(M, stack);
      Block b = Reg.blockNoItem(m + "_coinstack_block", p -> new CoinStackBlock(p, M), Properties.of().noOcclusion().strength(0.5f).sound(SoundType.CHAIN));
      BlockRegistry.COIN_BLOCKS.put(M, b);
    }
    for (String d : DECALS)
      Reg.block("decal_" + d, DecalBlock::new, Properties.of().strength(2, 2).sound(SoundType.LANTERN).noOcclusion());
    Reg.bricksTab = true;
    for (String c : BRICK_COLORS) for (String t : BRICK_TYPES) {
      if (c.equals("red") && t.isEmpty()) continue;
      String n = (t.isEmpty() ? "" : t + "_") + c + "_bricks";
      Properties bp = Properties.of().mapColor(MapColor.STONE).strength(2, 6).requiresCorrectToolForDrops().sound(SoundType.STONE);
      Block base = Reg.block(n, Block::new, bp);
      if (firstBrick == null) firstBrick = base;
      Reg.block(n + "_stairs", p -> new StairBlock(base.defaultBlockState(), p), bp);
      Reg.block(n + "_slab", SlabBlock::new, bp);
      Reg.block(n + "_wall", WallBlock::new, bp);
    }
    Reg.bricksTab = false;
    first = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(Reg.id("brass_catwalk"));
  }

  public static ItemStack tabIcon() { return new ItemStack(first); }
  public static ItemStack brickIcon() { return new ItemStack(firstBrick); }
}
