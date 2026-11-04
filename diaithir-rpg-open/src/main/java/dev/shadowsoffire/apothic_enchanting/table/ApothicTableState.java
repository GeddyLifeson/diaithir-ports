package dev.shadowsoffire.apothic_enchanting.table;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;

/**
 * Extension interface for {@link EnchantTableRenderState} to hold a custom book texture.
 */
public interface ApothicTableState {

    void apoth_setBookTexture(@Nullable SpriteId sprite);

    @Nullable
    SpriteId apoth_getBookTexture();

}
