package net.diaithir.diaithirtrees;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(TreesMod.ID)
public final class TreesMod {
    public static final String ID = "diaithirtrees";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

    private static final List<List<DeferredBlock<Block>>> PAIRS = new ArrayList<>();

    static {
        for (String s : readSpecies()) {
            add(s + "_planks", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));
            DeferredBlock<Block> log = add(s + "_log", RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG));
            DeferredBlock<Block> strippedLog = add(s + "_stripped_log", RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG));
            DeferredBlock<Block> wood = add(s + "_wood", RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD));
            DeferredBlock<Block> strippedWood = add(s + "_stripped_wood", RotatedPillarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD));
            add(s + "_leaves", SimpleLeaves::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
            ResourceKey<ConfiguredFeature<?, ?>> key = ResourceKey.create(Registries.CONFIGURED_FEATURE,
                Identifier.fromNamespaceAndPath(ID, s + "_tree"));
            TreeGrower grower = new TreeGrower(s, Optional.empty(), Optional.of(key), Optional.empty());
            add(s + "_sapling", p -> new SaplingBlock(grower, p), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING));
            PAIRS.add(List.of(log, strippedLog));
            PAIRS.add(List.of(wood, strippedWood));
        }
    }

    private static List<String> readSpecies() {
        List<String> out = new ArrayList<>();
        try (InputStream in = TreesMod.class.getResourceAsStream("/data/diaithirtrees/species.json")) {
            if (in == null) {
                return out;
            }
            JsonArray arr = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonArray();
            for (JsonElement e : arr) {
                out.add(e.getAsString());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read species.json", e);
        }
        return out;
    }

    private static DeferredBlock<Block> add(String name, Function<BlockBehaviour.Properties, Block> make, BlockBehaviour.Properties props) {
        DeferredBlock<Block> b = BLOCKS.registerBlock(name, make, () -> props);
        ITEMS.registerSimpleBlockItem(b);
        return b;
    }

    @SubscribeEvent
    public void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.isSimulated()) {
            return;
        }
        if (!event.getItemAbility().equals(ItemAbilities.AXE_STRIP)) {
            return;
        }
        BlockState state = event.getState();
        for (List<DeferredBlock<Block>> pair : PAIRS) {
            if (state.is(pair.get(0).get())) {
                BlockState stripped = pair.get(1).get().defaultBlockState();
                if (stripped.hasProperty(RotatedPillarBlock.AXIS) && state.hasProperty(RotatedPillarBlock.AXIS)) {
                    stripped = stripped.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
                }
                event.setFinalState(stripped);
                return;
            }
        }
    }

    public TreesMod(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        NeoForge.EVENT_BUS.register(this);
    }
}
