package net.diaithir.fauna;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fossils: veins of fossil rock in the strata (one block per rock type), dug into a fragment that names the animal at the moment it is
 * broken, cleaned on the preparation bench with a brush, then studied. What a block yields follows the real world: its depth gives the
 * age (deeper is older), its position gives the region on Earth, its rock type gives the formation family.
 */
public final class Fossils {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FaunaMod.ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FaunaMod.ID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FaunaMod.ID);

    public static final Map<String, DeferredBlock<FossilBlock>> ROCK = new LinkedHashMap<>();
    public static final DeferredItem<Item> FRAGMENT = ITEMS.registerItem("fossil_fragment", p -> new FossilItem(p, false));
    public static final DeferredItem<Item> CLEANED = ITEMS.registerItem("cleaned_fossil", p -> new FossilItem(p, true));
    public static final DeferredBlock<PrepBenchBlock> BENCH = BLOCKS.registerBlock("preparation_bench", PrepBenchBlock::new,
            p -> p.mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD));
    /** Species ids a player has studied. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<List<String>>> STUDIED = ATTACHMENTS.register("studied",
            () -> AttachmentType.<List<String>>builder(() -> new ArrayList<String>()).serialize(Codec.STRING.listOf().fieldOf("ids")).copyOnDeath().build());

    static {
        for (String rock : Species.ROCKS) {
            ROCK.put(rock, BLOCKS.registerBlock("fossil_" + rock, p -> new FossilBlock(p, rock),
                    p -> p.mapColor(MapColor.STONE).strength(2.5F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.STONE)));
            ITEMS.registerSimpleBlockItem(ROCK.get(rock));
        }
        ITEMS.registerSimpleBlockItem(BENCH);
    }

    private Fossils() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ATTACHMENTS.register(bus);
    }

    /** Age in million years of the strata at a block height; sea level is the present, 16 blocks down is about 1.5 million years. */
    public static double maAt(int y) {
        return 0.8 * Math.pow(Math.max(0, 63 - y) / 16.0, 1.35);
    }

    /** The fossil taxon a block of this rock yields here, relaxing rock, then age, then place until something fits. */
    public static Species pick(ServerLevel level, BlockPos pos, String rock, RandomSource random) {
        double ma = maAt(pos.getY());
        double tol = Math.max(2, ma * 0.15);
        double[] ll = EarthGeo.latLon(level, pos);
        List<Species> best = new ArrayList<>();
        for (int stage = 0; stage < 4 && best.isEmpty(); stage++) {
            for (Species s : Species.ALL) {
                if (!s.fossil) continue;
                if (stage < 1 && !s.rock.equals(rock)) continue;
                if (stage < 3 && !(ma >= s.maTo - tol * (stage + 1) && ma <= s.maFrom + tol * (stage + 1))) continue;
                if (stage < 2 && ll != null && !s.inRange(ll[0], ll[1])) continue;
                best.add(s);
            }
        }
        return best.get(random.nextInt(best.size()));
    }

    public static ItemStack stack(Item item, Species s) {
        ItemStack st = new ItemStack(item);
        CompoundTag t = new CompoundTag();
        t.putString("Species", s.id);
        st.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
        return st;
    }

    public static Species speciesOf(ItemStack st) {
        CustomData d = st.get(DataComponents.CUSTOM_DATA);
        return d == null ? null : Species.byId(d.copyTag().getStringOr("Species", ""));
    }
}
