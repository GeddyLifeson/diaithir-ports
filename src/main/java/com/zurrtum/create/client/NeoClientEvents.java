package com.zurrtum.create.client;

import com.zurrtum.create.client.catnip.gui.render.*;
import com.zurrtum.create.client.catnip.render.CustomRenderPipeline;
import com.zurrtum.create.client.foundation.gui.render.*;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderState;
import com.zurrtum.create.client.ponder.foundation.render.SceneRenderer;
import com.zurrtum.create.client.ponder.foundation.render.TitleTextRenderState;
import com.zurrtum.create.client.ponder.foundation.render.TitleTextRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;

public class NeoClientEvents {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void registerPip(RegisterPictureInPictureRenderersEvent e) {
        e.register((Class) ItemTransformRenderState.class, b -> (PictureInPictureRenderer) new ItemTransformElementRenderer(b));
        e.register((Class) BlockTransformRenderState.class, b -> (PictureInPictureRenderer) new BlockTransformElementRenderer(b));
        e.register((Class) EntityBlockRenderState.class, b -> (PictureInPictureRenderer) new EntityBlockRenderer(b));
        e.register((Class) PartialRenderState.class, b -> (PictureInPictureRenderer) new PartialElementRenderer(b));
        e.register((Class) BlazeBurnerRenderState.class, b -> (PictureInPictureRenderer) new BlazeBurnerElementRenderer(b));
        e.register((Class) PressBasinRenderState.class, b -> (PictureInPictureRenderer) new PressBasinRenderer(b));
        e.register((Class) PressRenderState.class, b -> (PictureInPictureRenderer) new PressRenderer(b));
        e.register((Class) MixingBasinRenderState.class, b -> (PictureInPictureRenderer) new MixingBasinRenderer(b));
        e.register((Class) BasinBlazeBurnerRenderState.class, b -> (PictureInPictureRenderer) new BasinBlazeBurnerRenderer(b));
        e.register((Class) MillstoneRenderState.class, b -> (PictureInPictureRenderer) new MillstoneRenderer(b));
        e.register((Class) SawRenderState.class, b -> (PictureInPictureRenderer) new SawRenderer(b));
        e.register((Class) CrushWheelRenderState.class, b -> (PictureInPictureRenderer) new CrushWheelRenderer(b));
        e.register((Class) DeployerRenderState.class, b -> (PictureInPictureRenderer) new DeployerRenderer(b));
        e.register((Class) ManualBlockRenderState.class, b -> (PictureInPictureRenderer) new ManualBlockRenderer(b));
        e.register((Class) SpoutRenderState.class, b -> (PictureInPictureRenderer) new SpoutRenderer(b));
        e.register((Class) CrafterRenderState.class, b -> (PictureInPictureRenderer) new CrafterRenderer(b));
        e.register((Class) DrainRenderState.class, b -> (PictureInPictureRenderer) new DrainRenderer(b));
        e.register((Class) SandPaperRenderState.class, b -> (PictureInPictureRenderer) new SandPaperRenderer(b));
        e.register((Class) TitleTextRenderState.class, b -> (PictureInPictureRenderer) new TitleTextRenderer(b));
        e.register((Class) SceneRenderState.class, b -> (PictureInPictureRenderer) new SceneRenderer());
        e.register((Class) FanRenderState.class, b -> (PictureInPictureRenderer) new FanRenderer(b));
    }

    public static void registerReloadListeners(net.neoforged.neoforge.client.event.AddClientReloadListenersEvent e) {
        net.minecraft.resources.Identifier base = net.minecraft.resources.Identifier.fromNamespaceAndPath("create", "obj_loader");
        e.addListener(base, com.zurrtum.create.client.model.obj.ObjLoader.INSTANCE);
        e.addListener(net.minecraft.resources.Identifier.fromNamespaceAndPath("create", "client_resources"), Create.RESOURCE_RELOAD_LISTENER);
        e.addListener(net.minecraft.resources.Identifier.fromNamespaceAndPath("create", "train_hats"), com.zurrtum.create.client.content.trains.schedule.hat.TrainHatInfoReloadListener.LISTENER);
    }
}
