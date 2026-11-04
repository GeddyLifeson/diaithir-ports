package net.diaithir.druidry.ars.common.event;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.api.ArsNouveauAPI;
import net.diaithir.druidry.ars.api.scrying.IScryer;
import net.diaithir.druidry.ars.client.ClientInfo;
import net.diaithir.druidry.ars.client.particle.ColorPos;
import net.diaithir.druidry.ars.client.particle.GlowParticleData;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.network.Networking;
import net.diaithir.druidry.ars.common.network.PacketGetPersistentData;
import net.diaithir.druidry.ars.setup.registry.ModPotions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT, modid = ArsNouveau.HOST)
public class ScryEvents {
    @SubscribeEvent
    public static void playerLoginEvent(final PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity().hasEffect(ModPotions.SCRYING_EFFECT)) {
            CompoundTag tag = event.getEntity().getPersistentData().getCompoundOrEmpty(Player.PERSISTED_NBT_TAG);
            Networking.sendToPlayerClient((CustomPacketPayload) new PacketGetPersistentData(tag), (ServerPlayer) event.getEntity());
        }
    }

    @SubscribeEvent
    public static void playerTickEvent(final PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide() && event.getEntity().getEffect(ModPotions.SCRYING_EFFECT) != null && ClientInfo.ticksInGame % 30 == 0) {

            List<BlockPos> scryingPos = new ArrayList<>();
            CompoundTag tag = ClientInfo.persistentData;
            if (!tag.contains("an_scryer"))
                return;
            IScryer base = ArsNouveauAPI.getInstance().getScryer(Identifier.tryParse(tag.getCompoundOrEmpty("an_scryer").getStringOr("id", "")));
            if (base == null)
                return;
            IScryer scryer = base.fromTag(tag.getCompoundOrEmpty("an_scryer"));
            Player playerEntity = event.getEntity();
            Level world = playerEntity.level();
            Vec3i scrySize = scryer.getScryingSize();
            for (BlockPos p : BlockPos.withinManhattan(playerEntity.blockPosition(), scrySize.getX(), scrySize.getY(), scrySize.getZ())) {
                if (world.isOutsideBuildHeight(p) || world.getBlockState(p).isAir())
                    continue;
                if (scryingPos.size() >= scryer.getScryMax())
                    break;

                if (scryer.shouldRevealBlock(world.getBlockState(p), p, playerEntity)) {
                    scryingPos.add(new BlockPos(p));
                }
            }
            ClientInfo.scryingPositions = scryingPos;
        }
    }

    @SubscribeEvent
    public static void onRenderHighlights(final ClientTickEvent.Post event) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;

        if(ClientInfo.highlightTicks > 0){
            ClientInfo.highlightTicks--;
            for(ColorPos pos : ClientInfo.highlightPositions){
                double xzOffset = 0.15;
                double xOffset = ParticleUtil.inRange(-xzOffset / 4, xzOffset / 4) + 0.5;
                double zOffset = ParticleUtil.inRange(-xzOffset / 4, xzOffset / 4) + 0.5;
                double centerX = pos.pos.getX() + xOffset;
                double centerZ = pos.pos.getZ() + zOffset;

                double xSpeedOffset = 0;
                double ySpeedOffset = ParticleUtil.inRange(0.0, 0.03f);
                double zSpeedOffset = 0;

                xSpeedOffset = ParticleUtil.inRange(-0.01f, 0.01f);
                zSpeedOffset = ParticleUtil.inRange(-0.01f, 0.01f);
                Vec3 renderPos = new Vec3(centerX, pos.pos.getY(), centerZ);
                ParticleColor color = new ParticleColor(pos.color.r, pos.color.g, pos.color.b);
                world.addParticle(
                        GlowParticleData.createData(color, true),
                        renderPos.x, renderPos.y  + ParticleUtil.inRange(-0.00, 0.1), renderPos.z,
                        xSpeedOffset, ySpeedOffset, zSpeedOffset);
            }
        }
    }

    @SubscribeEvent
    public static void renderScry(final ClientTickEvent.Post event) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;
        final Player playerEntity = Minecraft.getInstance().player;
        if (playerEntity == null || playerEntity.getEffect(ModPotions.SCRYING_EFFECT) == null)
            return;
        Vec3 vector3d = Minecraft.getInstance().gameRenderer.getMainCamera().position();

        double yView = vector3d.y();
        if (Minecraft.getInstance().isPaused())
            return;
        RandomSource rand = playerEntity.getRandom();
        for (BlockPos p : ClientInfo.scryingPositions) {
            ParticleColor color = new ParticleColor(
                    rand.nextInt(255),
                    rand.nextInt(255),
                    rand.nextInt(255));
            BlockPos renderPos = new BlockPos(p);
            if (Math.abs(yView - p.getY()) >= 30) {
                renderPos = new BlockPos(p.getX(), (int) (p.getY() > yView ? yView + 20 : yView - 20), p.getZ());
                color = new ParticleColor(
                        rand.nextInt(30),
                        rand.nextInt(255),
                        rand.nextInt(50));
            }

            if (Math.abs(yView - p.getY()) >= 60) {
                renderPos = new BlockPos(p.getX(), (int) (p.getY() > yView ? yView + 20 : yView - 20), p.getZ());
                color = new ParticleColor(
                        rand.nextInt(50),
                        rand.nextInt(50),
                        rand.nextInt(255));
            }

            world.addParticle(
                    GlowParticleData.createData(color, true),
                    renderPos.getX() + 0.5 + ParticleUtil.inRange(-0.1, 0.1), renderPos.getY() + 0.2 + ParticleUtil.inRange(-0.1, 0.1), renderPos.getZ() + 0.5 + ParticleUtil.inRange(-0.1, 0.1),
                    0, 0.03f, 0);
        }
    }
}
