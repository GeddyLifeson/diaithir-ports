// Ported from Dynamic Surroundings Rebirth (MIT) https://github.com/uawyiegrfv/DynamicSurroundingsRebirth
// Modifications: biome/weather/footstep handlers re-implemented on NeoForge client tick events with vanilla SoundEvents.
package net.diaithir.audio.surroundings.client;

import net.diaithir.audio.surroundings.config.SurroundingsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class SurroundingsClient {
    private static final RandomSource RNG = RandomSource.create();

    private static double lastX, lastZ;
    private static boolean wasOnGround = true;
    private static float fallDist;
    private static double stepAccum;
    private static int windTicks;
    private static int thunderCooldown;

    private SurroundingsClient() {}

    public static void init(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(new Handlers());
    }

    private static void play(Minecraft mc, SoundEvent ev, double x, double y, double z, float vol, float pitch) {
        if (vol <= 0.001F || mc.level == null) return;
        mc.level.playLocalSound(x, y, z, ev, SoundSource.AMBIENT, vol, pitch, false);
    }

    public static final class Handlers {
        @SubscribeEvent
        public void onTick(ClientTickEvent.Post e) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer p = mc.player;
            ClientLevel lvl = mc.level;
            if (p == null || lvl == null || mc.isPaused()) return;
            ambient(mc, p, lvl);
            weather(mc, p, lvl);
            footsteps(mc, p, lvl);
        }

        @SubscribeEvent
        public void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
            wasOnGround = true;
            fallDist = 0;
            stepAccum = 0;
            windTicks = 0;
            thunderCooldown = 0;
        }
    }

    private static void ambient(Minecraft mc, LocalPlayer p, ClientLevel lvl) {
        if (!SurroundingsConfig.ENABLE_BIOME_SOUNDS.get()) return;
        if (RNG.nextDouble() >= SurroundingsConfig.AMBIENT_CHANCE.get()) return;
        BlockPos pos = p.blockPosition();
        if (!lvl.canSeeSky(pos) && pos.getY() > lvl.getSeaLevel() - 8) return;
        Holder<Biome> biome = lvl.getBiome(pos);
        SoundEvent ev = pickBiomeSound(biome, lvl, pos);
        if (ev == null) return;
        float vol = (float) (0.35D * SurroundingsConfig.BIOME_VOLUME.get() * SurroundingsConfig.AMBIENT_VOLUME.get());
        double ox = p.getX() + (RNG.nextDouble() - 0.5D) * 24.0D;
        double oz = p.getZ() + (RNG.nextDouble() - 0.5D) * 24.0D;
        play(mc, ev, ox, p.getY(), oz, vol, 0.85F + RNG.nextFloat() * 0.3F);
    }

    private static SoundEvent pickBiomeSound(Holder<Biome> biome, ClientLevel lvl, BlockPos pos) {
        boolean night = lvl.getSkyDarken() > 8;
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_BEACH)) return SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS;
        if (biome.is(BiomeTags.IS_JUNGLE)) return night ? SoundEvents.BAT_AMBIENT : SoundEvents.PARROT_AMBIENT;
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_TAIGA)) return night ? SoundEvents.BAT_AMBIENT : SoundEvents.PARROT_AMBIENT;
        if (biome.is(BiomeTags.IS_NETHER)) return SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS.value();
        if (biome.is(BiomeTags.IS_END)) return SoundEvents.AMBIENT_CAVE.value();
        if (biome.value().coldEnoughToSnow(pos, lvl.getSeaLevel())) return SoundEvents.FOX_SCREECH;
        if (night) return SoundEvents.BAT_AMBIENT;
        return null;
    }

    private static void weather(Minecraft mc, LocalPlayer p, ClientLevel lvl) {
        float rain = lvl.getRainLevel(1.0F);
        float thunder = lvl.getThunderLevel(1.0F);
        BlockPos pos = p.blockPosition();
        boolean outside = lvl.canSeeSky(pos);
        if (SurroundingsConfig.ENABLE_WIND_SOUNDS.get() && rain > 0.05F && outside) {
            if (++windTicks >= 60) {
                windTicks = 0;
                float vol = (float) (0.2F * rain * SurroundingsConfig.AMBIENT_VOLUME.get());
                play(mc, SoundEvents.WEATHER_RAIN_ABOVE, p.getX(), p.getY(), p.getZ(), vol, 0.7F + thunder * 0.2F);
            }
        } else {
            windTicks = 0;
        }
        if (thunderCooldown > 0) thunderCooldown--;
        if (SurroundingsConfig.ENABLE_BACKGROUND_THUNDER.get() && thunder > 0.2F && thunderCooldown <= 0 && RNG.nextInt(400) == 0) {
            thunderCooldown = 200;
            double a = RNG.nextDouble() * Math.PI * 2.0D;
            double d = 60.0D + RNG.nextDouble() * 60.0D;
            float vol = (float) (0.5F * thunder * SurroundingsConfig.AMBIENT_VOLUME.get());
            play(mc, SoundEvents.LIGHTNING_BOLT_THUNDER, p.getX() + Math.cos(a) * d, p.getY(), p.getZ() + Math.sin(a) * d, vol, 0.5F + RNG.nextFloat() * 0.2F);
        }
    }

    private static void footsteps(Minecraft mc, LocalPlayer p, ClientLevel lvl) {
        boolean ground = p.onGround();
        if (SurroundingsConfig.ENABLE_JUMP_LAND.get()) {
            float pv = (float) (0.4F * SurroundingsConfig.PLAYER_EFFECT_VOLUME.get());
            if (wasOnGround && !ground && p.getDeltaMovement().y > 0.1D && !p.isInWater()) {
                BlockState below = lvl.getBlockState(p.blockPosition().below());
                if (!below.isAir()) {
                    SoundType st = below.getSoundType();
                    play(mc, st.getStepSound(), p.getX(), p.getY(), p.getZ(), pv * st.getVolume(), st.getPitch() * 0.9F);
                }
            }
            if (!ground && p.getDeltaMovement().y < 0.0D) {
                fallDist += (float) -p.getDeltaMovement().y;
            }
            if (!wasOnGround && ground) {
                if (fallDist > 0.9F) {
                    BlockState below = lvl.getBlockState(p.blockPosition().below());
                    SoundType st = below.getSoundType();
                    play(mc, st.getFallSound(), p.getX(), p.getY(), p.getZ(), Math.min(1.0F, pv * (0.5F + fallDist * 0.3F)) * st.getVolume(), st.getPitch());
                }
                fallDist = 0;
            }
            if (ground) fallDist = 0;
        }
        wasOnGround = ground;

        double dx = p.getX() - lastX;
        double dz = p.getZ() - lastZ;
        lastX = p.getX();
        lastZ = p.getZ();
        if (!SurroundingsConfig.ENABLE_FOOTSTEP_SOUNDS.get() || !ground || p.isInWater() || p.isPassenger()) {
            stepAccum = 0;
            return;
        }
        if (p.isCrouching() && !SurroundingsConfig.ENABLE_SNEAK_FOOTSTEPS.get()) return;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 1.0E-4D || dist > 2.0D) return;
        double stride = p.isSprinting() ? 1.1D : p.isCrouching() ? 0.7D : 0.8D;
        stepAccum += dist;
        if (stepAccum < stride) return;
        stepAccum = 0;

        BlockPos feet = p.blockPosition().below();
        BlockState state = lvl.getBlockState(feet);
        if (state.isAir()) return;
        SoundType st = state.getSoundType();
        float vol = (float) (0.25F * SurroundingsConfig.FOOTSTEP_VOLUME.get()) * st.getVolume();
        if (p.isCrouching()) vol *= 0.6F;
        play(mc, st.getStepSound(), p.getX(), p.getY(), p.getZ(), vol, st.getPitch() * (0.95F + RNG.nextFloat() * 0.1F));

        if (SurroundingsConfig.ENABLE_FOOTSTEP_ACCENTS.get()) {
            if (SurroundingsConfig.ENABLE_WET_ACCENTS.get() && (lvl.isRainingAt(p.blockPosition()) || lvl.getFluidState(p.blockPosition()).isSource())) {
                play(mc, SoundEvents.GENERIC_SPLASH, p.getX(), p.getY(), p.getZ(), vol * 0.15F, 1.5F + RNG.nextFloat() * 0.3F);
            }
            if (!p.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).isEmpty()) {
                play(mc, SoundEvents.ARMOR_EQUIP_CHAIN.value(), p.getX(), p.getY(), p.getZ(), vol * 0.12F, 1.6F + RNG.nextFloat() * 0.3F);
            }
        }
    }
}
