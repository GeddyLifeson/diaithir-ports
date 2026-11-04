package net.diaithir.worldsys.itemmagnets;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Magnet items that pull nearby dropped items toward the carrying player. */
public final class ItemMagnetsFeature {
    public static final String ID = "diaithirworldsys";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

    public static final DeferredItem<Item> IRON_MAGNET = ITEMS.registerItem("iron_magnet", Item::new, p -> p.stacksTo(1));
    public static final DeferredItem<Item> GOLD_MAGNET = ITEMS.registerItem("gold_magnet", Item::new, p -> p.stacksTo(1));

    private ItemMagnetsFeature() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                e.accept(IRON_MAGNET.get());
                e.accept(GOLD_MAGNET.get());
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, e -> {
            if (e.getEntity() instanceof ServerPlayer player) pull(player);
        });
    }

    private static void pull(ServerPlayer player) {
        if (player.isShiftKeyDown() || player.isSpectator()) return;
        double radius = 0;
        double speed = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(GOLD_MAGNET.get())) {
                radius = 12;
                speed = 0.2;
                break;
            }
            if (s.is(IRON_MAGNET.get())) {
                radius = 6;
                speed = 0.12;
            }
        }
        if (radius == 0) return;
        Vec3 target = player.position().add(0, 0.3, 0);
        for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(radius))) {
            if (item.hasPickUpDelay() || !item.isAlive()) continue;
            Vec3 delta = target.subtract(item.position());
            double dist = delta.length();
            if (dist < 0.6 || dist > radius) continue;
            item.setDeltaMovement(item.getDeltaMovement().scale(0.5).add(delta.scale(speed / dist)));
            item.hurtMarked = true;
        }
    }
}
