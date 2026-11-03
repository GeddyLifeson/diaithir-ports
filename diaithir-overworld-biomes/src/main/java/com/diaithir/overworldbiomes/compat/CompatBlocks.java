package com.diaithir.overworldbiomes.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers replaced-mod block and item ids (biomesoplenty:fir_planks and so on) from the data file
 * compat_ids.json so recipes, loot tables and tags written against the originals keep resolving.
 * File format: {"<namespace>": [{"id": "fir_planks", "kind": "planks"}, ...]}
 */
public final class CompatBlocks {
    static final java.util.List<Object[]> BOATS = new java.util.ArrayList<>();
    static final java.util.List<Block[]> SIGNS = new java.util.ArrayList<>();
    static final java.util.List<DeferredBlock<Block>> SIGN_BLOCKS = new java.util.ArrayList<>();
    static final java.util.List<DeferredBlock<Block>> HANGING_BLOCKS = new java.util.ArrayList<>();
    static final java.util.Map<String, WoodType> WOODS = new java.util.HashMap<>();

    private CompatBlocks() {}

    public static void init(IEventBus bus) {
        bus.addListener((net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent e) -> {
            if (!SIGN_BLOCKS.isEmpty()) e.modify(net.minecraft.world.level.block.entity.BlockEntityType.SIGN, SIGN_BLOCKS.stream().map(DeferredBlock::get).toArray(Block[]::new));
            if (!HANGING_BLOCKS.isEmpty()) e.modify(net.minecraft.world.level.block.entity.BlockEntityType.HANGING_SIGN, HANGING_BLOCKS.stream().map(DeferredBlock::get).toArray(Block[]::new));
        });
        if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) CompatClient.init(bus);
        try (InputStream in = CompatBlocks.class.getResourceAsStream("/data/diaithir_overworld_biomes/compat_ids.json")) {
            if (in == null) return;
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            var self = net.neoforged.fml.ModList.get().getModContainerById("diaithir_overworld_biomes");
            for (var ns : root.entrySet()) {
                // When the original mod is installed next to this jar, it owns these ids: do not register them twice.
                var other = net.neoforged.fml.ModList.get().getModContainerById(ns.getKey());
                if (other.isPresent() && self.isPresent()
                        && other.get().getModInfo().getOwningFile() != self.get().getModInfo().getOwningFile()) {
                    continue;
                }
                DeferredRegister.Blocks blocks = DeferredRegister.createBlocks(ns.getKey());
                DeferredRegister.Items items = DeferredRegister.createItems(ns.getKey());
                DeferredRegister<net.minecraft.world.entity.EntityType<?>> entities = DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, ns.getKey());
                JsonArray arr = ns.getValue().getAsJsonArray();
                for (JsonElement e : arr) {
                    JsonObject o = e.getAsJsonObject();
                    register(ns.getKey(), blocks, items, entities, o.get("id").getAsString(), o.get("kind").getAsString());
                }
                blocks.register(bus);
                items.register(bus);
                entities.register(bus);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("compat_ids.json failed to load", ex);
        }
    }

    private static void register(String modNs, DeferredRegister.Blocks blocks, DeferredRegister.Items items, DeferredRegister<net.minecraft.world.entity.EntityType<?>> entities, String id, String kind) {
        switch (kind) {
            case "item" -> items.registerSimpleItem(id);
            case "boat", "chest_boat" -> {
                boolean chest = kind.equals("chest_boat");
                String w = id.substring(0, id.length() - (chest ? "_chest_boat".length() : "_boat".length()));
                var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, net.minecraft.resources.Identifier.fromNamespaceAndPath(modNs, id));
                var holder = new net.neoforged.neoforge.registries.DeferredItem[1];
                var typeHolder = entities.register(id, () -> (net.minecraft.world.entity.EntityType<?>)
                    net.minecraft.world.entity.EntityType.Builder.<net.minecraft.world.entity.vehicle.boat.AbstractBoat>of(
                        (t, l) -> chest
                            ? new net.minecraft.world.entity.vehicle.boat.ChestBoat((net.minecraft.world.entity.EntityType) t, l, () -> (net.minecraft.world.item.Item) holder[0].get())
                            : new net.minecraft.world.entity.vehicle.boat.Boat((net.minecraft.world.entity.EntityType) t, l, () -> (net.minecraft.world.item.Item) holder[0].get()),
                        net.minecraft.world.entity.MobCategory.MISC)
                    .noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10).build(key));
                holder[0] = items.registerItem(id, p -> new net.minecraft.world.item.BoatItem((net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.vehicle.boat.AbstractBoat>) typeHolder.get(), p.stacksTo(1)), () -> new net.minecraft.world.item.Item.Properties());
                BOATS.add(new Object[] { modNs, w, chest, typeHolder });
            }
            case "sign", "hanging_sign" -> {
                boolean hanging = kind.equals("hanging_sign");
                String w = id.substring(0, id.length() - (hanging ? "_hanging_sign".length() : "_sign".length()));
                WoodType wt = WOODS.computeIfAbsent(modNs + ":" + w, n -> WoodType.register(new WoodType(n, BlockSetType.OAK)));
                String wallId = hanging ? w + "_wall_hanging_sign" : w + "_wall_sign";
                DeferredBlock<Block> standing;
                DeferredBlock<Block> wall;
                if (hanging) {
                    standing = blocks.registerBlock(id, p -> new net.minecraft.world.level.block.CeilingHangingSignBlock(wt, p.mapColor(MapColor.WOOD).forceSolidOn().noCollision().strength(1.0F).sound(SoundType.WOOD)), () -> BlockBehaviour.Properties.of());
                    wall = blocks.registerBlock(wallId, p -> new net.minecraft.world.level.block.WallHangingSignBlock(wt, p.mapColor(MapColor.WOOD).forceSolidOn().noCollision().strength(1.0F).sound(SoundType.WOOD)), () -> BlockBehaviour.Properties.of());
                    items.registerItem(id, p -> new net.minecraft.world.item.HangingSignItem(standing.get(), wall.get(), p.stacksTo(16)), () -> new net.minecraft.world.item.Item.Properties());
                    SIGNS.add(new Block[0]); HANGING_BLOCKS.add(standing); HANGING_BLOCKS.add(wall);
                } else {
                    standing = blocks.registerBlock(id, p -> new net.minecraft.world.level.block.StandingSignBlock(wt, p.mapColor(MapColor.WOOD).forceSolidOn().noCollision().strength(1.0F).sound(SoundType.WOOD)), () -> BlockBehaviour.Properties.of());
                    wall = blocks.registerBlock(wallId, p -> new net.minecraft.world.level.block.WallSignBlock(wt, p.mapColor(MapColor.WOOD).forceSolidOn().noCollision().strength(1.0F).sound(SoundType.WOOD)), () -> BlockBehaviour.Properties.of());
                    items.registerItem(id, p -> new net.minecraft.world.item.SignItem(standing.get(), wall.get(), p.stacksTo(16)), () -> new net.minecraft.world.item.Item.Properties());
                    SIGN_BLOCKS.add(standing); SIGN_BLOCKS.add(wall);
                }
            }
            case "planks" -> block(blocks, items, id, p -> new Block(p.mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
            case "log" -> block(blocks, items, id, p -> new RotatedPillarBlock(p.mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));
            case "stairs" -> block(blocks, items, id, p -> new StairBlock(net.minecraft.world.level.block.Blocks.OAK_PLANKS.defaultBlockState(), p.mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
            case "slab" -> block(blocks, items, id, p -> new SlabBlock(p.mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
            case "fence" -> block(blocks, items, id, p -> new FenceBlock(p.mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
            case "gate" -> block(blocks, items, id, p -> new FenceGateBlock(WoodType.OAK, p.mapColor(MapColor.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
            case "wall" -> block(blocks, items, id, p -> new net.minecraft.world.level.block.WallBlock(p.mapColor(MapColor.STONE).strength(1.5F, 6.0F).sound(SoundType.STONE)));
            case "door" -> block(blocks, items, id, p -> new net.minecraft.world.level.block.DoorBlock(BlockSetType.OAK, p.mapColor(MapColor.WOOD).strength(3.0F).sound(SoundType.WOOD).noOcclusion()));
            case "trapdoor" -> block(blocks, items, id, p -> new net.minecraft.world.level.block.TrapDoorBlock(BlockSetType.OAK, p.mapColor(MapColor.WOOD).strength(3.0F).sound(SoundType.WOOD).noOcclusion()));
            case "button" -> block(blocks, items, id, p -> new net.minecraft.world.level.block.ButtonBlock(BlockSetType.OAK, 30, p.noCollision().strength(0.5F).sound(SoundType.WOOD)));
            case "plate" -> block(blocks, items, id, p -> new net.minecraft.world.level.block.PressurePlateBlock(BlockSetType.OAK, p.mapColor(MapColor.WOOD).noCollision().strength(0.5F).sound(SoundType.WOOD)));
            case "leaves" -> block(blocks, items, id, p -> new net.minecraft.world.level.block.TintedParticleLeavesBlock(0.01F, p.mapColor(MapColor.PLANT).strength(0.2F).sound(SoundType.GRASS).noOcclusion()));
            case "sapling" -> {
                String tree = modNs + "_" + id.substring(0, id.length() - "_sapling".length()) + "_tree";
                var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE,
                        net.minecraft.resources.Identifier.fromNamespaceAndPath("diaithir_overworld_biomes", tree));
                var grower = new net.minecraft.world.level.block.grower.TreeGrower(id, java.util.Optional.empty(), java.util.Optional.of(key), java.util.Optional.empty());
                block(blocks, items, id, p -> new net.minecraft.world.level.block.SaplingBlock(grower, p.mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));
            }
            case "tall" -> block(blocks, items, id, p -> new net.minecraft.world.level.block.DoublePlantBlock(p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));
            case "plant" -> block(blocks, items, id, p -> new net.minecraft.world.level.block.BushBlock(p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)));
            default -> block(blocks, items, id, p -> new Block(p.mapColor(MapColor.STONE).strength(1.5F, 6.0F).sound(SoundType.STONE)));
        }
    }

    private static void block(DeferredRegister.Blocks blocks, DeferredRegister.Items items, String id,
                              java.util.function.Function<BlockBehaviour.Properties, Block> f) {
        DeferredBlock<Block> b = blocks.registerBlock(id, f, () -> BlockBehaviour.Properties.of());
        items.registerSimpleBlockItem(id, b);
    }
}
