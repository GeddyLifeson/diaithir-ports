package com.diaithir.addons.oreexcavation;

import java.util.function.Supplier;

import org.slf4j.Logger;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;

import com.diaithir.addons.oreexcavation.components.OreVeinAtlasDataComponent;
import com.diaithir.addons.oreexcavation.network.NetworkHandler;
import com.diaithir.addons.oreexcavation.recipe.DrillingRecipe;
import com.diaithir.addons.oreexcavation.recipe.ExtractorRecipe;
import com.diaithir.addons.oreexcavation.recipe.VeinRecipe;

@Mod(CreateOreExcavation.MODID)
public class CreateOreExcavation {
	public static final String MODID = "createoreexcavation";
	public static final Logger LOGGER = LogUtils.getLogger();

	public static boolean journeyMap, kubeJSCreate;

	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
	private static final DeferredRegister<RecipeType<?>> TYPE_REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);
	private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MODID);
	private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

	public static final RecipeTypeGroup<DrillingRecipe> DRILLING_RECIPES = recipe("drilling", DrillingRecipe.SERIALIZER);
	public static final RecipeTypeGroup<ExtractorRecipe> EXTRACTING_RECIPES = recipe("extracting", ExtractorRecipe.SERIALIZER);
	public static final RecipeTypeGroup<VeinRecipe> VEIN_RECIPES = recipe("vein", VeinRecipe.Serializer.SERIALIZER);

	public static final TagKey<Item> DRILL_TAG = TagKey.create(Registries.ITEM, Identifier.tryBuild(MODID, "drills"));

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<OreVeinAtlasDataComponent>> ORE_VEIN_ATLAS_DATA_COMPONENT = DATA_COMPONENTS.register("ore_vein_altas_data", () -> DataComponentType.<OreVeinAtlasDataComponent>builder().persistent(OreVeinAtlasDataComponent.CODEC).build());
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ORE_VEIN_FINDER_FILTERED_COMPONENT = DATA_COMPONENTS.register("ore_vein_finder_filtered", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).build());

	public static final Supplier<AttachmentType<OreDataAttachment>> ORE_DATA = ATTACHMENT_TYPES.register(
			"ore_vein", () -> AttachmentType.serializable(OreDataAttachment::new).build());

	public CreateOreExcavation(ModContainer mc, IEventBus bus) {
		bus.addListener(this::setup);
		bus.addListener(this::doClientStuff);
		bus.addListener(this::enqueueIMC);
		bus.addListener(this::processIMC);

		mc.registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
		mc.registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
		bus.register(ForgeConfig.class);
		bus.register(NetworkHandler.class);

		journeyMap = ModList.get().isLoaded("journeymap");
		kubeJSCreate = ModList.get().isLoaded("kubejs_create");

		// Register ourselves for server and other game events we are interested in
		NeoForge.EVENT_BUS.register(this);

		Registration.register(bus);

		RECIPE_SERIALIZER.register(bus);
		TYPE_REGISTER.register(bus);
		ATTACHMENT_TYPES.register(bus);
		DATA_COMPONENTS.register(bus);
	}

	private static <T extends Recipe<?>> RecipeTypeGroup<T> recipe(String name, RecipeSerializer<T> serializer) {
		RecipeTypeGroup<T> rg = new RecipeTypeGroup<>(Identifier.tryBuild(MODID, name));
		rg.recipeType = TYPE_REGISTER.register(name, () -> RecipeType.simple(Identifier.tryBuild(MODID, name)));
		rg.serializer = RECIPE_SERIALIZER.register(name, () -> serializer);
		return rg;
	}

	public static class RecipeTypeGroup<T extends Recipe<?>> {
		private DeferredHolder<RecipeSerializer<?>, RecipeSerializer<T>> serializer;
		private DeferredHolder<RecipeType<?>, RecipeType<T>> recipeType;
		private Identifier id;

		public RecipeTypeGroup(Identifier id) {
			this.id = id;
		}

		public RecipeType<T> getRecipeType() {
			return recipeType.get();
		}

		public RecipeSerializer<T> getSerializer() {
			return serializer.get();
		}

		public Identifier getId() {
			return id;
		}
	}

	private void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("Create Ore Excavation starting");
		Registration.postRegister();
		event.enqueueWork(CreateOreExcavation::registerBehaviours);
		//BlockStressValues.IMPACTS.register(Registration.KINETIC_INPUT.get(), AllConfigs.server().kinetics.stressValues.getImpact(null));
		COECommand.init();
	}

	private static void registerBehaviours() {
		com.zurrtum.create.api.behaviour.BlockEntityBehaviour.add(Registration.IO_TILE.get(),
				be -> new com.zurrtum.create.foundation.blockEntity.behaviour.CachedDirectionInventoryBehaviour<>(be, (b, side) -> b.itemInv(side)));
		com.zurrtum.create.api.behaviour.BlockEntityBehaviour.add(Registration.IO_TILE.get(),
				be -> new com.zurrtum.create.foundation.blockEntity.behaviour.CachedFluidInventoryBehaviour<>(be, b -> b.fluidInv()));
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		
	}

	private void enqueueIMC(final InterModEnqueueEvent event) {
	}

	private void processIMC(final InterModProcessEvent event) {
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent evt) {
		COECommand.register(evt);
	}

	@SubscribeEvent
	public void syncRecipes(net.neoforged.neoforge.event.OnDatapackSyncEvent evt) {
		evt.sendRecipes(DRILLING_RECIPES.getRecipeType(), EXTRACTING_RECIPES.getRecipeType(), VEIN_RECIPES.getRecipeType());
	}

	@SubscribeEvent
	public void reloadEvent(TagsUpdatedEvent evt) {
		OreVeinGenerator.invalidate();
	}

	public static boolean isModLoaded(String id) {
		return ModList.get().isLoaded(id);
	}

}
