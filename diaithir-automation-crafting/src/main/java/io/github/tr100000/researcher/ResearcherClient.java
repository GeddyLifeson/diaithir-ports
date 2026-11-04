package io.github.tr100000.researcher;

import io.github.tr100000.researcher.api.recipe.RecipeUnlockDisplayRegistry;
import io.github.tr100000.researcher.api.reward.ResearchClientRewardRegistry;
import io.github.tr100000.researcher.command.ResearcherClientCommand;
import io.github.tr100000.researcher.impl.recipe.CraftingRecipeUnlockDisplay;
import io.github.tr100000.researcher.impl.reward.ResearcherClientRewards;
import io.github.tr100000.researcher.screen.ResearchHud;
import io.github.tr100000.researcher.screen.ResearchScreen;
import io.github.tr100000.trutils.api.utils.RecipeViewerDelegate;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.TransmuteRecipe;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.NullMarked;
import org.lwjgl.glfw.GLFW;

@NullMarked
@Mod(value = Researcher.MODID, dist = Dist.CLIENT)
public class ResearcherClient {
    public static final KeyMapping.Category KEY_CATEGORY = new KeyMapping.Category(ModUtils.id("main"));
    public static final KeyMapping OPEN_RESEARCH_SCREEN_KEY = new KeyMapping(ModUtils.id("open_research_screen").toLanguageKey("key"), GLFW.GLFW_KEY_R, KEY_CATEGORY);

    public static final SoundEvent RESEARCH_FINISHED_SOUND = Researcher.RESEARCH_FINISHED_SOUND;

    public static RecipeViewerDelegate recipeViewerDelegate = RecipeViewerDelegate.NONE;

    public ResearcherClient(IEventBus modBus) {
        ResearcherTriggerHandlers.register();

        RecipeUnlockDisplayRegistry.register(ShapedRecipe.SERIALIZER, CraftingRecipeUnlockDisplay::createShaped);
        RecipeUnlockDisplayRegistry.register(ShapelessRecipe.SERIALIZER, CraftingRecipeUnlockDisplay::createShapeless);
        RecipeUnlockDisplayRegistry.register(TransmuteRecipe.SERIALIZER, CraftingRecipeUnlockDisplay::createTransmute);

        ResearchClientRewardRegistry.register(ResearcherRewardTypes.EXPERIENCE, ResearcherClientRewards::ofExperience);
        ResearchClientRewardRegistry.register(ResearcherRewardTypes.FIREWORKS, ResearcherClientRewards::ofFireworks);
        ResearchClientRewardRegistry.register(ResearcherRewardTypes.LOOT, ResearcherClientRewards::ofLoot);

        modBus.addListener((RegisterKeyMappingsEvent event) -> {
            event.registerCategory(KEY_CATEGORY);
            event.register(OPEN_RESEARCH_SCREEN_KEY);
        });
        modBus.addListener((RegisterGuiLayersEvent event) ->
                event.registerAbove(VanillaGuiLayers.CHAT, ResearchHud.LAYER_ID, ResearchHud::render));

        modBus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) ->
                event.enqueueWork(() -> net.minecraft.core.registries.BuiltInRegistries.ITEM.forEach(io.github.tr100000.researcher.api.trigger.util.EntityPredicateHelper::onItemRegistered)));

        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent event) ->
                ResearcherClientCommand.register(event.getDispatcher(), event.getBuildContext()));

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            Minecraft client = Minecraft.getInstance();
            while (OPEN_RESEARCH_SCREEN_KEY.consumeClick()) {
                if (client.screen instanceof ResearchScreen researchScreen) {
                    client.setScreen(researchScreen.parent);
                }
                else {
                    client.setScreen(new ResearchScreen(client.screen));
                }
            }
        });

        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> {
            ResearchScreen.setSelected(null);
            ClientSyncedRecipes.clear();
        });
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.RecipesReceivedEvent event) ->
                ClientSyncedRecipes.set(event.getRecipeMap()));
        modBus.addListener((net.neoforged.fml.event.config.ModConfigEvent.Reloading event) -> {
            var connection = Minecraft.getInstance().getConnection();
            if (connection != null) {
                connection.researcher$getClientTracker().pinAvailableResearches();
            }
        });
    }
}
