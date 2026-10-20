package untamedwilds.client.render.compat;

// MC 26.2 deleted net.minecraft.client.renderer.MultiBufferSource outright — the last vanilla
// caller went away when 1.21.9 moved rendering to the extract/submit pipeline, and 26.2 finished
// the job by removing the interface and Minecraft#renderBuffers() with it.
//
// This mod still has ~130 renderers, ~37 layers and ~130 models written against the immediate-mode
// signature, and AMSubmitBuffers already stands between them and SubmitNodeCollector (see its
// header). All that is missing on 26.2 is the *type* those bodies name, so it is vendored here —
// the same two methods, in the compat package that already shadows EntityRenderer, MobRenderer,
// RenderLayer and friends — and the `!mc262-multibuffersource` replacement points the fully
// qualified vanilla name at it. That one rule covers all 109 imports and every inline FQN use.
//
// Nothing outside this mod ever sees one: every entry point that used to hand a legacy body a
// vanilla MultiBufferSource has, since 1.21.9, handed it an AMSubmitBuffers instead.
//
// Below 26.2 this compilation unit is just a package declaration, so the vanilla interface is what
// every node resolves. (client/render/compat/** is excluded from the compile below 1.21.2.)
