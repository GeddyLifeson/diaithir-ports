package net.diaithir.ctm;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Client only. */
final class ClientSetup {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    static final ModConfigSpec.BooleanValue ENABLED = B.comment("Draw connected textures for blocks in the diaithirctm:connect tag. Off until checked in a client; off shows each block's single plain tile.").define("enabled", false);
    private static final ModConfigSpec SPEC = B.build();

    private ClientSetup() {}

    static void init(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC);
        bus.addListener(ClientSetup::bake);
    }

    /** The sheet for block ns:path is the sprite ns:block/path_sheet; blocks without one are left alone. */
    private static void bake(ModelEvent.ModifyBakingResult e) {
        if (!ENABLED.get()) {
            return;
        }
        Map<BlockState, BlockStateModel> models = e.getBakingResult().blockStateModels();
        Map<BlockState, BlockStateModel> swap = new HashMap<>();
        for (Map.Entry<BlockState, BlockStateModel> en : models.entrySet()) {
            if (!en.getKey().is(DiaithirCtm.CONNECT)) {
                continue;
            }
            Identifier id = BuiltInRegistries.BLOCK.getKey(en.getKey().getBlock());
            Identifier sheetId = Identifier.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath() + "_sheet");
            TextureAtlasSprite sheet = e.getTextureGetter().apply(sheetId);
            if (sheet != null && sheet.contents().name().equals(sheetId)) {
                swap.put(en.getKey(), new CtmModel(en.getValue(), sheet));
            }
        }
        models.putAll(swap);
    }
}
