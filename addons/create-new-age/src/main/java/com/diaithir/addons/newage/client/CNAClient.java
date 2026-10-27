package com.diaithir.addons.newage.client;

import com.diaithir.addons.newage.CNABlockEntityTypes;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.client.AllBlockEntityRenders;
import com.zurrtum.create.client.content.kinetics.motor.MotorValueBox;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.KineticScrollValueBehaviour;
import com.zurrtum.create.client.content.kinetics.base.OrientedRotatingVisual;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.audio.KineticAudioBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.GeneratingKineticTooltipBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;

/** Client-side registrations. Called from the client entry point during client setup. */
public final class CNAClient {
    private CNAClient() {}

    public static void setup() {
        AllBlockEntityRenders.visual(CNABlockEntityTypes.ENERGISER.get(), EnergiserRenderer::new, SingleAxisRotatingVisual::shaft);
        AllBlockEntityRenders.visual(CNABlockEntityTypes.CARBON_BRUSHES.get(), CarbonBrushesRenderer::new, SingleAxisRotatingVisual.of(CNAPartialModels.COIL));
        AllBlockEntityRenders.visual(CNABlockEntityTypes.GENERATOR_COIL.get(), com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer::new,
            SingleAxisRotatingVisual.of(CNAPartialModels.GENERATOR_COIL));
        AllBlockEntityRenders.visual(CNABlockEntityTypes.STIRLING_ENGINE.get(), StirlingRenderer::new, SingleAxisRotatingVisual::shaft);
        for (var t : new BlockEntityType<?>[] {
            CNABlockEntityTypes.BASIC_MOTOR.get(), CNABlockEntityTypes.ADVANCED_MOTOR.get(), CNABlockEntityTypes.REINFORCED_MOTOR.get() }) {
            registerMotor(t);
        }
        AllBlockEntityRenders.render(CNABlockEntityTypes.ELECTRICAL_CONNECTOR.get(), WireRenderer::new);
        AllBlockEntityRenders.render(CNABlockEntityTypes.STREET_LIGHT.get(), WireRenderer::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerMotor(BlockEntityType t) {
        AllBlockEntityRenders.visual(t, MotorRenderer::new, OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF));
        AllBlockEntityBehaviours.add(t,
            (java.util.function.Function) (be -> new GeneratingKineticTooltipBehaviour((KineticBlockEntity) be)),
            (java.util.function.Function) (be -> new KineticAudioBehaviour((KineticBlockEntity) be)),
            (java.util.function.Function) (be -> new KineticScrollValueBehaviour(
                CreateLang.translateDirect("kinetics.creative_motor.rotation_speed"), (KineticBlockEntity) be, new MotorValueBox())));
    }
}
