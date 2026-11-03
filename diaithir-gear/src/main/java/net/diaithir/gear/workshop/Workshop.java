package net.diaithir.gear.workshop;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.diaithir.gear.DiaithirGear;
import net.diaithir.gear.GearConfig;
import net.diaithir.gear.Sets;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Parts stations (clean-room, built from the idea of in-world part benches only). Three benches: smithy (melee, tools),
 * workshop (bows, wood and leather work), gunsmith (guns). Click parts onto the bench, take the finished item empty-handed.
 * Materials set the stats (see Craft), the Age of the blueprint and of every material gates who can build it, use hones the
 * item, improvements are added with a spare item in the off hand.
 */
public final class Workshop {
    public static final String SMITHY = "smithy", GUNSMITH = "gunsmith", WORKSHOP = "workshop";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DiaithirGear.ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DiaithirGear.ID);
    private static final DeferredRegister<BlockEntityType<?>> BES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, DiaithirGear.ID);
    private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, DiaithirGear.ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DiaithirGear.ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Assembly>> ASSEMBLY =
            COMPONENTS.registerComponentType("assembly", b -> b.persistent(Assembly.CODEC).networkSynchronized(Assembly.STREAM));
    /** Schematic item component: the blueprint id it teaches. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SCHEMATIC =
            COMPONENTS.registerComponentType("schematic", b -> b.persistent(Codec.STRING).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8));
    /** Blueprint ids a player has learned (needed only for blueprints flagged "schematic": true). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<List<String>>> KNOWN =
            ATTACHMENTS.register("known_blueprints", () -> AttachmentType.<List<String>>builder(() -> new ArrayList<>())
                    .serialize(Codec.STRING.listOf().fieldOf("ids")).copyOnDeath().build());

    private static BlockBehaviour.Properties bench() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.0F, 6.0F).sound(SoundType.ANVIL).requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<StationBlock> SMITHY_BLOCK = BLOCKS.registerBlock("smithy", p -> new StationBlock(p, SMITHY), Workshop::bench);
    public static final DeferredBlock<StationBlock> GUNSMITH_BLOCK = BLOCKS.registerBlock("gunsmith_bench", p -> new StationBlock(p, GUNSMITH), Workshop::bench);
    public static final DeferredBlock<StationBlock> WORKSHOP_BLOCK = BLOCKS.registerBlock("workshop_bench", p -> new StationBlock(p, WORKSHOP), Workshop::bench);
    public static final DeferredItem<BlockItem> SMITHY_ITEM = ITEMS.registerSimpleBlockItem(SMITHY_BLOCK);
    public static final DeferredItem<BlockItem> GUNSMITH_ITEM = ITEMS.registerSimpleBlockItem(GUNSMITH_BLOCK);
    public static final DeferredItem<BlockItem> WORKSHOP_ITEM = ITEMS.registerSimpleBlockItem(WORKSHOP_BLOCK);
    public static final DeferredItem<Item> SCHEMATIC_ITEM = ITEMS.registerItem("schematic", Item::new, () -> new Item.Properties().stacksTo(1));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StationEntity>> STATION_BE = BES.register("station",
            () -> new BlockEntityType<>(StationEntity::new, SMITHY_BLOCK.get(), GUNSMITH_BLOCK.get(), WORKSHOP_BLOCK.get()));

    private Workshop() {}

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BES.register(bus);
        COMPONENTS.register(bus);
        ATTACHMENTS.register(bus);
        bus.addListener(Workshop::tabs);
        NeoForge.EVENT_BUS.register(new Workshop());
        Catalog.load();
    }

    private static void tabs(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey().equals(net.minecraft.world.item.CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            e.accept(SMITHY_ITEM.get());
            e.accept(GUNSMITH_ITEM.get());
            e.accept(WORKSHOP_ITEM.get());
        }
    }

    // ---- rules -----------------------------------------------------------------------------------------------------------------

    public static int ageOf(Player p) { return GearConfig.IGNORE_AGE.getAsBoolean() ? 9 : Sets.ageOf(p); }

    public static boolean knows(Player p, Craft.Blueprint b) { return !b.schematic() || p.getData(KNOWN.get()).contains(b.id()); }

    public static void learn(Player p, String blueprint) {
        List<String> k = new ArrayList<>(p.getData(KNOWN.get()));
        if (!k.contains(blueprint)) { k.add(blueprint); p.setData(KNOWN.get(), k); }
    }

    public static ItemStack schematic(String blueprint) {
        ItemStack s = new ItemStack(SCHEMATIC_ITEM.get());
        s.set(SCHEMATIC.get(), blueprint);
        return s;
    }

    // ---- building an item ------------------------------------------------------------------------------------------------------

    static Map<String, Craft.Material> materials(Assembly a) {
        Map<String, Craft.Material> m = new LinkedHashMap<>();
        a.parts().forEach((slot, id) -> { Craft.Material x = Catalog.materials().get(id); if (x != null) m.put(slot, x); });
        return m;
    }

    public static Craft.Stats stats(Craft.Blueprint bp, Assembly a) {
        double dmg = 0;
        int dur = 0;
        for (String id : a.improvements()) {
            Catalog.Improvement im = Catalog.improvements().get(id);
            if (im != null) { dmg += im.damage(); dur += im.durability(); }
        }
        Craft.Stats s = Craft.assemble(bp, materials(a), a.hone(), dmg);
        return new Craft.Stats(s.damage(), s.speed(), s.durability() + dur, s.massKg(), s.spreadMul());
    }

    /** A new finished item for the blueprint and parts. Null when the blueprint's base item is not installed. */
    public static ItemStack build(Craft.Blueprint bp, Map<String, String> parts) {
        Item base = Catalog.baseItem(bp);
        if (base == null) return ItemStack.EMPTY;
        ItemStack st = new ItemStack(base);
        Assembly a = new Assembly(bp.id(), new LinkedHashMap<>(parts), 0, List.of());
        st.set(ASSEMBLY.get(), a);
        apply(st, bp, a);
        return st;
    }

    /** Rewrites the stat components of a stack from its assembly (used on build, honing and improvements). */
    public static void refresh(ItemStack st) {
        Assembly a = st.get(ASSEMBLY.get());
        if (a == null) return;
        Craft.Blueprint bp = Catalog.blueprints().get(a.blueprint());
        if (bp != null) apply(st, bp, a);
    }

    private static void apply(ItemStack st, Craft.Blueprint bp, Assembly a) {
        Craft.Stats s = stats(bp, a);
        Map<String, Craft.Material> mats = materials(a);
        boolean melee = bp.kind().equals("melee") || bp.kind().equals("tool");
        if (melee) {
            st.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, s.damage() - 1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, s.speed(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .build());
        }
        if (!bp.kind().equals("gun")) {
            int old = st.getDamageValue();
            st.set(DataComponents.MAX_DAMAGE, s.durability());
            st.setDamageValue(Math.min(old, s.durability() - 1));
        } else {
            CustomData.update(DataComponents.CUSTOM_DATA, st, tag -> {
                var g = new net.minecraft.nbt.CompoundTag();
                g.putFloat("damage_mul", (float) s.damage());
                g.putFloat("spread_mul", (float) s.spreadMul());
                g.putFloat("mass_kg", (float) s.massKg());
                tag.put("diaithirgear", g);
            });
        }
        List<Integer> colors = new ArrayList<>();
        for (Craft.Slot sl : bp.slots()) colors.add(mats.containsKey(sl.name()) ? mats.get(sl.name()).color() : 0xFFFFFF);
        st.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(), colors));
        if (!bp.kind().equals("ranged")) st.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(DiaithirGear.ID, "workshop/" + bp.id()));
        Craft.Slot main = bp.slots().get(0);
        st.set(DataComponents.ITEM_NAME, Component.translatable("workshop.diaithirgear.name",
                Component.translatable("material.diaithirgear." + a.parts().getOrDefault(main.name(), "wood")),
                Component.translatable("blueprint.diaithirgear." + bp.id())));
    }

    // ---- honing: every hit or block broken counts as one use ---------------------------------------------------------------------

    public static void addUse(Player p, ItemStack st, int n) {
        Assembly a = st.get(ASSEMBLY.get());
        if (a == null) return;
        int before = a.hone();
        Assembly b = a.withUses(a.uses() + n);
        st.set(ASSEMBLY.get(), b);
        if (b.hone() != before) {
            refresh(st);
            p.sendOverlayMessage(Component.translatable("workshop.diaithirgear.honed", st.getHoverName(), b.hone()));
        }
    }

    @SubscribeEvent
    public void hit(LivingDamageEvent.Post e) {
        if (e.getSource().getEntity() instanceof Player p && !p.level().isClientSide() && e.getSource().getDirectEntity() == p) addUse(p, p.getMainHandItem(), 1);
    }

    @SubscribeEvent
    public void broke(BreakBlockEvent e) {
        if (!e.getPlayer().level().isClientSide()) addUse(e.getPlayer(), e.getPlayer().getMainHandItem(), 1);
    }

    @SubscribeEvent
    public void tooltip(ItemTooltipEvent e) {
        ItemStack st = e.getItemStack();
        String sch = st.get(SCHEMATIC.get());
        if (sch != null) e.getToolTip().add(Component.translatable("blueprint.diaithirgear." + sch).withStyle(net.minecraft.ChatFormatting.GRAY));
        Assembly a = st.get(ASSEMBLY.get());
        if (a == null) return;
        Craft.Blueprint bp = Catalog.blueprints().get(a.blueprint());
        if (bp == null) return;
        Craft.Stats s = stats(bp, a);
        var g = net.minecraft.ChatFormatting.GRAY;
        a.parts().forEach((slot, mat) -> e.getToolTip().add(Component.translatable("workshop.diaithirgear.part", Component.translatable("slot.diaithirgear." + slot), Component.translatable("material.diaithirgear." + mat)).withStyle(g)));
        e.getToolTip().add(Component.translatable("workshop.diaithirgear.mass", s.massKg()).withStyle(g));
        e.getToolTip().add(Component.translatable("workshop.diaithirgear.hone", a.hone(), Craft.HONE_USES.length, a.uses()).withStyle(g));
        for (String id : a.improvements()) e.getToolTip().add(Component.literal("+ " + Catalog.improvements().getOrDefault(id, new Catalog.Improvement(id, "", 0, 0, id)).name()).withStyle(net.minecraft.ChatFormatting.DARK_AQUA));
    }

    /** Names like "slot.diaithirgear.head" are filled from lang; unknown ones fall back to the key. */
    static Set<String> roles() { return Set.of("head", "handle", "guard", "binding", "limb", "bowstring", "grip", "lock", "barrel", "stock", "pot"); }

    static Optional<Catalog.Improvement> improvementFor(ItemStack st) {
        for (Catalog.Improvement im : Catalog.improvements().values())
            if (net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(st.getItem()).toString().equals(im.item())) return Optional.of(im);
        return Optional.empty();
    }
}
