package cn.leolezury.eternalstarlight.neoforge.client.event;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.compat.ESRender;
import cn.leolezury.eternalstarlight.common.client.gui.tooltip.ClientGalacticQuiverTooltip;
import cn.leolezury.eternalstarlight.common.client.handler.ESClientHandler;
import cn.leolezury.eternalstarlight.common.client.handler.ESClientSetupHandler;
import cn.leolezury.eternalstarlight.common.client.model.ESArmorSets;
import cn.leolezury.eternalstarlight.common.client.model.armor.AlchemistArmorModel;
import cn.leolezury.eternalstarlight.common.client.model.armor.StarlitDiamondArmorModel;
import cn.leolezury.eternalstarlight.common.client.model.armor.ThermalSpringStoneArmorModel;
import cn.leolezury.eternalstarlight.common.client.model.armor.UnrealiumArmorModel;
import cn.leolezury.eternalstarlight.common.item.tooltip.GalacticQuiverTooltipComponent;
import cn.leolezury.eternalstarlight.common.registry.ESFluids;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import cn.leolezury.eternalstarlight.neoforge.client.ESNeoSkyboxRenderer;
import cn.leolezury.eternalstarlight.neoforge.registry.ESFluidTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.joml.Vector4f;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

@EventBusSubscriber(modid = EternalStarlight.ID, value = Dist.CLIENT)
public class ClientSetupEvents {
	@SubscribeEvent
	private static void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(ESClientSetupHandler::clientSetup);
	}

	@SubscribeEvent
	private static void onAddPackFinders(AddPackFindersEvent event) {
		if (event.getPackType() == PackType.CLIENT_RESOURCES) {
			event.addPackFinders(
				EternalStarlight.id("resourcepacks/starlight_3d_mace"),
				PackType.CLIENT_RESOURCES,
				Component.translatable("resource_pack." + EternalStarlight.ID + ".starlight_3d_mace"),
				PackSource.BUILT_IN,
				true,
				Pack.Position.TOP
			);
		}
	}

	@SubscribeEvent
	private static void onRegisterEnvironmentRenderers(RegisterCustomEnvironmentEffectRendererEvent event) {
		event.registerSkyboxRenderer(EternalStarlight.id("starlight"), new ESNeoSkyboxRenderer());
	}

	@SubscribeEvent
	private static void onRegisterBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
		ESClientSetupHandler.registerBlockColors(event::register);
	}

	@SubscribeEvent
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void onRegisterRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
		// gives the entity based accessory layers access to the entity behind a humanoid render state
		event.registerEntityModifier((Class) LivingEntityRenderer.class, (java.util.function.BiConsumer) (entity, state) -> {
			if (state instanceof HumanoidRenderState) {
				ESRender.bindEntity((net.minecraft.client.renderer.entity.state.EntityRenderState) state, (Entity) entity);
			}
		});
	}

	private static IClientItemExtensions armorExtension(Function<EquipmentSlot, Model> factory) {
		return new IClientItemExtensions() {
			private final Map<EquipmentSlot, Model> models = new EnumMap<>(EquipmentSlot.class);

			@Override
			public Model getHumanoidArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
				var equippable = itemStack.get(DataComponents.EQUIPPABLE);
				if (equippable == null) {
					return original;
				}
				return models.computeIfAbsent(equippable.slot(), factory);
			}
		};
	}

	private static Model slotModel(HumanoidModel<HumanoidRenderState> model, EquipmentSlot slot) {
		ESArmorSets.applySlotVisibility(model, slot);
		return model;
	}

	private static ModelPartBaker baker() {
		return location -> Minecraft.getInstance().getEntityModels().bakeLayer(location);
	}

	private interface ModelPartBaker {
		net.minecraft.client.model.geom.ModelPart bake(ModelLayerLocation location);
	}

	@SubscribeEvent
	private static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
		IClientItemExtensions alchemistArmor = armorExtension(slot -> slotModel(new AlchemistArmorModel(baker().bake(AlchemistArmorModel.LAYER_LOCATION)), slot));
		event.registerItem(alchemistArmor, ESItems.ALCHEMIST_MASK.get(), ESItems.ALCHEMIST_ROBE.get());
		IClientItemExtensions thermalSpringstoneArmor = armorExtension(slot -> slotModel(new ThermalSpringStoneArmorModel(baker().bake(slot == EquipmentSlot.LEGS ? ThermalSpringStoneArmorModel.INNER_LOCATION : ThermalSpringStoneArmorModel.OUTER_LOCATION)), slot));
		event.registerItem(thermalSpringstoneArmor, ESItems.THERMAL_SPRINGSTONE_HELMET.get(), ESItems.THERMAL_SPRINGSTONE_CHESTPLATE.get(), ESItems.THERMAL_SPRINGSTONE_LEGGINGS.get(), ESItems.THERMAL_SPRINGSTONE_BOOTS.get());
		IClientItemExtensions starlitDiamondArmor = armorExtension(slot -> slotModel(new StarlitDiamondArmorModel(baker().bake(slot == EquipmentSlot.LEGS ? StarlitDiamondArmorModel.INNER_LOCATION : StarlitDiamondArmorModel.OUTER_LOCATION)), slot));
		event.registerItem(starlitDiamondArmor, ESItems.STARLIT_DIAMOND_HELMET.get(), ESItems.STARLIT_DIAMOND_CHESTPLATE.get(), ESItems.STARLIT_DIAMOND_LEGGINGS.get(), ESItems.STARLIT_DIAMOND_BOOTS.get());
		IClientItemExtensions unrealiumArmor = armorExtension(slot -> slotModel(new UnrealiumArmorModel(baker().bake(slot == EquipmentSlot.LEGS ? UnrealiumArmorModel.INNER_LOCATION : UnrealiumArmorModel.OUTER_LOCATION)), slot));
		event.registerItem(unrealiumArmor, ESItems.UNREALIUM_HELMET.get(), ESItems.UNREALIUM_CHESTPLATE.get(), ESItems.UNREALIUM_LEGGINGS.get(), ESItems.UNREALIUM_BOOTS.get());

		event.registerFluidType(new IClientFluidTypeExtensions() {
			@Override
			public void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
				fluidFogColor.set(232 / 255F, 255 / 255F, 222 / 255F, fluidFogColor.w);
			}

			@Override
			public void modifyFogRender(Camera camera, FogEnvironment environment, float renderDistance, float partialTick, FogData fogData) {
				fogData.environmentalStart = 0.0F;
				fogData.environmentalEnd = 3.0F;
			}
		}, ESFluidTypes.ETHER.get());
	}

	@SubscribeEvent
	private static void onRegisterFluidModels(RegisterFluidModelsEvent event) {
		FluidModel.Unbaked ether = new FluidModel.Unbaked(
			new Material(EternalStarlight.id("block/ether")),
			new Material(EternalStarlight.id("block/ether_flow")),
			null,
			new FluidTintSource() {
				@Override
				public int color(FluidState state) {
					return ESClientHandler.getEtherTint(null, null);
				}

				@Override
				public int colorInWorld(FluidState state, BlockState blockState, BlockAndTintGetter getter, BlockPos pos) {
					return ESClientHandler.getEtherTint(getter, pos);
				}
			}
		);
		event.register(ether, ESFluids.ETHER_STILL.get(), ESFluids.ETHER_FLOWING.get());
	}

	@SubscribeEvent
	private static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
		ESClientSetupHandler.registerParticleProviders(event::registerSpriteSet);
	}

	@SubscribeEvent
	private static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		ESClientSetupHandler.registerEntityRenderers(event::registerEntityRenderer);
		ESClientSetupHandler.registerBlockEntityRenderers(event::registerBlockEntityRenderer);
	}

	@SubscribeEvent
	private static void onRegisterSkullModels(EntityRenderersEvent.CreateSkullModels event) {
		ESClientSetupHandler.registerSkullModels(event::registerSkullModel);
	}

	@SubscribeEvent
	private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		ESClientSetupHandler.registerLayers(event::registerLayerDefinition);
	}

	@SubscribeEvent
	private static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
		ESClientSetupHandler.registerMenuScreens(event::register);
	}

	@SubscribeEvent
	private static void onAddLayers(EntityRenderersEvent.AddLayers event) {
		for (EntityType<? extends Entity> entityType : event.getEntityTypes()) {
			if (event.getRenderer(entityType) instanceof LivingEntityRenderer<?, ?, ?> livingEntityRenderer) {
				ESClientSetupHandler.onRenderLayerAttachment(entityType, livingEntityRenderer, event.getContext());
			}
		}
		for (PlayerModelType model : event.getSkins()) {
			if (event.getPlayerRenderer(model) instanceof LivingEntityRenderer<?, ?, ?> livingEntityRenderer) {
				ESClientSetupHandler.onRenderLayerAttachment(EntityType.PLAYER, livingEntityRenderer, event.getContext());
			}
		}
	}

	@SubscribeEvent
	private static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, EternalStarlight.id("offhand_attack_indicator"), (graphics, deltaTracker) -> ESClientHandler.renderOffhandAttackIndicator(graphics));
		event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, EternalStarlight.id("ether_erosion"), (graphics, deltaTracker) -> ESClientHandler.renderEtherErosion(graphics));
		event.registerAbove(VanillaGuiLayers.ARMOR_LEVEL, EternalStarlight.id("ether_armor"), (graphics, deltaTracker) -> {
			if (Minecraft.getInstance().gameMode != null && Minecraft.getInstance().gameMode.canHurtPlayer()) {
				ESClientHandler.renderEtherArmor(graphics, graphics.guiWidth(), graphics.guiHeight());
			}
		});
		event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, EternalStarlight.id("dream_catcher"), (graphics, deltaTracker) -> ESClientHandler.renderDreamCatcher(graphics));
		event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, EternalStarlight.id("carved_lunaris_cactus_fruit_blur"), (graphics, deltaTracker) -> ESClientHandler.renderCarvedLunarisCactusFruitBlur(graphics));
		event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, EternalStarlight.id("portal"), (graphics, deltaTracker) -> ESClientHandler.renderPortalOverlay(graphics));
	}

	@SubscribeEvent
	private static void onAddReloadListener(AddClientReloadListenersEvent event) {
		int[] counter = {0};
		ESClientSetupHandler.addClientReloadListeners(listener -> event.addListener(EternalStarlight.id("client_reload_" + counter[0]++), listener));
	}

	@SubscribeEvent
	private static void onRegisterClientTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(GalacticQuiverTooltipComponent.class, component -> new ClientGalacticQuiverTooltip(component.contents()));
	}
}
