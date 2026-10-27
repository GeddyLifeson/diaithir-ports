package org.diaithir.connected.client;

import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.client.AllBlockEntityRenders;
import com.zurrtum.create.client.content.kinetics.transmission.SplitShaftRenderer;
import com.zurrtum.create.client.content.kinetics.transmission.SplitShaftVisual;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.audio.KineticAudioBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.RotationDirectionScrollBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.diaithir.connected.content.brassgearbox.BrassGearboxBlockEntity;
import org.diaithir.connected.content.parallelgearbox.ParallelGearboxBlockEntity;
import org.diaithir.connected.content.sixwaygearbox.SixWayGearboxBlockEntity;
import org.diaithir.connected.registries.CCBlockEntityTypes;

public class CreateConnectedClient {
    public static void init(IEventBus bus) {
        bus.addListener(FMLClientSetupEvent.class, e -> e.enqueueWork(CreateConnectedClient::register));
    }

    private static <T extends SplitShaftBlockEntity> void box(BlockEntityType<T> type, SplitBoxVisual.Modifier modifier) {
        AllBlockEntityRenders.visual(type, ctx -> new SplitBoxRenderer<T>(modifier), (ctx, be, pt) -> new SplitBoxVisual<>(ctx, be, pt, modifier));
        AllBlockEntityBehaviours.add(type, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
    }

    private static <T extends SplitShaftBlockEntity> void shaftPair(BlockEntityType<T> type) {
        AllBlockEntityRenders.visual(type, SplitShaftRenderer::new, SplitShaftVisual::new);
        AllBlockEntityBehaviours.add(type, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
    }

    private static void register() {
        box(CCBlockEntityTypes.PARALLEL_GEARBOX.get(), (state, face, src) -> ParallelGearboxBlockEntity.getRotationSpeedModifier(face, src));
        box(CCBlockEntityTypes.SIX_WAY_GEARBOX.get(), SixWayGearboxBlockEntity::getRotationSpeedModifier);
        box(CCBlockEntityTypes.BRASS_GEARBOX.get(), (state, face, src) -> BrassGearboxBlockEntity.getRotationSpeedModifier(face, src, state));
        shaftPair(CCBlockEntityTypes.INVERTED_CLUTCH.get());
        shaftPair(CCBlockEntityTypes.INVERTED_GEARSHIFT.get());
        shaftPair(CCBlockEntityTypes.BRAKE.get());
        var sp = CCBlockEntityTypes.SHEAR_PIN.get();
        AllBlockEntityRenders.visual(sp, AxisShaftRenderer<org.diaithir.connected.content.shearpin.ShearPinBlockEntity>::new, com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual.of(com.zurrtum.create.client.AllPartialModels.SHAFT));
        AllBlockEntityBehaviours.add(sp, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        var ov = CCBlockEntityTypes.OVERSTRESS_CLUTCH.get();
        shaftPair(ov);
        AllBlockEntityBehaviours.add(ov, be -> new ScrollValueBehaviour<>(Component.translatable("create_connected.overstress_clutch.uncouple_delay"), be,
            new CenteredSideValueBoxTransform((state, d) -> state.getValue(org.diaithir.connected.content.overstressclutch.OverstressClutchBlock.AXIS) != d.getAxis())));
        var fw = CCBlockEntityTypes.FREEWHEEL_CLUTCH.get();
        shaftPair(fw);
        AllBlockEntityBehaviours.add(fw, be -> new RotationDirectionScrollBehaviour(be, Component.translatable("create.contraptions.windmill.rotation_direction"), new ClutchValueBox()));
        var ce = CCBlockEntityTypes.CENTRIFUGAL_CLUTCH.get();
        shaftPair(ce);
        AllBlockEntityBehaviours.add(ce, be -> new ScrollValueBehaviour<>(Component.translatable("create_connected.centrifugal_clutch.speed_threshold"), be, new ClutchValueBox()));
    }
}
