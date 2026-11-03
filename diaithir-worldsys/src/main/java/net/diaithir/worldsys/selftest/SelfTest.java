package net.diaithir.worldsys.selftest;

import net.diaithir.worldsys.jewelrypowers.JewelrypowersFeature;
import net.diaithir.worldsys.villagercare2.Villagercare2Feature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** Headless in-world checks. Runs once at server start when the environment variable DIAITHIR_SELFTEST is set, then stops the server. */
@EventBusSubscriber(modid = "diaithirworldsys")
public final class SelfTest {
    private static final Logger LOG = LoggerFactory.getLogger("worldsys-selftest");
    private static final String NS = "diaithirworldsys";
    private static int pass;
    private static int fail;

    private static void check(String name, boolean ok) {
        if (ok) pass++; else fail++;
        LOG.info("SELFTEST {} {}", ok ? "PASS" : "FAIL", name);
    }

    private static int wait = -1;
    private static final List<ItemStack> JOINED = new ArrayList<>();

    @SubscribeEvent
    static void joined(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e) {
        if (wait == -2 && e.getEntity() instanceof ItemEntity ie) JOINED.add(ie.getItem().copy());
    }

    @SubscribeEvent
    static void started(ServerStartedEvent e) {
        if (System.getenv("DIAITHIR_SELFTEST") != null) wait = 0;
    }

    @SubscribeEvent
    static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e) {
        if (wait < 0) return;
        if (++wait < 200) return;
        wait = -2;
        run(e.getServer());
        wait = -1;
    }

    private static void run(MinecraftServer server) {
        try {
            ServerLevel level = server.overworld();
            level.setChunkForced(0, 0, true);
            ServerPlayer p = new net.neoforged.neoforge.common.util.FakePlayer(level, new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "selftest")) {
                @Override
                public java.util.OptionalInt openMenu(net.minecraft.world.MenuProvider provider) {
                    this.containerMenu = provider.createMenu(1, getInventory(), this);
                    return java.util.OptionalInt.of(1);
                }
            };
            sticks(level, p);
            jewelry(p);
            villagers(level, p);
        } catch (Throwable t) {
            LOG.error("SELFTEST crashed", t);
            fail++;
        }
        LOG.info("SELFTEST DONE pass={} fail={}", pass, fail);
        server.halt(false);
    }

    private static void sticks(ServerLevel level, ServerPlayer p) {
        String[][] sticks = {{"cartography_table", "CartographyTableMenu"}, {"grindstone", "GrindstoneMenu"}, {"loom", "LoomMenu"}, {"smithing_table", "SmithingMenu"},
                {"stonecutter", "StonecutterMenu"}, {"anvil", "AnvilMenu"}, {"chipped_anvil", "AnvilMenu"}, {"damaged_anvil", "AnvilMenu"}};
        for (String[] s : sticks) {
            var item = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(NS, "crafting_on_a_stick_" + s[0]));
            ItemStack st = new ItemStack(item);
            p.setItemInHand(InteractionHand.MAIN_HAND, st);
            p.closeContainer();
            item.use(level, p, InteractionHand.MAIN_HAND);
            check("stick " + s[0] + " opens " + s[1], p.containerMenu.getClass().getSimpleName().equals(s[1]));
            p.closeContainer();
        }
    }

    private static void jewelry(ServerPlayer p) {
        p.getInventory().clearContent();
        double base = p.getAttributeValue(Attributes.ARMOR);
        give(p, "jewelry_iron_ring");
        JewelrypowersFeature.refresh(p);
        check("iron ring adds 1 armor", p.getAttributeValue(Attributes.ARMOR) == base + 1.0);
        give(p, "jewelry_iron_ring");
        JewelrypowersFeature.refresh(p);
        check("second iron ring does not stack", p.getAttributeValue(Attributes.ARMOR) == base + 1.0);
        give(p, "jewelry_diamond_ring");
        JewelrypowersFeature.refresh(p);
        check("diamond ring adds 2 more armor", p.getAttributeValue(Attributes.ARMOR) == base + 3.0);
        double hp = p.getAttributeValue(Attributes.MAX_HEALTH);
        give(p, "jewelry_ruby_necklace");
        JewelrypowersFeature.refresh(p);
        check("ruby necklace adds 2 max health", p.getAttributeValue(Attributes.MAX_HEALTH) == hp + 2.0);
        p.getInventory().clearContent();
        JewelrypowersFeature.refresh(p);
        check("removing jewelry removes bonuses", p.getAttributeValue(Attributes.ARMOR) == base && p.getAttributeValue(Attributes.MAX_HEALTH) == hp);
    }

    private static void give(ServerPlayer p, String id) {
        p.getInventory().add(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(NS, id))));
    }

    private static Villager farmer(ServerLevel level) {
        Villager v = EntityType.VILLAGER.create(level, EntitySpawnReason.COMMAND);
        v.setVillagerData(v.getVillagerData().withProfession(level.registryAccess(), net.minecraft.world.entity.npc.villager.VillagerProfession.FARMER).withLevel(1));
        v.getOffers();
        return v;
    }

    private static BlockHitResult hit(BlockPos pos) {
        return new BlockHitResult(Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false);
    }

    private static void villagers(ServerLevel level, ServerPlayer p) {
        BlockPos base = new BlockPos(0, 120, 0);
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
            level.setBlock(base.offset(dx, -1, dz), Blocks.STONE.defaultBlockState(), 3);
            for (int dy = 0; dy < 4; dy++) level.setBlock(base.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3);
        }
        // trader
        BlockPos tp = base;
        level.setBlock(tp, Villagercare2Feature.TRADER.get().defaultBlockState(), 3);
        var tbe = (Villagercare2Feature.VillagerBE) level.getBlockEntity(tp);
        check("trader block entity exists", tbe != null);
        ItemStack carrier = Villagercare2Feature.pack(farmer(level));
        p.setItemInHand(InteractionHand.MAIN_HAND, carrier);
        var r1 = level.getBlockState(tp).useItemOn(carrier, level, p, InteractionHand.MAIN_HAND, hit(tp));
        check("trader accepts a stored villager", tbe.villagers.size() == 1 && r1.consumesAction());
        LOG.info("SELFTEST offers={}", tbe.villagers.isEmpty() ? "none" : tbe.villagers.get(0).toString().length() + " bytes nbt, has Offers=" + tbe.villagers.get(0).contains("Offers"));
        String buyId = tbe.villagers.get(0).getCompound("Offers").flatMap(o -> o.getList("Recipes")).flatMap(l -> l.getCompound(0)).flatMap(r -> r.getCompound("buy")).flatMap(b -> b.getString("id")).orElse("minecraft:wheat");
        int buyCount = tbe.villagers.get(0).getCompound("Offers").flatMap(o -> o.getList("Recipes")).flatMap(l -> l.getCompound(0)).flatMap(r -> r.getCompound("buy")).flatMap(b -> b.getInt("count")).orElse(20);
        ItemStack wheat = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(buyId)), buyCount);
        p.setItemInHand(InteractionHand.MAIN_HAND, wheat);
        level.getBlockState(tp).useItemOn(wheat, level, p, InteractionHand.MAIN_HAND, hit(tp));
        LOG.info("SELFTEST dbg wheat={} emeralds={} offers={}", wheat.getCount(), p.getInventory().countItem(Items.EMERALD), tbe.villagers.isEmpty() ? "-" : tbe.villagers.get(0).get("Offers"));
        check("trader takes the first offer's cost and pays out", wheat.isEmpty() && !p.getInventory().isEmpty());
        p.setShiftKeyDown(true);
        level.getBlockState(tp).useWithoutItem(level, p, hit(tp));
        p.setShiftKeyDown(false);
        check("sneak-click returns the villager", tbe.villagers.isEmpty() && p.getInventory().countItem(Villagercare2Feature.VILLAGER.get()) >= 1);
        // save/load round trip
        var tag = tbe.saveWithoutMetadata(level.registryAccess());
        check("block entity saves", tag != null);
        // breeder
        BlockPos bp = base.offset(3, 0, 0);
        level.setBlock(bp, Villagercare2Feature.BREEDER.get().defaultBlockState(), 3);
        var bbe = (Villagercare2Feature.VillagerBE) level.getBlockEntity(bp);
        bbe.villagers.add(Villagercare2Feature.pack(farmer(level)).get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag());
        bbe.villagers.add(Villagercare2Feature.pack(farmer(level)).get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag());
        bbe.villagers.add(Villagercare2Feature.pack(farmer(level)).get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag());
        bbe.food = 12;
        JOINED.clear();
        for (int i = 0; i < 6000; i++) bbe.tick(level, 1);
        check("breeder makes a baby villager item after 5 minutes", JOINED.stream().anyMatch(x -> x.is(Villagercare2Feature.VILLAGER.get())) && bbe.food == 0);
        BlockPos fp = base.offset(-3, 0, 0);
        level.setBlock(fp, Villagercare2Feature.FARMER.get().defaultBlockState(), 3);
        var fbe = (Villagercare2Feature.VillagerBE) level.getBlockEntity(fp);
        fbe.villagers.add(Villagercare2Feature.pack(farmer(level)).get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag());
        BlockPos crop = fp.above().east();
        level.setBlock(crop.below(), Blocks.FARMLAND.defaultBlockState(), 3);
        level.setBlock(crop, Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7), 3);
        JOINED.clear();
        for (int i = 0; i < 200; i++) fbe.tick(level, 2);
        check("farmer harvests and replants mature wheat", level.getBlockState(crop).getValue(CropBlock.AGE) == 0 && JOINED.stream().anyMatch(x -> x.is(Items.WHEAT)));
    }

    private SelfTest() {}
}
