package untamedwilds.client.render.compat;

// MC 26.2 deleted com.mojang.blaze3d.vertex.VertexMultiConsumer along with the public
// ItemFeatureRenderer#getFoilBuffer that was its last caller — the enchantment glint is drawn from
// the submit pipeline's own item path now, which this mod's ~130 hand-written renderers do not go
// through.
//
// The glint is a visible feature here (RenderToucan always asks for it on a golden toucan, and the
// tusklin gear / armour layers ask for it per stack), so rather than drop it this reproduces
// vanilla's deleted VertexMultiConsumer.Double: every vertex call is forwarded to both delegates,
// so the same geometry is emitted once into the glint render type and once into the base one.
//
// Only the eight abstract methods need forwarding — VertexConsumer's defaults (the 11-arg
// addVertex, setLight, setOverlay, the float setColor) are all written in terms of them, so they
// reach both delegates too.
//
// Below 26.2 this compilation unit is just a package declaration; every other node still has
// vanilla's own multi-consumer behind ItemRenderer/ItemFeatureRenderer#getFoilBuffer.
// (client/render/compat/** is excluded from the compile below 1.21.2.)
