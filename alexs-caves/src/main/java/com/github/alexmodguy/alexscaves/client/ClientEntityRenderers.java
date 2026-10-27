package com.github.alexmodguy.alexscaves.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.render.entity.*;
import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Entity renderer registration (ported from upstream ClientProxy). Types without a ported renderer get the box fallback. */
@EventBusSubscriber(modid = AlexsCaves.MODID, value = Dist.CLIENT)
public class ClientEntityRenderers {
    private static final Set<EntityType<?>> REGISTERED = new HashSet<>();

    private static <T extends Entity> void reg(EntityRenderersEvent.RegisterRenderers event, EntityType<? extends T> type, EntityRendererProvider<T> provider) {
        REGISTERED.add(type);
        event.registerEntityRenderer(type, provider);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        REGISTERED.clear();
        reg(event, ACEntityRegistry.BOAT.get(), (context) -> {
            return new AlexsCavesBoatRenderer(context, false);
        });
        reg(event, ACEntityRegistry.CHEST_BOAT.get(), (context) -> {
            return new AlexsCavesBoatRenderer(context, true);
        });
        reg(event, ACEntityRegistry.MOVING_METAL_BLOCK.get(), MovingMetalBlockRenderer::new);
        reg(event, ACEntityRegistry.TELETOR.get(), TeletorRenderer::new);
        reg(event, ACEntityRegistry.MAGNETIC_WEAPON.get(), MagneticWeaponRenderer::new);
        reg(event, ACEntityRegistry.MAGNETRON.get(), MagnetronRenderer::new);
        reg(event, ACEntityRegistry.BOUNDROID.get(), BoundroidRenderer::new);
        reg(event, ACEntityRegistry.BOUNDROID_WINCH.get(), BoundroidWinchRenderer::new);
        reg(event, ACEntityRegistry.FERROUSLIME.get(), FerrouslimeRenderer::new);
        reg(event, ACEntityRegistry.NOTOR.get(), NotorRenderer::new);
        reg(event, ACEntityRegistry.QUARRY_SMASHER.get(), QuarrySmasherRenderer::new);
        reg(event, ACEntityRegistry.SEEKING_ARROW.get(), SeekingArrowRenderer::new);
        reg(event, ACEntityRegistry.SUBTERRANODON.get(), SubterranodonRenderer::new);
        reg(event, ACEntityRegistry.VALLUMRAPTOR.get(), VallumraptorRenderer::new);
        reg(event, ACEntityRegistry.GROTTOCERATOPS.get(), GrottoceratopsRenderer::new);
        reg(event, ACEntityRegistry.TRILOCARIS.get(), TrilocarisRenderer::new);
        reg(event, ACEntityRegistry.TREMORSAURUS.get(), TremorsaurusRenderer::new);
        reg(event, ACEntityRegistry.RELICHEIRUS.get(), RelicheirusRenderer::new);
        reg(event, ACEntityRegistry.FALLING_TREE_BLOCK.get(), FallingTreeBlockRenderer::new);
        reg(event, ACEntityRegistry.CRUSHED_BLOCK.get(), CrushedBlockRenderer::new);
        reg(event, ACEntityRegistry.LIMESTONE_SPEAR.get(), LimestoneSpearRenderer::new);
        reg(event, ACEntityRegistry.EXTINCTION_SPEAR.get(), ExtinctionSpearRenderer::new);
        reg(event, ACEntityRegistry.DINOSAUR_SPIRIT.get(), DinosaurSpiritRenderer::new);
        reg(event, ACEntityRegistry.LUXTRUCTOSAURUS.get(), LuxtructosaurusRenderer::new);
        reg(event, ACEntityRegistry.TEPHRA.get(), TephraRenderer::new);
        reg(event, ACEntityRegistry.ATLATITAN.get(), AtlatitanRenderer::new);
        reg(event, ACEntityRegistry.NUCLEAR_EXPLOSION.get(), EmptyRenderer::new);
        reg(event, ACEntityRegistry.NUCLEAR_BOMB.get(), NuclearBombRenderer::new);
        reg(event, ACEntityRegistry.NUCLEEPER.get(), NucleeperRenderer::new);
        reg(event, ACEntityRegistry.RADGILL.get(), RadgillRenderer::new);
        reg(event, ACEntityRegistry.BRAINIAC.get(), BrainiacRenderer::new);
        reg(event, ACEntityRegistry.THROWN_WASTE_DRUM.get(), ThrownWasteDrumEntityRenderer::new);
        reg(event, ACEntityRegistry.GAMMAROACH.get(), GammaroachRenderer::new);
        reg(event, ACEntityRegistry.RAYCAT.get(), RaycatRenderer::new);
        reg(event, ACEntityRegistry.CINDER_BRICK.get(), (context) -> {
            return new ThrownItemRenderer<>(context, 1.25F, false);
        });
        reg(event, ACEntityRegistry.TREMORZILLA.get(), TremorzillaRenderer::new);
        reg(event, ACEntityRegistry.LANTERNFISH.get(), LanternfishRenderer::new);
        reg(event, ACEntityRegistry.SEA_PIG.get(), SeaPigRenderer::new);
        reg(event, ACEntityRegistry.SUBMARINE.get(), SubmarineRenderer::new);
        reg(event, ACEntityRegistry.HULLBREAKER.get(), HullbreakerRenderer::new);
        reg(event, ACEntityRegistry.GOSSAMER_WORM.get(), GossamerWormRenderer::new);
        reg(event, ACEntityRegistry.TRIPODFISH.get(), TripodfishRenderer::new);
        reg(event, ACEntityRegistry.DEEP_ONE.get(), DeepOneRenderer::new);
        reg(event, ACEntityRegistry.INK_BOMB.get(), (context) -> {
            return new ThrownItemRenderer<>(context, 1.25F, false);
        });
        reg(event, ACEntityRegistry.DEEP_ONE_KNIGHT.get(), DeepOneKnightRenderer::new);
        reg(event, ACEntityRegistry.DEEP_ONE_MAGE.get(), DeepOneMageRenderer::new);
        reg(event, ACEntityRegistry.WATER_BOLT.get(), WaterBoltRenderer::new);
        reg(event, ACEntityRegistry.WAVE.get(), WaveRenderer::new);
        reg(event, ACEntityRegistry.MINE_GUARDIAN.get(), MineGuardianRenderer::new);
        reg(event, ACEntityRegistry.MINE_GUARDIAN_ANCHOR.get(), MineGuardianAnchorRenderer::new);
        reg(event, ACEntityRegistry.DEPTH_CHARGE.get(), (context) -> {
            return new ThrownItemRenderer<>(context, 1.75F, true);
        });
        reg(event, ACEntityRegistry.FLOATER.get(), FloaterRenderer::new);
        reg(event, ACEntityRegistry.GUANO.get(), (context) -> {
            return new ThrownItemRenderer<>(context, 1.25F, false);
        });
        reg(event, ACEntityRegistry.FALLING_GUANO.get(), FallingBlockRenderer::new);
        reg(event, ACEntityRegistry.GLOOMOTH.get(), GloomothRenderer::new);
        reg(event, ACEntityRegistry.UNDERZEALOT.get(), UnderzealotRenderer::new);
        reg(event, ACEntityRegistry.WATCHER.get(), WatcherRenderer::new);
        reg(event, ACEntityRegistry.CORRODENT.get(), CorrodentRenderer::new);
        reg(event, ACEntityRegistry.VESPER.get(), VesperRenderer::new);
        reg(event, ACEntityRegistry.FORSAKEN.get(), ForsakenRenderer::new);
        reg(event, ACEntityRegistry.BEHOLDER_EYE.get(), EmptyRenderer::new);
        reg(event, ACEntityRegistry.DESOLATE_DAGGER.get(), DesolateDaggerRenderer::new);
        reg(event, ACEntityRegistry.BURROWING_ARROW.get(), BurrowingArrowRenderer::new);
        reg(event, ACEntityRegistry.DARK_ARROW.get(), DarkArrowRenderer::new);
        reg(event, ACEntityRegistry.SWEETISH_FISH.get(), SweetishFishRenderer::new);
        reg(event, ACEntityRegistry.CANIAC.get(), CaniacRenderer::new);
        reg(event, ACEntityRegistry.GUMBEEPER.get(), GumbeeperRenderer::new);
        reg(event, ACEntityRegistry.GUMBALL.get(), GumballRenderer::new);
        reg(event, ACEntityRegistry.CANDICORN.get(), CandicornRenderer::new);
        reg(event, ACEntityRegistry.GUM_WORM.get(), GumWormRenderer::new);
        reg(event, ACEntityRegistry.GUM_WORM_SEGMENT.get(), GumWormSegmentRenderer::new);
        reg(event, ACEntityRegistry.CARAMEL_CUBE.get(), CaramelCubeRenderer::new);
        reg(event, ACEntityRegistry.MELTED_CARAMEL.get(), MeltedCaramelRenderer::new);
        reg(event, ACEntityRegistry.GUMMY_BEAR.get(), GummyBearRenderer::new);
        reg(event, ACEntityRegistry.LICOWITCH.get(), LicowitchRenderer::new);
        reg(event, ACEntityRegistry.SPINNING_PEPPERMINT.get(), SpinningPeppermintRenderer::new);
        reg(event, ACEntityRegistry.SUGAR_STAFF_HEX.get(), SugarStaffHexRenderer::new);
        reg(event, ACEntityRegistry.GINGERBREAD_MAN.get(), GingerbreadManRenderer::new);
        reg(event, ACEntityRegistry.FALLING_FROSTMINT.get(), FallingBlockRenderer::new);
        reg(event, ACEntityRegistry.CANDY_CANE_HOOK.get(), CandyCaneHookRenderer::new);
        reg(event, ACEntityRegistry.SODA_BOTTLE_ROCKET.get(), (render) -> {
            return new ThrownItemRenderer<>(render, 1.25F, true);
        });
        reg(event, ACEntityRegistry.FROSTMINT_SPEAR.get(), FrostmintSpearRenderer::new);
        reg(event, ACEntityRegistry.THROWN_ICE_CREAM_SCOOP.get(), (context) -> {
            return new ThrownItemRenderer<>(context, 1.25F, false);
        });
        FallbackRenderers.register(event, REGISTERED);
    }
}
