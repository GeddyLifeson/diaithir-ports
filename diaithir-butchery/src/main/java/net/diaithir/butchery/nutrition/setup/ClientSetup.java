package net.diaithir.butchery.nutrition.setup;

import net.diaithir.butchery.nutrition.lunchbox.LunchBoxItemRenderer;
import net.diaithir.butchery.nutrition.lunchbox.LunchBoxScreen;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class ClientSetup {
    public static void init(final RegisterMenuScreensEvent event) {
        event.register(Registration.LUNCHBOX_MENU_TYPE.get(), LunchBoxScreen::new);
    }

    public static void addCreative(BuildCreativeModeTabContentsEvent event){
        if(event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS)
        {
            event.accept(Registration.LUNCHBOX_ITEM.get());
        }
    }

    @SuppressWarnings("unchecked")
    public static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(
            Identifier.fromNamespaceAndPath("nutritionalbalance", "lunchbox_renderer"),
            (MapCodec<? extends SpecialModelRenderer.Unbaked<?>>) (MapCodec<?>) LunchBoxItemRenderer.Unbaked.MAP_CODEC
        );
    }
}
