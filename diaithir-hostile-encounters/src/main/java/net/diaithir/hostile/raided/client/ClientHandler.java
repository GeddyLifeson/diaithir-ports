package net.diaithir.hostile.raided.client;

import net.diaithir.hostile.raided.Raided;
import net.diaithir.hostile.raided.client.model.ElectromancerModel;
import net.diaithir.hostile.raided.client.model.IncineratorModel;
import net.diaithir.hostile.raided.client.model.InquisitorModel;
import net.diaithir.hostile.raided.client.model.NecromancerModel;
import net.diaithir.hostile.raided.client.model.SavagerModel;
import net.diaithir.hostile.raided.client.model.projectile.LightningProjectileModel;
import net.diaithir.hostile.raided.client.renderer.ElectromancerRenderer;
import net.diaithir.hostile.raided.client.renderer.IncineratorRenderer;
import net.diaithir.hostile.raided.client.renderer.InquisitorRenderer;
import net.diaithir.hostile.raided.client.renderer.LightningProjectileRenderer;
import net.diaithir.hostile.raided.client.renderer.NecromancerRenderer;
import net.diaithir.hostile.raided.client.renderer.SavagerRenderer;
import net.diaithir.hostile.raided.registry.RaidedRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ClientHandler {
	public static final ModelLayerLocation INQUISITOR = new ModelLayerLocation(Raided.modLoc("inquisitor"), "main");
	public static final ModelLayerLocation INCINERATOR = new ModelLayerLocation(Raided.modLoc("incinerator"), "main");
	public static final ModelLayerLocation SAVAGER = new ModelLayerLocation(Raided.modLoc("savager"), "main");
	public static final ModelLayerLocation NECROMANCER = new ModelLayerLocation(Raided.modLoc("necromancer"), "main");
	public static final ModelLayerLocation ELECTROMANCER = new ModelLayerLocation(Raided.modLoc("electromancer"), "main");

	public static final ModelLayerLocation LIGHTNING_PROJECTILE = new ModelLayerLocation(Raided.modLoc("lightning_projectile"), "main");

	public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(RaidedRegistry.INQUISITOR.getEntityType(), InquisitorRenderer::new);
		event.registerEntityRenderer(RaidedRegistry.INCINERATOR.getEntityType(), IncineratorRenderer::new);
		event.registerEntityRenderer(RaidedRegistry.INCINERATOR_FIREBALL.get(), (context) -> new ThrownItemRenderer<>(context, 2.0F, true));
		event.registerEntityRenderer(RaidedRegistry.SAVAGER.getEntityType(), SavagerRenderer::new);
		event.registerEntityRenderer(RaidedRegistry.NECROMANCER.getEntityType(), NecromancerRenderer::new);
		event.registerEntityRenderer(RaidedRegistry.ELECTROMANCER.getEntityType(), ElectromancerRenderer::new);

		event.registerEntityRenderer(RaidedRegistry.LIGHTNING_PROJECTILE.get(), LightningProjectileRenderer::new);
	}

	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(INQUISITOR, InquisitorModel::createBodyLayer);
		event.registerLayerDefinition(INCINERATOR, IncineratorModel::createBodyLayer);
		event.registerLayerDefinition(SAVAGER, SavagerModel::createBodyLayer);
		event.registerLayerDefinition(NECROMANCER, NecromancerModel::createBodyLayer);
		event.registerLayerDefinition(ELECTROMANCER, ElectromancerModel::createBodyLayer);
		event.registerLayerDefinition(LIGHTNING_PROJECTILE, LightningProjectileModel::createBodyLayer);
	}
}
