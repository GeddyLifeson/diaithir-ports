package com.diaithir.overworldbiomes.naturesspirit.registration.sets;

import com.diaithir.overworldbiomes.naturesspirit.blocks.LargeFlowerBlock;
import com.diaithir.overworldbiomes.naturesspirit.blocks.MidFlowerBlock;
import com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.List;

import static com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper.*;

public class FlowerSet {

  private final String name;
  private final Item dyeColor;
  private final Holder<MobEffect> statusEffect;
  private final Item itemBefore;
  private final FlowerPreset preset;
  private Block flowerBlock;
  private Block pottedFlowerBlock;
  private final List<Block> registeredBlocksList = new ArrayList<>();
  private final List<Item> registeredItemsList = new ArrayList<>();

  public FlowerSet(String name, Item dyeColor, Holder<MobEffect> statusEffect, Item itemBefore, FlowerPreset preset) {
    this.name = name;
    this.dyeColor = dyeColor;
    this.statusEffect = statusEffect;
    this.itemBefore = itemBefore;
    this.preset = preset;
    this.registerFlower();
  }

  public FlowerSet(String name, Item dyeColor, Item itemBefore, FlowerPreset preset) {
    this.name = name;
    this.dyeColor = dyeColor;
    this.statusEffect = null;
    this.itemBefore = itemBefore;
    this.preset = preset;
    this.registerFlower();
  }

  public FlowerSet(String name, Holder<MobEffect> statusEffect, Item itemBefore, FlowerPreset preset) {
    this.name = name;
    this.dyeColor = null;
    this.statusEffect = statusEffect;
    this.itemBefore = itemBefore;
    this.preset = preset;
    this.registerFlower();
  }

  public FlowerSet(String name, Item itemBefore, FlowerPreset preset) {
    this.name = name;
    this.dyeColor = null;
    this.statusEffect = null;
    this.itemBefore = itemBefore;
    this.preset = preset;
    this.registerFlower();
  }

  private void registerFlower() {
    if (isTall()) {
      flowerBlock = registerTallPlantBlock(NSRegistryHelper.name(name),
          new TallFlowerBlock(NSRegistryHelper.bp()
              .noCollision()
              .instabreak()
              .sound(SoundType.GRASS)
              .ignitedByLava()
              .offsetType(BlockBehaviour.OffsetType.XZ)
              .pushReaction(PushReaction.DESTROY)),
          itemBefore,
          0.4f
      );

    } else if (preset == FlowerPreset.BIG_SMALL) {
      flowerBlock = registerPlantBlock(NSRegistryHelper.name(name), new LargeFlowerBlock(
          statusEffect,
          7,
          NSRegistryHelper.bp()
              .mapColor(MapColor.COLOR_GREEN)
              .noCollision()
              .instabreak()
              .sound(SoundType.GRASS)
              .offsetType(BlockBehaviour.OffsetType.XZ)
              .pushReaction(PushReaction.DESTROY)
      ), itemBefore, 0.4f);
    } else if (preset == FlowerPreset.SMALL) {
      flowerBlock = registerPlantBlock(NSRegistryHelper.name(name), new FlowerBlock(
          statusEffect,
          7,
          NSRegistryHelper.bp()
              .mapColor(MapColor.COLOR_GREEN)
              .noCollision()
              .instabreak()
              .sound(SoundType.GRASS)
              .offsetType(BlockBehaviour.OffsetType.XZ)
              .pushReaction(PushReaction.DESTROY)
      ), itemBefore, 0.3f);
    } else if (preset == FlowerPreset.MID_SMALL) {
      flowerBlock = registerPlantBlock(NSRegistryHelper.name(name), new MidFlowerBlock(
          statusEffect,
          7,
          NSRegistryHelper.bp()
              .mapColor(MapColor.COLOR_GREEN)
              .noCollision()
              .instabreak()
              .sound(SoundType.GRASS)
              .offsetType(BlockBehaviour.OffsetType.XZ)
              .pushReaction(PushReaction.DESTROY)
      ), itemBefore, 0.3f);
    }
    if (hasFlowerPot()) {
      pottedFlowerBlock = registerTransparentBlockWithoutTab(NSRegistryHelper.name("potted_" + name),
          new FlowerPotBlock(flowerBlock, NSRegistryHelper.bp().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY)));
      registeredBlocksList.add(pottedFlowerBlock);
    }
    registeredBlocksList.add(flowerBlock);
    NSRegistryHelper.FlowerHashMap.put(name, this);
  }

  public int getDyeNumber() {
    switch (preset) {
      case TALL, BIG_SMALL -> {
        return 2;
      }
      case BIG_TALL -> {
        return 4;
      }
      default -> {
        return 1;
      }
    }
  }

  public List<Block> getRegisteredBlocksList() {
    return registeredBlocksList;
  }

  public List<Item> getRegisteredItemsList() {
    return registeredItemsList;
  }

  public FlowerPreset getPreset() {
    return preset;
  }

  public boolean hasFlowerPot() {
    return getPreset() == FlowerPreset.SMALL || getPreset() == FlowerPreset.BIG_SMALL || getPreset() == FlowerPreset.MID_SMALL;
  }

  public boolean isTall() {
    return getPreset() == FlowerPreset.TALL || getPreset() == FlowerPreset.BIG_TALL;
  }

  public Item getDyeColor() {
    return dyeColor;
  }

  public Block getFlowerBlock() {
    return flowerBlock;
  }

  public Block getPottedFlowerBlock() {
    return pottedFlowerBlock;
  }

  public String getName() {
    return name;
  }

  public enum FlowerPreset {
    SMALL, MID_SMALL,
    TALL,
    BIG_SMALL,
    BIG_TALL
  }
}
