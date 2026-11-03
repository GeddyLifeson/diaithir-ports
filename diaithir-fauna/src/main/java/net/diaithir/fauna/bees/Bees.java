package net.diaithir.fauna.bees;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Species bees. Vanilla bees carry a species (attachment). Wild bees pick a surface species when they first appear; two parents of a
 * crossing pair breed the crossed species (otherwise one parent's). A jar carries a bee, an apiary houses up to three and turns the
 * flowers around it into combs of their species. Combs are cooked or crushed into the resource.
 */
public final class Bees {
    public static final String ID = "diaithirfauna";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> SPECIES = ATTACHMENTS.register("bee_species",
            () -> AttachmentType.builder(() -> "").serialize(Codec.STRING.fieldOf("id")).sync(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8).build());
    public static final DeferredBlock<ApiaryBlock> APIARY = BLOCKS.registerBlock("apiary", ApiaryBlock::new,
            p -> p.mapColor(MapColor.WOOD).strength(1.5F).sound(SoundType.WOOD));
    public static final DeferredItem<net.minecraft.world.item.BlockItem> APIARY_ITEM = ITEMS.registerSimpleBlockItem(APIARY);
    public static final DeferredItem<Item> JAR = ITEMS.registerItem("bee_jar", BeeJarItem::new, p -> p.stacksTo(16));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ApiaryEntity>> APIARY_ENTITY =
            ENTITIES.register("apiary", () -> new BlockEntityType<>(ApiaryEntity::new, APIARY.get()));
    /** One comb per species except the meadow one, which is the vanilla honeycomb. */
    public static final Map<String, DeferredItem<Item>> COMBS = new LinkedHashMap<>();

    /** A wild nest per wild species. */
    public static final Map<String, DeferredBlock<NestBlock>> NESTS = new LinkedHashMap<>();

    static {
        for (BeeSpecies s : BeeSpecies.ALL) {
            if (s.wild() > 0) {
                NESTS.put(s.id(), BLOCKS.registerBlock("nest_" + s.id(), p -> new NestBlock(p, s.id()), p -> p.mapColor(MapColor.WOOD).strength(0.8F).sound(SoundType.WOOD)));
                ITEMS.registerSimpleBlockItem(NESTS.get(s.id()));
            }
            if (!s.id().equals(BeeSpecies.DEFAULT)) COMBS.put(s.id(), ITEMS.registerSimpleItem("comb_" + s.id()));
        }
    }

    private Bees() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        ATTACHMENTS.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) e.accept(APIARY_ITEM.get());
            else if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) {
                e.accept(JAR.get());
                COMBS.values().forEach(c -> e.accept(c.get()));
            }
        });
        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, Bees::onJoin);
        NeoForge.EVENT_BUS.addListener(BabyEntitySpawnEvent.class, Bees::onBaby);
    }

    /** The bee's genes ("species/productivity/endurance"), or "" before it has any. */
    public static String genesOf(Bee bee) {
        String g = bee.getData(SPECIES.get());
        return Genes.valid(g) ? g : "";
    }

    public static String speciesOf(Bee bee) { return Genes.species(genesOf(bee)); }


    /** The item a species' combs are: the vanilla honeycomb for the meadow bee. */
    public static Item combOf(String species) {
        return species.equals(BeeSpecies.DEFAULT) ? net.minecraft.world.item.Items.HONEYCOMB : COMBS.get(species).get();
    }

    static void onJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide() || !(e.getEntity() instanceof Bee bee) || !speciesOf(bee).isEmpty()) return;
        bee.setData(SPECIES.get(), Genes.of(pickWild(bee.getRandom()), 0, 0));
    }

    /** A weighted surface species. */
    static String pickWild(net.minecraft.util.RandomSource r) {
        int total = 0;
        for (BeeSpecies s : BeeSpecies.ALL) total += s.wild();
        int n = r.nextInt(total);
        for (BeeSpecies s : BeeSpecies.ALL) {
            if ((n -= s.wild()) < 0) return s.id();
        }
        return BeeSpecies.DEFAULT;
    }

    static void onBaby(BabyEntitySpawnEvent e) {
        if (e.getParentA() instanceof Bee a && e.getParentB() instanceof Bee b && e.getChild() instanceof Bee child) inherit(a, b, child);
    }

    /** A crossing pair gives the crossed species, otherwise the child takes a parent's; traits are mixed from both parents. */
    public static void inherit(Bee a, Bee b, Bee child) {
        String ga = genesOf(a), gb = genesOf(b), sa = Genes.species(ga), sb = Genes.species(gb);
        BeeSpecies cross = BeeSpecies.crossOf(sa, sb);
        String pick = cross != null ? cross.id() : (child.getRandom().nextBoolean() ? sa : sb);
        if (pick.isEmpty()) pick = BeeSpecies.DEFAULT;
        child.setData(SPECIES.get(), Genes.of(pick, Genes.mix(Genes.prod(ga), Genes.prod(gb), child.getRandom()), Genes.mix(Genes.end(ga), Genes.end(gb), child.getRandom())));
    }
}
