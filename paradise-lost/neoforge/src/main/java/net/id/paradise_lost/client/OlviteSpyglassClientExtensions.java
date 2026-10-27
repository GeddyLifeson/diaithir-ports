package net.id.paradise_lost.client;

import net.id.paradise_lost.ModConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * Supplies the olvite spyglass scope overlay (replaces the pre-26.1 Gui#renderSpyglassOverlay mixin).
 */
public final class OlviteSpyglassClientExtensions implements IClientItemExtensions {
    private static final Identifier SCOPE = ModConstants.locate("textures/hud/olvite_spyglass_scope.png");

    @Override
    public Identifier getScopeOverlayTexture(ItemStack stack) {
        return SCOPE;
    }
}
