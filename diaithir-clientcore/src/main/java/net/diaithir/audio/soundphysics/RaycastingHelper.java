// Ported from Sound Physics Perfected (MIT) https://github.com/FalseMSP/SoundPhysicsPerfected
// Modifications: config via ModConfigSpec, Lombok removed, mixin @Unique dropped, SLF4J logging, package moved.
package net.diaithir.audio.soundphysics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.diaithir.audio.soundphysics.config.SoundPhysicsConfig;
import net.diaithir.audio.soundphysics.config.SoundPhysicsConfig.AttenuationType;
import net.diaithir.audio.soundphysics.config.SoundPhysicsConfig.DebugType;
import net.diaithir.audio.soundphysics.data.AveragedSoundData;
import net.diaithir.audio.soundphysics.data.BlueRayResult;
import net.diaithir.audio.soundphysics.data.RayHitData;
import net.diaithir.audio.soundphysics.data.RaycastResult;
import net.diaithir.audio.soundphysics.data.SoundData;
import net.diaithir.audio.soundphysics.data.TickableSoundData;
import net.diaithir.audio.soundphysics.reverb.EnhancedReverbData;
import net.diaithir.audio.soundphysics.reverb.ReverbSurfaceData;
import net.diaithir.audio.soundphysics.wrappers.RedPermeatedSoundInstance;
import net.diaithir.audio.soundphysics.wrappers.RedSoundInstance;
import net.diaithir.audio.soundphysics.wrappers.RedTickableInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Raycasting helper. Ray colours:
 * White: normal bouncing ray, Green: sound seeking ray, Blue: reverb seeking ray, Red: sound seeking permeated ray.
 */
public class RaycastingHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger("diaithir_audio_soundphysics");

    public static final Queue<RedTickableInstance> tickQueue = new LinkedList<>();
    public static final Queue<RedPermeatedSoundInstance> permeatedTickQueue = new LinkedList<>();
    public static final Queue<SoundData> soundQueue = new LinkedList<>();
    public static final ArrayList<SoundInstance> notProcd = new ArrayList<>();
    private static final double SPEED_OF_SOUND_TICKS = 17.15;
    private static final Map<Integer, ArrayList<SoundInstance>> soundPlayingWaiting = new ConcurrentHashMap<>();
    private static int ticksSinceWorld;

    // Reverb state
    private static final AtomicReference<Double> distanceFromWallEcho = new AtomicReference<>(0.0);
    private static final AtomicReference<Double> distanceFromWallEchoDenom = new AtomicReference<>(0.0);
    private static final AtomicInteger reverbStrength = new AtomicInteger(0);
    private static final AtomicInteger reverbDenom = new AtomicInteger(0);
    private static final AtomicInteger outdoorLeak = new AtomicInteger(0);
    private static final AtomicInteger outdoorLeakDenom = new AtomicInteger(0);
    private static final AtomicInteger totalRaysHitSurface = new AtomicInteger(0);
    private static final ConcurrentHashMap<String, ReverbSurfaceData> surfaceMaterials = new ConcurrentHashMap<>();
    private static final AtomicInteger totalSurfaceArea = new AtomicInteger(0);
    private static final AtomicReference<Double> averageAbsorption = new AtomicReference<>(0.0);
    private static final AtomicReference<Double> surfaceToVolumeRatio = new AtomicReference<>(0.0);
    private static final AtomicReference<Double> earlyReflectionStrength = new AtomicReference<>(0.0);
    private static final AtomicReference<Double> lateReflectionStrength = new AtomicReference<>(0.0);
    private static final AtomicReference<Double> weightedReverbStrength = new AtomicReference<>(0.0);
    private static final AtomicInteger earlyReflectionCount = new AtomicInteger(0);
    private static final AtomicInteger lateReflectionCount = new AtomicInteger(0);

    // Ray data
    private static final ConcurrentHashMap<SoundData, List<RayHitData>> rayHitsByEntity = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<SoundData, List<RayHitData>> redRaysToTarget = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<SoundData, AveragedSoundData> muffledAveragedResults = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<SoundInstance, RedTickableInstance> soundInstanceMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<SoundInstance, RedPermeatedSoundInstance> soundPermInstanceMap = new ConcurrentHashMap<>();

    private static final int THREAD_POOL_SIZE = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
    // Executors are recreated on demand: shutdown() is called whenever the sound engine is destroyed.
    private static volatile ExecutorService raycastExecutor = createExecutor(THREAD_POOL_SIZE, "SPP-Raycast");
    private static volatile ExecutorService soundProcessingExecutor = createExecutor(2, "SPP-SoundProcessing");

    private static ExecutorService createExecutor(int threads, String name) {
        AtomicInteger counter = new AtomicInteger();
        return Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r, name + "-" + counter.incrementAndGet());
            t.setDaemon(true);
            return t;
        });
    }

    private static synchronized ExecutorService getRaycastExecutor() {
        if (raycastExecutor.isShutdown()) raycastExecutor = createExecutor(THREAD_POOL_SIZE, "SPP-Raycast");
        return raycastExecutor;
    }

    private static synchronized ExecutorService getSoundProcessingExecutor() {
        if (soundProcessingExecutor.isShutdown()) soundProcessingExecutor = createExecutor(2, "SPP-SoundProcessing");
        return soundProcessingExecutor;
    }

    private static final AtomicBoolean isRaytracing = new AtomicBoolean(false);
    public static Vec3 playerEyePos = new Vec3(0, 0, 0);
    private static Vec3[] rayDirections;
    private static int lastRaysCast = -1;
    private static volatile List<CompletableFuture<Void>> rayTasks;

    static {
        surfaceMaterials.put("default", new ReverbSurfaceData(0.05, 0.7, "medium"));
    }

    public static final Queue<RedPermeatedSoundInstance> FXQueue = new LinkedList<>();
    public static OpenALEffectsHandler fxHandler = new OpenALEffectsHandler();

    // Entrypoint #1
    public static void castBouncingRaysAndDetectSFX(Level world, Player player) {
        try {
            playerEyePos = player.getEyePosition();
            double maxTotalDistance = 16.0 * SoundPhysicsConfig.MAX_RAY_LENGTH.get() * SoundPhysicsConfig.RAYS_BOUNCED.get();

            Minecraft client = Minecraft.getInstance();
            if (client == null || client.getSoundManager() == null) return;

            int raysCast = SoundPhysicsConfig.RAYS_CAST.get();
            if (raysCast != lastRaysCast || rayDirections == null) rayDirections = generateRayDirections();
            lastRaysCast = raysCast;

            if (processAndPlayAveragedSounds(world, player, playerEyePos, new ArrayList<>(Arrays.asList(rayDirections)), maxTotalDistance, client) == -1)
                return;

            rayHitsByEntity.clear();
            redRaysToTarget.clear();
            rayTasks = null;
            isRaytracing.set(false);
        } catch (Exception e) {
            rayTasks = null;
            isRaytracing.set(false);
            rayHitsByEntity.clear();
            redRaysToTarget.clear();
            LOGGER.error("Error in player bouncing ray entity detection", e);
        }
    }

    public static void playQueuedObjects() {
        ticksSinceWorld++;

        if (soundPlayingWaiting.isEmpty()) return;

        Minecraft client = Minecraft.getInstance();

        List<SoundInstance> soundsToPlay = new ArrayList<>();
        for (ArrayList<SoundInstance> soundList : soundPlayingWaiting.values()) {
            synchronized (soundList) {
                soundsToPlay.addAll(soundList);
            }
        }

        for (SoundInstance newSound : soundsToPlay) {
            if (newSound == null) continue;
            client.getSoundManager().play(newSound);
        }

        notProcd.clear();
        soundPlayingWaiting.clear();
    }

    public static int processAndPlayAveragedSounds(Level world, Player player, Vec3 playerEyePos,
                                                   List<Vec3> rayDirections,
                                                   double maxTotalDistance, Minecraft client) {
        if (processRaysWithAveraging(world, player, playerEyePos, rayDirections, maxTotalDistance) == -1) return -1;

        Map<SoundData, AveragedSoundData> averagedResults = new ConcurrentHashMap<>();
        muffledAveragedResults.clear();

        for (Map.Entry<SoundData, List<RayHitData>> entry : rayHitsByEntity.entrySet()) {
            averagedResults.put(entry.getKey(), calculateWeightedAverages(entry.getKey(), entry.getValue()));
        }

        for (Map.Entry<SoundData, List<RayHitData>> entry : redRaysToTarget.entrySet()) {
            muffledAveragedResults.put(entry.getKey(), calculateWeightedAverages(entry.getKey(), entry.getValue()));
        }

        if (averagedResults.isEmpty() && muffledAveragedResults.isEmpty()) return 0;

        List<CompletableFuture<Void>> soundTasks = new ArrayList<>();
        boolean permeation = SoundPhysicsConfig.PERMEATION.get();
        if (!permeation) {
            for (AveragedSoundData avgData : averagedResults.values()) {
                soundTasks.add(CompletableFuture.runAsync(() ->
                        playAveragedSoundWithAdjustments(client, avgData, playerEyePos, 1.0f, 1.0f),
                        getSoundProcessingExecutor()));
            }
        } else {
            for (AveragedSoundData avgData : muffledAveragedResults.values()) {
                soundTasks.add(CompletableFuture.runAsync(() ->
                        playMuffled(client, avgData, playerEyePos, 1f, 1f, world, player),
                        getSoundProcessingExecutor()));
            }
        }

        CompletableFuture.allOf(soundTasks.toArray(new CompletableFuture[0])).join();
        return 0;
    }

    public static void playAveragedSoundWithAdjustments(Minecraft client, AveragedSoundData avgData, Vec3 playerPos,
                                                        float volumeMultiplier, float pitchMultiplier) {
        if (client == null || client.level == null || avgData == null) return;

        try {
            Vec3 targetPosition = playerPos.add(avgData.averageDirection.scale(avgData.averageDistance));
            if (avgData.totalWeight == 0) targetPosition = avgData.soundEntity.position;

            SoundInstance originalSound = avgData.soundEntity.sound;
            Identifier soundId = originalSound.getIdentifier();
            if (avgData.totalWeight == 0 && originalSound instanceof RedTickableInstance rt) {
                rt.setTargetVolume(0);
                rt.setTargetPosition(rt.getOriginalPosition());
                rt.setAttenuationMultiplier(0);
                return;
            }

            float baseVolume;
            if (originalSound instanceof RedTickableInstance rt) baseVolume = rt.getOriginalVolume();
            else baseVolume = ((RedSoundInstance) originalSound).original.getVolume();

            float confidenceMultiplier;
            float attenuationMultiplier = 1;
            AttenuationType type = SoundPhysicsConfig.ATTENUATION_TYPE.get();
            if (type == AttenuationType.VERCIDIUM_INVERSE_SQUARE) {
                confidenceMultiplier = (float) avgData.rayCount / Math.max(totalRaysHitSurface.get(), 1);
                attenuationMultiplier = 1.0f / (float) Math.pow(Math.max(avgData.averageDistance, 0.01), 2);
            } else if (type == AttenuationType.VERCIDIUM_LINEAR) {
                confidenceMultiplier = (float) avgData.rayCount / Math.max(totalRaysHitSurface.get(), 1);
                attenuationMultiplier = 1.0f / (float) Math.max(avgData.averageDistance, 0.01);
            } else {
                confidenceMultiplier = (float) avgData.totalWeight / SoundPhysicsConfig.RAYS_CAST.get() * SoundPhysicsConfig.RAYS_BOUNCED.get();
            }
            float adjustedVolume = baseVolume * volumeMultiplier * confidenceMultiplier * attenuationMultiplier;

            float basePitch = originalSound.getPitch();
            float adjustedPitch = basePitch * pitchMultiplier;

            if (originalSound instanceof RedTickableInstance rt) {
                rt.setTargetPosition(targetPosition);
                rt.setTargetVolume(Math.max(0.01f, Math.min(1.0f, adjustedVolume)));
                rt.setAttenuationMultiplier(attenuationMultiplier);
                return;
            }

            RedSoundInstance redOriginal = (RedSoundInstance) originalSound;
            RedTickableInstance newSound = new RedTickableInstance(soundId, originalSound.getSound(), originalSound.getSource(),
                    targetPosition, Math.max(0.001f, Math.min(1.0f, adjustedVolume)),
                    Math.max(0.5f, Math.min(2.0f, adjustedPitch)), redOriginal.getOriginal(), attenuationMultiplier);

            soundInstanceMap.put(redOriginal.getOriginal(), newSound);
            debugMessage(client, redOriginal.getOriginal().toString());

            queueSound(newSound);
        } catch (Exception e) {
            LOGGER.error("Error playing adjusted averaged sound: {}", e.getMessage());
        }
    }

    public static void playMuffled(Minecraft client, AveragedSoundData avgData, Vec3 playerPos,
                                   float volumeMultiplier, float pitchMultiplier, Level world, Player player) {
        if (client == null || client.level == null || avgData == null) return;

        try {
            Vec3 targetPosition = playerPos.add(avgData.averageDirection.scale(avgData.averageDistance));
            if (avgData.totalWeight == 0) targetPosition = avgData.soundEntity.position;

            SoundInstance originalSound = avgData.soundEntity.sound;
            Identifier soundId = originalSound.getIdentifier();

            float baseVolume;
            if (originalSound instanceof RedTickableInstance rt) baseVolume = rt.getOriginalVolume();
            else baseVolume = ((RedSoundInstance) originalSound).original.getVolume();

            float confidenceMultiplier;
            float attenuationMultiplier = 1;
            AttenuationType type = SoundPhysicsConfig.ATTENUATION_TYPE.get();
            if (type == AttenuationType.VERCIDIUM_INVERSE_SQUARE) {
                confidenceMultiplier = (float) avgData.rayCount / Math.max(totalRaysHitSurface.get(), 1);
                attenuationMultiplier = 1.0f / (float) Math.pow(Math.max(avgData.averageDistance, 0.01), 2);
            } else if (type == AttenuationType.VERCIDIUM_LINEAR) {
                confidenceMultiplier = (float) avgData.rayCount / Math.max(totalRaysHitSurface.get(), 1);
                attenuationMultiplier = 1.0f / (float) Math.max(avgData.averageDistance, 0.01);
            } else {
                confidenceMultiplier = (float) avgData.totalWeight / SoundPhysicsConfig.RAYS_CAST.get() * SoundPhysicsConfig.RAYS_BOUNCED.get();
            }
            float permeationIndex = (float) avgData.averageMuffle;

            if (SoundPhysicsConfig.SHORTCUT_DIRECTIONALITY.get() && hasLineOfSight(world, player, originalSound)
                    && originalSound instanceof RedTickableInstance rt) {
                targetPosition = rt.getOriginalPosition();
                confidenceMultiplier = 1;
            } else if (SoundPhysicsConfig.SHORTCUT_DIRECTIONALITY.get() && hasLineOfSight(world, player, originalSound)) {
                targetPosition = new Vec3(originalSound.getX(), originalSound.getY(), originalSound.getZ());
                confidenceMultiplier = 1;
            }

            float adjustedVolume = baseVolume * volumeMultiplier * confidenceMultiplier * attenuationMultiplier * permeationIndex;

            float basePitch = originalSound.getPitch();
            float adjustedPitch = basePitch * pitchMultiplier;

            if (avgData.totalWeight == 0 && originalSound instanceof RedPermeatedSoundInstance rp) {
                rp.setTargetVolume(0);
                rp.setTargetPosition(rp.getOriginalPosition());
                rp.setPermeationIndex(0);
                rp.setAttenuationMultiplier(0);
                return;
            }

            if (originalSound instanceof RedPermeatedSoundInstance rp) {
                rp.setTargetPosition(targetPosition);
                rp.setTargetVolume(adjustedVolume);
                rp.setPermeationIndex(permeationIndex);
                rp.setAttenuationMultiplier(attenuationMultiplier);
                return;
            }

            RedSoundInstance redOriginal = (RedSoundInstance) originalSound;
            RedPermeatedSoundInstance newSound = new RedPermeatedSoundInstance(soundId, originalSound.getSound(),
                    originalSound.getSource(), targetPosition, Math.max(0.01f, adjustedVolume), adjustedPitch,
                    redOriginal.getOriginal(), permeationIndex, attenuationMultiplier);
            soundPermInstanceMap.put(redOriginal.getOriginal(), newSound);

            debugMessage(client, redOriginal.getOriginal().toString());

            queueSound(newSound);
        } catch (Exception e) {
            LOGGER.error("Error playing adjusted averaged sound: {}", e.getMessage());
        }
    }

    private static void debugMessage(Minecraft client, String text) {
        if (client.player == null) return;
        DebugType debug = SoundPhysicsConfig.DEBUG.get();
        if (debug == DebugType.ACTION_BAR) client.player.sendOverlayMessage(Component.literal(text));
        else if (debug == DebugType.CHAT) client.player.sendSystemMessage(Component.literal(text));
    }

    private static void queueSound(SoundInstance newSound) {
        soundPlayingWaiting.computeIfAbsent(ticksSinceWorld + 1, k -> new ArrayList<>()).add(newSound);
    }

    public static int processRaysWithAveraging(Level world, Player player,
                                               Vec3 playerEyePos, List<Vec3> rayDirections,
                                               double maxTotalDistance) {
        if (isRaytracing.compareAndSet(false, true)) {
            final ConcurrentLinkedQueue<SoundData> sQ = new ConcurrentLinkedQueue<>(soundQueue);
            soundQueue.clear();
            final ConcurrentLinkedQueue<RedTickableInstance> tQ = new ConcurrentLinkedQueue<>(tickQueue);
            tickQueue.clear();
            final ConcurrentLinkedQueue<RedPermeatedSoundInstance> pTQ = new ConcurrentLinkedQueue<>(permeatedTickQueue);
            permeatedTickQueue.clear();
            try {
                rayTasks = createRayTasks(world, player, playerEyePos, rayDirections, sQ, tQ, pTQ, maxTotalDistance);
            } catch (Throwable t) {
                rayTasks = null;
                isRaytracing.set(false);
                throw t;
            }
        }

        List<CompletableFuture<Void>> tasks = rayTasks;
        if (tasks == null) {
            isRaytracing.set(false);
            return -1;
        }

        if (!CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).isDone()) return -1;
        return 0;
    }

    private static List<CompletableFuture<Void>> createRayTasks(Level world, Player player,
                                                                Vec3 playerEyePos, List<Vec3> rayDirections,
                                                                Queue<SoundData> sq, Queue<RedTickableInstance> tQ,
                                                                Queue<RedPermeatedSoundInstance> pTQ, double maxTotalDistance) {
        reverbStrength.set(0);
        weightedReverbStrength.set(0.0);
        distanceFromWallEcho.set(0.0);
        distanceFromWallEchoDenom.set(0.0);
        reverbDenom.set(0);
        lateReflectionCount.set(0);
        earlyReflectionStrength.set(0.0);
        lateReflectionStrength.set(0.0);
        earlyReflectionCount.set(0);
        outdoorLeak.set(0);
        outdoorLeakDenom.set(0);
        totalRaysHitSurface.set(0);
        totalSurfaceArea.set(0);
        averageAbsorption.set(0.0);

        int raysPerChunk = Math.max(1, rayDirections.size() / THREAD_POOL_SIZE);
        List<List<Vec3>> rayChunks = new ArrayList<>();
        for (int i = 0; i < rayDirections.size(); i += raysPerChunk) {
            int endIndex = Math.min(i + raysPerChunk, rayDirections.size());
            rayChunks.add(rayDirections.subList(i, endIndex));
        }

        List<CompletableFuture<Void>> tasks = new ArrayList<>();
        for (List<Vec3> rayChunk : rayChunks) {
            tasks.add(CompletableFuture.runAsync(() -> {
                for (Vec3 direction : rayChunk) {
                    castBouncingRay(world, player, playerEyePos, direction, sq, tQ, pTQ, maxTotalDistance);
                }
            }, getRaycastExecutor()));
        }
        return tasks;
    }

    private static ClipContext barrierAwareContext(Level world, Vec3 from, Vec3 to, Player player) {
        return new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player) {
            @Override
            public VoxelShape getBlockShape(BlockState blockState, BlockGetter level, BlockPos pos) {
                if (blockState.getBlock() == Blocks.BARRIER && SoundPhysicsConfig.BARRIER_AS_AIR.get()) {
                    return Shapes.empty();
                }
                return super.getBlockShape(blockState, level, pos);
            }
        };
    }

    public static void castBouncingRay(Level world, Player player, Vec3 startPos, Vec3 direction,
                                       Queue<SoundData> sQ, Queue<RedTickableInstance> tQ,
                                       Queue<RedPermeatedSoundInstance> pTQ, double maxTotalDistance) {
        Vec3 currentPos = startPos;
        Vec3 currentDirection = direction.normalize();
        Vec3 initialDirection = currentDirection.normalize();
        double remainingDistance = maxTotalDistance;
        double totalDistanceTraveled = 0.0;

        int raysBounced = SoundPhysicsConfig.RAYS_BOUNCED.get();
        double maxLen = 16.0 * SoundPhysicsConfig.MAX_RAY_LENGTH.get();
        boolean permeation = SoundPhysicsConfig.PERMEATION.get();
        boolean reverb = SoundPhysicsConfig.REVERB.get();
        double bounceAbsorption = SoundPhysicsConfig.BOUNCE_ABSORPTION_MULTIPLIER.get();

        if (permeation) castRedRay(world, player, startPos, sQ, tQ, pTQ, totalDistanceTraveled, initialDirection, -1);
        else castGreenRay(world, player, startPos, sQ, tQ, pTQ, totalDistanceTraveled, initialDirection, -1);

        for (int bounce = 0; bounce <= raysBounced && remainingDistance > 0; bounce++) {
            double segmentDistance = Math.min(maxLen, remainingDistance);
            Vec3 segmentEnd = currentPos.add(currentDirection.scale(segmentDistance));

            BlockHitResult blockHit = world.clip(barrierAwareContext(world, currentPos, segmentEnd, player));

            Vec3 actualEnd = segmentEnd;
            boolean hitBlock = false;

            if (blockHit.getType() == HitResult.Type.BLOCK) {
                actualEnd = blockHit.getLocation();
                hitBlock = true;
            }

            double segmentTraveled = currentPos.distanceTo(actualEnd);
            totalDistanceTraveled += segmentTraveled;
            totalDistanceTraveled *= 1.0 / bounceAbsorption;

            if (hitBlock) {
                if (reverb) {
                    BlueRayResult blueRayResult = castBlueRay(world, player, actualEnd, sQ, totalDistanceTraveled, initialDirection, bounce);
                    if (blueRayResult.arrived) {
                        initialDirection = blueRayResult.directionFromPlayer;
                        totalDistanceTraveled = blueRayResult.distance;
                        totalDistanceTraveled *= 1.0 / bounceAbsorption;
                    }
                }
                if (permeation)
                    castRedRay(world, player, actualEnd, sQ, tQ, pTQ, totalDistanceTraveled, initialDirection, bounce);
                else
                    castGreenRay(world, player, actualEnd, sQ, tQ, pTQ, totalDistanceTraveled, initialDirection, bounce);

                Vec3 hitPos = blockHit.getLocation();
                Direction hitSide = blockHit.getDirection();

                Vec3 reflectedDirection = calculateReflection(currentDirection, hitSide);

                currentPos = hitPos.add(reflectedDirection.scale(0.01));
                currentDirection = reflectedDirection;
                remainingDistance -= segmentTraveled;
                totalRaysHitSurface.incrementAndGet();
                outdoorLeakDenom.incrementAndGet();
            } else {
                Vec3 toCenter = player.position().subtract(actualEnd);
                Vec3 normal = toCenter.normalize();
                Vec3 reflectedDirection = calculateReflection(currentDirection, normal);

                currentPos = segmentEnd.add(reflectedDirection.scale(0.01));
                currentDirection = reflectedDirection;
                remainingDistance -= segmentTraveled;

                outdoorLeak.incrementAndGet();
                outdoorLeakDenom.incrementAndGet();
                if (bounce == 0) return;
            }
        }
    }

    private static void castGreenRay(Level world, Player player, Vec3 currentPos, Queue<SoundData> sQ,
                                     Queue<RedTickableInstance> tQ, Queue<RedPermeatedSoundInstance> pTQ,
                                     double currentDistance, Vec3 initialDirection, int bounces) {
        for (SoundData soundEntity : sQ) {
            rayHitsByEntity.computeIfAbsent(soundEntity, k -> new CopyOnWriteArrayList<>());
            Vec3 entityCenter = soundEntity.position;
            double distanceToEntity = currentPos.distanceTo(entityCenter);

            if (distanceToEntity + currentDistance > 16 * soundEntity.sound.getVolume()) continue;

            ClipContext raycastContext = new ClipContext(currentPos, entityCenter,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
            BlockHitResult blockHit = world.clip(raycastContext);

            boolean hasLineOfSight = blockHit.getType() != HitResult.Type.BLOCK
                    || currentPos.distanceTo(blockHit.getLocation()) >= distanceToEntity - 1;

            if (hasLineOfSight) {
                double weight = getWeight(currentDistance, 0, distanceToEntity);
                RaycastResult result = new RaycastResult(distanceToEntity, initialDirection, soundEntity);
                Vec3 normalVector = entityCenter.subtract(currentPos).normalize();
                RayHitData hitData = new RayHitData(result, normalVector, weight, 0, bounces);
                rayHitsByEntity.computeIfAbsent(soundEntity, k -> new CopyOnWriteArrayList<>()).add(hitData);
            }
        }

        for (RedTickableInstance soundEntity : tQ) {
            SoundData data = new TickableSoundData(soundEntity, soundEntity.getOriginalPosition(), soundEntity.getSound().toString());
            rayHitsByEntity.computeIfAbsent(data, k -> new CopyOnWriteArrayList<>());

            Vec3 entityCenter = soundEntity.getOriginalPosition();
            double distanceToEntity = currentPos.distanceTo(entityCenter);

            if (distanceToEntity + currentDistance > 16 * soundEntity.getOriginalVolume()) continue;

            ClipContext raycastContext = new ClipContext(currentPos, entityCenter,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
            BlockHitResult blockHit = world.clip(raycastContext);

            boolean hasLineOfSight = blockHit.getType() != HitResult.Type.BLOCK
                    || currentPos.distanceTo(blockHit.getLocation()) >= distanceToEntity - 1;

            if (hasLineOfSight) {
                double weight = getWeight(currentDistance, 0, distanceToEntity);
                RaycastResult result = new RaycastResult(distanceToEntity, initialDirection, data);
                Vec3 normalVector = entityCenter.subtract(currentPos).normalize();
                RayHitData hitData = new RayHitData(result, normalVector, weight, 0, bounces);
                rayHitsByEntity.computeIfAbsent(data, k -> new CopyOnWriteArrayList<>()).add(hitData);
            }
        }
    }

    private static BlueRayResult castBlueRay(Level world, Player player, Vec3 currentPos,
                                             Queue<SoundData> entities, double currentDistance,
                                             Vec3 initialDirection, int bounceNumber) {
        Vec3 entityCenter = player.getBoundingBox().getCenter();
        Vec3 toPlayer = entityCenter.subtract(currentPos).normalize();
        Vec3 rayStartPos = currentPos.add(toPlayer.scale(0.1));

        ClipContext raycastContext = new ClipContext(rayStartPos, entityCenter,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player);
        BlockHitResult blockHit = world.clip(raycastContext);
        boolean hasLineOfSight = blockHit.getType() != HitResult.Type.BLOCK;

        if (bounceNumber <= 2) {
            analyzeSurfaceAtPosition(world, currentPos, currentDistance);
        }

        double reflectionDelay = currentDistance / SPEED_OF_SOUND_TICKS;
        if (reflectionDelay < 0.05) {
            earlyReflectionStrength.updateAndGet(current -> current + (hasLineOfSight ? 1.0 : 0.0));
            earlyReflectionCount.incrementAndGet();
        } else {
            lateReflectionStrength.updateAndGet(current -> current + (hasLineOfSight ? 0.6 : 0.0));
            lateReflectionCount.incrementAndGet();
        }

        reverbDenom.incrementAndGet();

        if (hasLineOfSight) {
            distanceFromWallEcho.updateAndGet(current -> current + currentDistance);
            distanceFromWallEchoDenom.updateAndGet(current -> current + 1.0);
            reverbStrength.incrementAndGet();

            Vec3 playerToAdjustedPos = rayStartPos.subtract(entityCenter);
            Vec3 reflectionAngle = initialDirection.subtract(toPlayer);
            double angleDeviation = Math.abs(reflectionAngle.length());

            double reflectionQuality = Math.max(0.1, 1.0 - angleDeviation);
            weightedReverbStrength.updateAndGet(current -> current + reflectionQuality);
            return new BlueRayResult(true, playerToAdjustedPos, entityCenter.distanceTo(rayStartPos));
        }

        return new BlueRayResult(false, null, -1);
    }

    private static double getAbsorptionCoeficient(Level world, Vec3 pos) {
        if (!SoundPhysicsConfig.USE_EXPLOSION_RESISTANCE.get()) return 1;
        BlockPos blockPos = new BlockPos((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));
        BlockState blockState = world.getBlockState(blockPos);

        if (!blockState.isAir()) {
            if (blockState.is(BlockTags.DAMPENS_VIBRATIONS) || blockState.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS))
                return SoundPhysicsConfig.MAX_BLOCKS_PERMEATED.get();
            return Math.min(blockState.getBlock().getExplosionResistance() / 6, 5.0);
        }
        return 1;
    }

    private static void analyzeSurfaceAtPosition(Level world, Vec3 pos, double distance) {
        BlockPos blockPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
        BlockState blockState = world.getBlockState(blockPos);

        if (!blockState.isAir()) {
            String materialName = blockState.getBlock().getName().getString().toLowerCase();
            ReverbSurfaceData surfaceData = surfaceMaterials.getOrDefault(materialName, surfaceMaterials.get("default"));

            if (blockState.is(BlockTags.DAMPENS_VIBRATIONS) || blockState.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS))
                surfaceData.absorptionCoefficient = SoundPhysicsConfig.MAX_BLOCKS_PERMEATED.get();
            else
                surfaceData.absorptionCoefficient = Math.min(blockState.getBlock().getExplosionResistance() / 6, 5.0);

            double distanceWeight = 1.0 / Math.max(distance, 1.0);
            final double absorptionNow = surfaceData.absorptionCoefficient;
            averageAbsorption.updateAndGet(current -> current + (absorptionNow * distanceWeight));
            totalSurfaceArea.updateAndGet(current -> current + 1);
        }
    }

    public static EnhancedReverbData calculateEnhancedReverb() {
        double totalSurface = totalSurfaceArea.get();
        double avgAbsorption = totalSurface > 0 ? averageAbsorption.get() / totalSurface : 0.05;
        double denom = distanceFromWallEchoDenom.get();
        double volume = denom > 0 ? Math.pow(distanceFromWallEcho.get() / denom, 3) : 0.0;
        double surfaceToVolRatio = surfaceToVolumeRatio.get();

        double totalAbsorption = totalSurface * avgAbsorption;
        double rt60 = totalAbsorption > 0 ? (0.161 * volume) / totalAbsorption : 0.0;

        double roomRadius = Math.cbrt(volume * 3.0 / (4.0 * Math.PI));
        double earlyReflectionDelay = roomRadius / SPEED_OF_SOUND_TICKS;

        double lateStrength = lateReflectionCount.get() > 0
                ? lateReflectionStrength.get() / lateReflectionCount.get() : 0.0;

        String acousticProfile = determineAcousticProfile(avgAbsorption, surfaceToVolRatio, volume);

        int leakDenom = outdoorLeakDenom.get();
        boolean isIndoors = leakDenom > 0 && (double) outdoorLeak.get() / leakDenom < 0.05;

        return new EnhancedReverbData(rt60, earlyReflectionDelay, lateStrength,
                roomRadius, avgAbsorption, acousticProfile, isIndoors);
    }

    private static String determineAcousticProfile(double absorption, double surfaceToVolRatio, double volume) {
        if (volume > 10000) return "cathedral";
        if (absorption > 0.6) return "padded_room";
        if (absorption < 0.1 && surfaceToVolRatio < 0.3) return "gymnasium";
        if (surfaceToVolRatio > 1.0) return "small_room";
        if (absorption > 0.3) return "living_room";
        return "generic_room";
    }

    public static EnhancedReverbData getEnhancedReverbData() {
        return calculateEnhancedReverb();
    }

    public static double getWeightedReverbStrength() {
        return weightedReverbStrength.get();
    }

    public static double getEarlyReflectionRatio() {
        int totalEarly = earlyReflectionCount.get();
        int totalLate = lateReflectionCount.get();
        int total = totalEarly + totalLate;
        return total > 0 ? (double) totalEarly / total : 0.0;
    }

    private static void castRedRay(Level world, Player player, Vec3 currentPos, Queue<SoundData> sQ,
                                   Queue<RedTickableInstance> tQ, Queue<RedPermeatedSoundInstance> pTQ,
                                   double currentDistance, Vec3 initialDirection, int bounces) {
        double maxLen = SoundPhysicsConfig.MAX_RAY_LENGTH.get() * 16;
        double absorptionBase = SoundPhysicsConfig.PERMEATION_ABSORPTION.get();

        for (SoundData soundEntity : sQ) {
            redRaysToTarget.computeIfAbsent(soundEntity, k -> new CopyOnWriteArrayList<>());
            Vec3 entityCenter = soundEntity.position;
            double distanceToEntity = currentPos.distanceTo(entityCenter);

            if (distanceToEntity + currentDistance > maxLen) continue;

            double blockCount = countBlocksBetween(world, currentPos, entityCenter, player);

            double weight = getWeight(currentDistance, blockCount, distanceToEntity);
            double permeationAbsorption = Math.pow(absorptionBase, blockCount);
            weight *= permeationAbsorption;

            RaycastResult rayResult = new RaycastResult(distanceToEntity, initialDirection, soundEntity);
            Vec3 normalVector = entityCenter.subtract(currentPos).normalize();
            RayHitData hitData = new RayHitData(rayResult, normalVector, weight, permeationAbsorption, bounces);

            redRaysToTarget.computeIfAbsent(soundEntity, k -> new CopyOnWriteArrayList<>()).add(hitData);
        }
        for (RedPermeatedSoundInstance soundEntity : pTQ) {
            SoundData data = new TickableSoundData(soundEntity, soundEntity.getOriginalPosition(), soundEntity.getSound().toString());

            Vec3 entityCenter = soundEntity.getOriginalPosition();
            double distanceToEntity = currentPos.distanceTo(entityCenter);

            if (distanceToEntity + currentDistance > maxLen) continue;

            double blockCount = countBlocksBetween(world, currentPos, entityCenter, player);

            double weight = getWeight(currentDistance, blockCount, distanceToEntity);
            double permeationAbsorption = Math.pow(absorptionBase, blockCount);
            weight *= permeationAbsorption;

            RaycastResult rayResult = new RaycastResult(distanceToEntity, initialDirection, data);
            Vec3 normalVector = entityCenter.subtract(currentPos).normalize();
            RayHitData hitData = new RayHitData(rayResult, normalVector, weight, permeationAbsorption, bounces);
            redRaysToTarget.computeIfAbsent(data, k -> new CopyOnWriteArrayList<>()).add(hitData);
        }
    }

    private static double getWeight(double currentDistance, double blockCount, double distanceToEntity) {
        double permeationAbsorption = Math.pow(SoundPhysicsConfig.PERMEATION_ABSORPTION.get(), blockCount);
        double d = Math.max(distanceToEntity + currentDistance, 0.1);
        AttenuationType type = SoundPhysicsConfig.ATTENUATION_TYPE.get();
        double weight;
        if (type == AttenuationType.INVERSE_SQUARE) weight = permeationAbsorption / (d * d);
        else if (type == AttenuationType.LINEAR) weight = permeationAbsorption / d;
        else if (type == AttenuationType.VERCIDIUM_INVERSE_SQUARE) weight = permeationAbsorption / (d * d);
        else if (type == AttenuationType.VERCIDIUM_LINEAR) weight = permeationAbsorption / (d * d);
        else weight = 0;
        return weight;
    }

    private static AveragedSoundData calculateWeightedAverages(SoundData entity, List<RayHitData> rayHits) {
        double totalWeight = 0.0;
        double weightedDistanceSum = 0.0;
        double weightedMuffleSum = 0.0;
        Vec3 weightedDirectionSum = Vec3.ZERO;

        for (RayHitData rayHit : rayHits) {
            double weight = rayHit.weight;
            totalWeight += weight;
            weightedDistanceSum += rayHit.rayResult.totalDistance * weight;
            weightedMuffleSum += rayHit.muffleFac * weight;
            weightedDirectionSum = weightedDirectionSum.add(rayHit.rayResult.initialDirection.scale(weight));
        }

        if (totalWeight == 0.0)
            return new AveragedSoundData(entity, weightedDirectionSum, weightedDistanceSum,
                    totalWeight, rayHits.size(), rayHits);

        double averageDistance = weightedDistanceSum / totalWeight;
        Vec3 averageDirection = weightedDirectionSum.scale(1.0 / totalWeight).normalize();

        return new AveragedSoundData(entity, averageDirection, averageDistance,
                totalWeight, rayHits.size(), rayHits, weightedMuffleSum / totalWeight);
    }

    public static double countBlocksBetween(Level world, Vec3 start, Vec3 end, Player player) {
        double totalDistanceInBlocks = 0;
        Vec3 currentStart = start;

        BlockPos endBlockPos = new BlockPos((int) Math.floor(end.x), (int) Math.floor(end.y), (int) Math.floor(end.z));
        double maxPermeated = SoundPhysicsConfig.MAX_BLOCKS_PERMEATED.get();
        double step = SoundPhysicsConfig.PERMEATION_STEP_SIZE.get();

        while (totalDistanceInBlocks < maxPermeated) {
            BlockHitResult hit = world.clip(barrierAwareContext(world, currentStart, end, player));

            if (hit.getType() != HitResult.Type.BLOCK) break;

            BlockPos hitBlockPos = hit.getBlockPos();
            BlockState blockState = world.getBlockState(hitBlockPos);

            if (hitBlockPos.equals(endBlockPos)) break;

            Vec3 direction = end.subtract(currentStart).normalize();

            VoxelShape blockShape = blockState.getShape(world, hitBlockPos);
            AABB blockBounds;
            if (!blockShape.isEmpty()) {
                blockBounds = blockShape.bounds().move(hitBlockPos);
            } else {
                blockBounds = new AABB(
                        hitBlockPos.getX(), hitBlockPos.getY(), hitBlockPos.getZ(),
                        hitBlockPos.getX() + 1, hitBlockPos.getY() + 1, hitBlockPos.getZ() + 1);
            }

            if (blockBounds.contains(end)) break;

            Vec3 exitPoint = hit.getLocation();
            int guard = 0;
            while (blockBounds.contains(exitPoint) && guard++ < 4096) {
                exitPoint = exitPoint.add(direction.scale(step));
            }

            if (currentStart.distanceTo(start) >= end.distanceTo(start)) break;

            double distanceInBlock = hit.getLocation().distanceTo(exitPoint);
            double absorptionIndex = getAbsorptionCoeficient(world, hit.getLocation());
            totalDistanceInBlocks += distanceInBlock * absorptionIndex;

            currentStart = exitPoint.add(direction.scale(0.01));
        }
        return totalDistanceInBlocks;
    }

    public static boolean hasLineOfSight(Level world, Player player, SoundInstance sound) {
        if (sound instanceof RedTickableInstance rt)
            return countBlocksBetween(world, player.getEyePosition(), rt.getOriginalPosition(), player) == 0;
        return countBlocksBetween(world, player.getEyePosition(), new Vec3(sound.getX(), sound.getY(), sound.getZ()), player) == 0;
    }

    public static Vec3 calculateReflection(Vec3 incident, Direction hitSide) {
        Vec3 normal = Vec3.atLowerCornerOf(hitSide.getUnitVec3i());
        double dotProduct = incident.dot(normal);
        return incident.subtract(normal.scale(2 * dotProduct));
    }

    public static Vec3 calculateReflection(Vec3 incident, Vec3 normal) {
        double dotProduct = incident.dot(normal);
        return incident.subtract(normal.scale(2 * dotProduct));
    }

    public static Vec3[] generateRayDirections() {
        int numRays = SoundPhysicsConfig.RAYS_CAST.get();
        Vec3[] directions = new Vec3[numRays];

        double goldenRatio = (1 + Math.sqrt(5)) / 2;

        for (int i = 0; i < numRays; i++) {
            double theta = 2 * Math.PI * i / goldenRatio;
            double phi = Math.acos(1 - 2.0 * (i + 0.5) / numRays);

            double x = Math.sin(phi) * Math.cos(theta);
            double y = Math.cos(phi);
            double z = Math.sin(phi) * Math.sin(theta);

            directions[i] = new Vec3(x, y, z);
        }

        return directions;
    }

    public static double getDistanceFromWallEcho() {
        return distanceFromWallEcho.get();
    }

    public static double getDistanceFromWallEchoDenom() {
        return distanceFromWallEchoDenom.get();
    }

    public static int getReverbStrength() {
        return reverbStrength.get();
    }

    public static int getReverbDenom() {
        return reverbDenom.get();
    }

    public static int getOutdoorLeak() {
        return outdoorLeak.get();
    }

    public static int getOutdoorLeakDenom() {
        return outdoorLeakDenom.get();
    }

    public static synchronized void shutdown() {
        rayTasks = null;
        isRaytracing.set(false);
        try {
            raycastExecutor.shutdown();
            soundProcessingExecutor.shutdown();

            if (!raycastExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                raycastExecutor.shutdownNow();
            }

            if (!soundProcessingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                soundProcessingExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            raycastExecutor.shutdownNow();
            soundProcessingExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
