package net.diaithir.claims.permissions;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

/**
 * Permission nodes for gameplay actions, so a rank or permission mod can allow or deny them per player:
 * pg.break, pg.interact, pg.use, pg.attack. Ported from PermissionGate (ngoclinh_, MIT, Fabric) to NeoForge's PermissionAPI.
 * Difference from upstream: every node defaults to allowed (upstream defaults to denied, which locks out everyone not granted).
 */
public final class Gate {
    public static final String NS = "pg";
    public static final PermissionNode<Boolean> BREAK = node("break");
    public static final PermissionNode<Boolean> INTERACT = node("interact");
    public static final PermissionNode<Boolean> USE = node("use");
    public static final PermissionNode<Boolean> ATTACK = node("attack");

    private Gate() {}

    private static PermissionNode<Boolean> node(String name) {
        return new PermissionNode<>(NS, name, PermissionTypes.BOOLEAN, (player, uuid, ctx) -> true);
    }

    public static void init(IEventBus bus) {
        NeoForge.EVENT_BUS.addListener((PermissionGatherEvent.Nodes e) -> e.addNodes(BREAK, INTERACT, USE, ATTACK));
        NeoForge.EVENT_BUS.register(new Gate());
    }

    private static boolean allowed(net.minecraft.world.entity.player.Player p, PermissionNode<Boolean> n, String what) {
        if (!(p instanceof ServerPlayer sp)) return true;
        if (PermissionAPI.getPermission(sp, n)) return true;
        sp.sendOverlayMessage(Component.literal("You may not " + what + " here."));
        return false;
    }

    @SubscribeEvent
    public void onBreak(net.neoforged.neoforge.event.level.block.BreakBlockEvent e) {
        if (!allowed(e.getPlayer(), BREAK, "break blocks")) e.setCanceled(true);
    }

    @SubscribeEvent
    public void onBlock(PlayerInteractEvent.RightClickBlock e) {
        if (!allowed(e.getEntity(), INTERACT, "use blocks")) e.setCanceled(true);
    }

    @SubscribeEvent
    public void onEntity(PlayerInteractEvent.EntityInteract e) {
        if (!allowed(e.getEntity(), INTERACT, "interact")) e.setCanceled(true);
    }

    @SubscribeEvent
    public void onItem(PlayerInteractEvent.RightClickItem e) {
        if (!allowed(e.getEntity(), USE, "use items")) e.setCanceled(true);
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) {
        boolean frame = e.getTarget() instanceof ItemFrame || e.getTarget() instanceof ArmorStand;
        if (!allowed(e.getEntity(), frame ? INTERACT : ATTACK, frame ? "interact" : "attack")) e.setCanceled(true);
    }
}
