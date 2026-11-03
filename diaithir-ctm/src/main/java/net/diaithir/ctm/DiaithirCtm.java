package net.diaithir.ctm;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@Mod(value = DiaithirCtm.ID, dist = Dist.CLIENT)
public final class DiaithirCtm {
    public static final String ID = "diaithirctm";
    public static final TagKey<Block> CONNECT = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ID, "connect"));

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue ENABLED = B.comment("Draw connected textures for blocks in the diaithirctm:connect tag. Off until checked in a client.").define("enabled", false);
    private static final ModConfigSpec SPEC = B.build();

    public DiaithirCtm(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC);
        bus.addListener(DiaithirCtm::bake);
    }

    private static void bake(ModelEvent.ModifyBakingResult e) {
        if (!ENABLED.get()) {
            return;
        }
        Map<BlockState, BlockStateModel> models = e.getBakingResult().blockStateModels();
        Map<BlockState, BlockStateModel> swap = new HashMap<>();
        for (Map.Entry<BlockState, BlockStateModel> en : models.entrySet()) {
            if (en.getKey().is(CONNECT)) {
                swap.put(en.getKey(), new CtmModel(en.getValue()));
            }
        }
        models.putAll(swap);
    }
}
