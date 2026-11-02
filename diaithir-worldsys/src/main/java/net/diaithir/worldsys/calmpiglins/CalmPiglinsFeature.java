package net.diaithir.worldsys.calmpiglins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Keeps piglins calm toward players who are actively bartering with them. */
public final class CalmPiglinsFeature {
    private static final long GRACE_TICKS = 40L;
    private static final double DROP_RADIUS = 16.0D;
    private static final Map<UUID, Long> TRADERS = new ConcurrentHashMap<>();

    private CalmPiglinsFeature() {}

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract.class, CalmPiglinsFeature::onInteract);
        NeoForge.EVENT_BUS.addListener(ItemTossEvent.class, CalmPiglinsFeature::onToss);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, LivingChangeTargetEvent.class, CalmPiglinsFeature::onTarget);
    }

    private static void mark(ServerLevel level, Player player) {
        TRADERS.put(player.getUUID(), level.getGameTime() + GRACE_TICKS);
    }

    private static boolean calm(ServerLevel level, LivingEntity target) {
        if (!(target instanceof Player player)) return false;
        Long until = TRADERS.get(player.getUUID());
        if (until == null) return false;
        if (until < level.getGameTime()) {
            TRADERS.remove(player.getUUID(), until);
            return false;
        }
        return true;
    }

    private static void onInteract(PlayerInteractEvent.EntityInteract e) {
        if (!(e.getTarget() instanceof Piglin piglin) || !(e.getLevel() instanceof ServerLevel level)) return;
        if (e.getHand() != InteractionHand.MAIN_HAND && e.getHand() != InteractionHand.OFF_HAND) return;
        ItemStack held = e.getItemStack();
        if (!held.isEmpty() && piglin.isAdult() && piglin.isBarterCurrency(held)) mark(level, e.getEntity());
    }

    private static void onToss(ItemTossEvent e) {
        if (!(e.getPlayer().level() instanceof ServerLevel level)) return;
        ItemStack stack = e.getEntity().getItem();
        boolean near = !level.getEntitiesOfClass(Piglin.class, e.getPlayer().getBoundingBox().inflate(DROP_RADIUS),
                p -> p.isAdult() && p.isBarterCurrency(stack)).isEmpty();
        if (near) mark(level, e.getPlayer());
    }

    private static void onTarget(LivingChangeTargetEvent e) {
        if (!(e.getEntity() instanceof Piglin piglin) || !(piglin.level() instanceof ServerLevel level)) return;
        LivingEntity next = e.getNewAboutToBeSetTarget();
        if (next != null && calm(level, next)) e.setCanceled(true);
    }
}
