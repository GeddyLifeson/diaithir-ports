package com.github.alexmodguy.alexscaves.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.block.fluid.ACFluidRegistry;
import com.github.alexmodguy.alexscaves.server.entity.util.PossessesCamera;
import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import com.github.alexmodguy.alexscaves.server.potion.DeepsightEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

/** Trimmed 26.1 port of upstream ClientEvents: state ticking, fluid/magma fog, camera roll, hand cancel, bow FOV. Biome sky/fog overrides are data-driven in 26.1 and not ported. */
@EventBusSubscriber(modid = AlexsCaves.MODID, value = Dist.CLIENT)
public class ClientEvents {

    private static boolean deepsight(Minecraft mc) {
        return mc.player != null && mc.player.hasEffect(ACEffectRegistry.DEEPSIGHT);
    }

    private static float deepsightAmount(Minecraft mc, double pt) {
        return DeepsightEffect.getIntensity(mc.player, (float) pt);
    }

    private static float sampleBiome(Entity player, com.github.alexmodguy.alexscaves.server.level.biome.BiomeSampler.FloatFetcher fetcher) {
        if (Minecraft.getInstance().options.biomeBlendRadius().get() == 0) {
            return fetcher.fetch(player.level().getBiome(player.blockPosition()));
        }
        return com.github.alexmodguy.alexscaves.server.level.biome.BiomeSampler.sampleBiomesFloat(player.level(), player.position(), fetcher);
    }

    @SubscribeEvent
    public static void clientLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || !entity.level().isClientSide()) return;
        if (entity instanceof net.minecraft.client.player.LocalPlayer player) {
            var inv = player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.getItem() instanceof com.github.alexmodguy.alexscaves.server.item.ClientTickingItem ticking) {
                    ticking.clientInventoryTick(stack, player.level(), player);
                }
            }
        }
        if (entity.hasEffect(ACEffectRegistry.DARKNESS_INCARNATE) && entity.isAlive()) {
            int trailPointer = ClientProxy.darknessTrailPointerMap.getOrDefault(entity, -1);
            Vec3 latest = entity.position();
            if (ClientProxy.darknessTrailPosMap.get(entity) == null) {
                Vec3[] trailPositions = new Vec3[64];
                if (trailPointer == -1) Arrays.fill(trailPositions, latest);
                ClientProxy.darknessTrailPosMap.put(entity, trailPositions);
            }
            if (++trailPointer == ClientProxy.darknessTrailPosMap.get(entity).length) trailPointer = 0;
            ClientProxy.darknessTrailPointerMap.put(entity, trailPointer);
            ClientProxy.darknessTrailPosMap.get(entity)[trailPointer] = latest;
        } else if (ClientProxy.darknessTrailPosMap.containsKey(entity)) {
            ClientProxy.darknessTrailPosMap.remove(entity);
            ClientProxy.darknessTrailPointerMap.remove(entity);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getCameraEntity() instanceof PossessesCamera) {
            event.setCanceled(true);
            return;
        }
        if (mc.player != null) {
            var ray = com.github.alexmodguy.alexscaves.client.render.misc.RaygunBeam.extractFirstPersonHand(mc.player, event.getHand(), event.getPartialTick());
            if (ray != null) {
                var cam = mc.gameRenderer.getMainCamera();
                boolean left = (event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND) == (mc.player.getMainArm() == net.minecraft.world.entity.HumanoidArm.LEFT);
                org.joml.Vector3f start = new org.joml.Vector3f(left ? -0.42F : 0.42F, -0.3F, -0.9F);
                cam.rotation().transform(start);
                Vec3 camFrom = new Vec3(start.x, start.y, start.z);
                Vec3 camTo = ray.to().subtract(cam.position());
                com.github.alexmodguy.alexscaves.client.render.misc.RaygunBeam.submit(event.getSubmitNodeCollector().order(3), event.getPoseStack(), camFrom, camTo, cam.rotation(), ray);
            }
        }
    }

    @SubscribeEvent
    public static void computeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        Entity player = mc.getCameraEntity();
        if (player instanceof PossessesCamera) mc.options.setCameraType(CameraType.FIRST_PERSON);
        if (player instanceof LivingEntity living && living.hasEffect(ACEffectRegistry.STUNNED)) {
            event.setRoll((float) (Math.sin((player.tickCount + event.getPartialTick()) * 0.2F) * 10F));
        }
    }

    @SubscribeEvent
    public static void onComputeFOVModifier(ComputeFovModifierEvent event) {
        ItemStack stack = event.getPlayer().getUseItem();
        if (event.getPlayer().isUsingItem() && stack.is(ACItemRegistry.DREADBOW.get())) {
            float f1 = event.getPlayer().getTicksUsingItem() / 20.0F;
            f1 = f1 > 1.0F ? 1.0F : f1 * f1;
            event.setNewFovModifier(event.getFovModifier() * (1.0F - f1 * 0.15F));
        }
    }

    @SubscribeEvent
    public static void fogRender(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        Entity player = mc.getCameraEntity();
        if (player == null || mc.player == null) return;
        BlockPos pos = event.getCamera().blockPosition();
        FluidState fluid = player.level().getFluidState(pos);
        BlockState state = player.level().getBlockState(pos);
        float pt = (float) event.getPartialTick();
        float far = -1, near = 0;
        if (!fluid.isEmpty() && fluid.getType().getFluidType().equals(ACFluidRegistry.ACID_FLUID_TYPE.get())) {
            far = 10.0F * (deepsight(mc) ? 1.0F + 1.5F * deepsightAmount(mc, pt) : 1.0F);
        } else if (!fluid.isEmpty() && fluid.getType().getFluidType().equals(ACFluidRegistry.PURPLE_SODA_FLUID_TYPE.get())) {
            far = 20.0F;
            near = -8.0F;
            if (deepsight(mc)) {
                float f = deepsightAmount(mc, pt);
                far *= 1.0F + 1.5F * f;
                near *= 1.0F - f;
            }
        } else if (state.is(ACBlockRegistry.PRIMAL_MAGMA.get()) || state.is(ACBlockRegistry.FISSURE_PRIMAL_MAGMA.get())) {
            far = 2.0F * (deepsight(mc) ? 1.0F + 1.5F * deepsightAmount(mc, pt) : 1.0F);
        }
        if (far > 0) {
            event.setNearPlaneDistance(near);
            event.setFarPlaneDistance(far);
            return;
        }
        // biome fog distance overrides (fog colors and sky colors are data-driven biome attributes in 26.1)
        if (event.getType() == net.minecraft.world.level.material.FogType.WATER && AlexsCaves.CLIENT_CONFIG.biomeWaterFogOverrides.get()) {
            float farness = sampleBiome(player, com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRegistry::getBiomeWaterFogFarness);
            if (deepsight(mc)) farness *= 1.0F + 1.5F * deepsightAmount(mc, pt);
            if (farness != 1.0F) event.scaleFarPlaneDistance(farness);
        } else if (event.getType() == net.minecraft.world.level.material.FogType.ATMOSPHERIC && AlexsCaves.CLIENT_CONFIG.biomeSkyFogOverrides.get()) {
            float nearness = sampleBiome(player, com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRegistry::getBiomeFogNearness);
            float boss = AlexsCaves.PROXY.getPrimordialBossActiveAmount(pt);
            if (boss > 0.0F) nearness *= (1.0F - boss * 0.75F);
            if (Math.abs(nearness - 1.0F) > 0.01F) event.scaleNearPlaneDistance(nearness);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void fogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        Entity player = mc.player;
        if (player == null) return;
        BlockPos pos = event.getCamera().blockPosition();
        BlockState state = player.level().getBlockState(pos);
        FluidState fluid = player.level().getFluidState(pos);
        if (state.is(ACBlockRegistry.PRIMAL_MAGMA.get()) || state.is(ACBlockRegistry.FISSURE_PRIMAL_MAGMA.get())) {
            event.setRed(1F); event.setGreen(0.4F); event.setBlue(0F);
        } else if (!fluid.isEmpty() && fluid.getType().getFluidType().equals(ACFluidRegistry.ACID_FLUID_TYPE.get())) {
            event.setRed(0F); event.setGreen(1F); event.setBlue(0F);
        } else if (!fluid.isEmpty() && fluid.getType().getFluidType().equals(ACFluidRegistry.PURPLE_SODA_FLUID_TYPE.get())) {
            event.setRed(0.6F); event.setGreen(0.1F); event.setBlue(0.85F);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Entity cameraEntity = mc.getCameraEntity();
        float partialTicks = AlexsCaves.PROXY.getPartialTicks();
        ClientProxy.tickBubbledEffects();
        ACPostEffects.tick(mc);
        if (ClientProxy.shaderLoadAttemptCooldown > 0) ClientProxy.shaderLoadAttemptCooldown--;
        ClientProxy.prevPrimordialBossActiveAmount = ClientProxy.primordialBossActiveAmount;
        ClientProxy.prevNukeFlashAmount = ClientProxy.nukeFlashAmount;
        if (ClientProxy.renderNukeSkyDarkFor > 0) ClientProxy.renderNukeSkyDarkFor--;
        if (ClientProxy.muteNonNukeSoundsFor > 0) {
            ClientProxy.muteNonNukeSoundsFor--;
            if (ClientProxy.masterVolumeNukeModifier < 1.0F) ClientProxy.masterVolumeNukeModifier += 0.1F;
        } else if (ClientProxy.masterVolumeNukeModifier > 0.0F) {
            ClientProxy.masterVolumeNukeModifier -= 0.1F;
        }
        if (ClientProxy.lastBossLevel != mc.level) {
            ClientProxy.primordialBossActive = false;
            ClientProxy.primordialBossActiveAmount = 0;
            ClientProxy.lastBossLevel = mc.level;
        }
        if (ClientProxy.primordialBossActive) {
            if (ClientProxy.primordialBossActiveAmount < 1.0F) ClientProxy.primordialBossActiveAmount += 0.025F;
        } else if (ClientProxy.primordialBossActiveAmount > 0.0F) {
            ClientProxy.primordialBossActiveAmount -= 0.025F;
        }
        if (ClientProxy.renderNukeFlashFor > 0) {
            if (ClientProxy.nukeFlashAmount < 1F) ClientProxy.nukeFlashAmount = Math.min(ClientProxy.nukeFlashAmount + 0.4F, 1F);
            ClientProxy.renderNukeFlashFor--;
        } else if (ClientProxy.nukeFlashAmount > 0F) {
            ClientProxy.nukeFlashAmount = Math.max(ClientProxy.nukeFlashAmount - 0.05F, 0F);
        }
        ClientProxy.prevPossessionStrengthAmount = ClientProxy.possessionStrengthAmount;
        if (cameraEntity instanceof PossessesCamera watcher) {
            float target = watcher.getPossessionStrength(partialTicks);
            if (watcher.instant()) {
                ClientProxy.possessionStrengthAmount = target;
            } else if (ClientProxy.possessionStrengthAmount < target) {
                ClientProxy.possessionStrengthAmount = Math.min(ClientProxy.possessionStrengthAmount + 0.2F, target);
            } else {
                ClientProxy.possessionStrengthAmount = Math.max(ClientProxy.possessionStrengthAmount - 0.2F, target);
            }
            if (watcher instanceof com.github.alexmodguy.alexscaves.server.entity.item.BeholderEyeEntity eye && mc.player != null) {
                eye.setOldRots();
                eye.setEyeYRot(mc.player.getYHeadRot());
                eye.setEyeXRot(mc.player.getXRot());
                if (AlexsCaves.PROXY.isKeyDown(4)) AlexsCaves.PROXY.resetRenderViewEntity(mc.player);
            }
        } else if (ClientProxy.possessionStrengthAmount > 0F) {
            ClientProxy.possessionStrengthAmount = Math.max(ClientProxy.possessionStrengthAmount - 0.05F, 0F);
        }
        if (ClientProxy.primordialBossActive && mc.level != null && cameraEntity != null && !mc.isPaused()) {
            var level = mc.level;
            BlockPos cam = cameraEntity.blockPosition();
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            int dist = 16;
            for (int n = 0; n < 100; ++n) {
                p.set(cam.getX() + level.getRandom().nextInt(dist) - level.getRandom().nextInt(dist),
                        cam.getY() + level.getRandom().nextInt(dist) - level.getRandom().nextInt(dist),
                        cam.getZ() + level.getRandom().nextInt(dist) - level.getRandom().nextInt(dist));
                if (!level.getBlockState(p).isCollisionShapeFullBlock(level, p)) {
                    level.addParticle(ParticleTypes.ASH, p.getX() + level.getRandom().nextDouble(), p.getY() + level.getRandom().nextDouble(), p.getZ() + level.getRandom().nextDouble(), 0, 0, 0);
                }
            }
        }
    }
}
