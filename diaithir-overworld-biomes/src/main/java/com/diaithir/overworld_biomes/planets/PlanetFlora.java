package com.diaithir.overworld_biomes.planets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Each planet's own named tree (log + leaves), plant and ore, read from /data/diaithir_planets/planet_flora.json
 * (written by planets-data/gen_flora.py from the planet's real Library entries). Models, textures, loot, tags and
 * the worldgen features that place them are plain data in the jar.
 */
public final class PlanetFlora {
    private PlanetFlora() {}

    public static void init(IEventBus bus) {
        try (InputStream in = PlanetFlora.class.getResourceAsStream("/data/diaithir_planets/planet_flora.json")) {
            if (in == null) return;
            JsonArray arr = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonArray();
            DeferredRegister.Blocks blocks = DeferredRegister.createBlocks("diaithir_planets");
            DeferredRegister.Items items = DeferredRegister.createItems("diaithir_planets");
            for (JsonElement e : arr) {
                String id = e.getAsJsonObject().get("id").getAsString();
                String kind = e.getAsJsonObject().get("kind").getAsString();
                DeferredBlock<Block> b = switch (kind) {
                    case "log" -> blocks.registerBlock(id, p -> new RotatedPillarBlock(p.mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD)), () -> BlockBehaviour.Properties.of());
                    case "leaves" -> blocks.registerBlock(id, p -> new net.minecraft.world.level.block.TintedParticleLeavesBlock(0.01F, p.mapColor(MapColor.PLANT).strength(0.2F).sound(SoundType.GRASS).noOcclusion()), () -> BlockBehaviour.Properties.of());
                    case "plant" -> blocks.registerBlock(id, p -> new net.minecraft.world.level.block.BushBlock(p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)), () -> BlockBehaviour.Properties.of());
                    default -> blocks.registerBlock(id, p -> new Block(p.mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.0F, 3.0F).sound(SoundType.STONE)), () -> BlockBehaviour.Properties.of());
                };
                items.registerSimpleBlockItem(id, b);
            }
            blocks.register(bus);
            items.register(bus);
        } catch (Exception ex) {
            throw new IllegalStateException("planet_flora.json failed to load", ex);
        }
    }
}
