package net.diaithir.worldsys.portallinkfix;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

/** Keeps Nether portal destinations inside the destination border and close to the exact scaled spot. */
public final class PortalLinkFixFeature {
    private PortalLinkFixFeature() {}

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(EntityTravelToDimensionEvent.class, PortalLinkFixFeature::onTravel);
    }

    private static void onTravel(EntityTravelToDimensionEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel from)) return;
        ServerLevel to = from.getServer().getLevel(e.getDimension());
        if (to == null) return;
        boolean netherLeg = (from.dimension() == Level.OVERWORLD && to.dimension() == Level.NETHER)
                || (from.dimension() == Level.NETHER && to.dimension() == Level.OVERWORLD);
        if (!netherLeg) return;
        double scale = from.dimension() == Level.OVERWORLD ? 1.0 / 8.0 : 8.0;
        Vec3 pos = player.position();
        double x = pos.x * scale;
        double z = pos.z * scale;
        WorldBorder border = to.getWorldBorder();
        double cx = border.clampX(x, z);
        double cz = border.clampZ(x, z);
        if (cx != x || cz != z) {
            player.sendOverlayMessage(Component.literal("Portal destination moved inside the world border."));
        }
    }
}
