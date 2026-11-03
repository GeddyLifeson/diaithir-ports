package net.diaithir.worldsys.gemrings;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.minecraft.core.Holder;

import java.util.List;

/** Gems dropped from ores and crafted into rings that grant effects while carried. */
public final class GemRingsFeature {
    public static final String ID = "diaithirworldsys";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

    public static final DeferredItem<Item> RUBY = ITEMS.registerSimpleItem("ruby");
    public static final DeferredItem<Item> SAPPHIRE = ITEMS.registerSimpleItem("sapphire");
    public static final DeferredItem<Item> TOPAZ = ITEMS.registerSimpleItem("topaz");
    public static final DeferredItem<Item> RUBY_RING = ITEMS.registerItem("ruby_ring", Item::new, p -> p.stacksTo(1));
    public static final DeferredItem<Item> SAPPHIRE_RING = ITEMS.registerItem("sapphire_ring", Item::new, p -> p.stacksTo(1));
    public static final DeferredItem<Item> TOPAZ_RING = ITEMS.registerItem("topaz_ring", Item::new, p -> p.stacksTo(1));

    private static final List<DeferredItem<Item>> GEMS = List.of(RUBY, SAPPHIRE, TOPAZ);
    private static final double GEM_CHANCE = 0.08;
    private static final int REFRESH_TICKS = 40;
    private static final int EFFECT_TICKS = 100;

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) GEMS.forEach(g -> e.accept(g.get()));
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                e.accept(RUBY_RING.get());
                e.accept(SAPPHIRE_RING.get());
                e.accept(TOPAZ_RING.get());
            }
        });
        NeoForge.EVENT_BUS.addListener(BreakBlockEvent.class, GemRingsFeature::onBreak);
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, GemRingsFeature::onPlayerTick);
    }

    private static void onBreak(BreakBlockEvent e) {
        if (!(e.getLevel() instanceof ServerLevel level) || e.getPlayer().isCreative()) return;
        var state = e.getState();
        if (!(state.is(BlockTags.COAL_ORES) || state.is(BlockTags.IRON_ORES) || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.EMERALD_ORES) || state.is(BlockTags.LAPIS_ORES) || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.COPPER_ORES))) return;
        if (level.getRandom().nextDouble() >= GEM_CHANCE) return;
        ItemStack gem = new ItemStack(GEMS.get(level.getRandom().nextInt(GEMS.size())).get());
        var pos = e.getPos();
        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, gem));
    }

    private static void onPlayerTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer player) || player.tickCount % REFRESH_TICKS != 0) return;
        boolean ruby = false;
        boolean sapphire = false;
        boolean topaz = false;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            if (s.is(RUBY_RING.get())) ruby = true;
            else if (s.is(SAPPHIRE_RING.get())) sapphire = true;
            else if (s.is(TOPAZ_RING.get())) topaz = true;
        }
        if (ruby) apply(player, MobEffects.STRENGTH, 0);
        if (sapphire) apply(player, MobEffects.WATER_BREATHING, 0);
        if (topaz) apply(player, MobEffects.HASTE, 0);
    }

    private static void apply(ServerPlayer player, Holder<MobEffect> effect, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, EFFECT_TICKS, amplifier, true, false, true));
    }
}
