package com.diaithir.addons.oreexcavation.client;

import static net.minecraft.ChatFormatting.GRAY;

import java.util.List;

import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import com.zurrtum.create.content.kinetics.base.IRotate.StressImpact;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;

import com.diaithir.addons.oreexcavation.CreateOreExcavation;
import com.diaithir.addons.oreexcavation.Registration;

@EventBusSubscriber(value = Dist.CLIENT, modid = CreateOreExcavation.MODID)
public class ClientEventHandler {

	@SubscribeEvent
	public static void addToItemTooltip(ItemTooltipEvent event) {
		if (event.getEntity() == null)
			return;

		ItemStack stack = event.getItemStack();
		if(stack.getItem() == Registration.DRILL_BLOCK.get().asItem() || stack.getItem() == Registration.EXTRACTOR_BLOCK.get().asItem()) {
			appendVariableStress(event.getToolTip());
		}
	}

	public static void appendVariableStress(List<Component> tooltip) {
		boolean hasGoggles = GogglesItem.isWearingGoggles(Minecraft.getInstance().player);
		boolean hasStressImpact = StressImpact.isEnabled();
		LangBuilder rpmUnit = CreateLang.translate("generic.unit.rpm");

		if (hasStressImpact) {
			CreateLang.translate("tooltip.stressImpact")
			.style(GRAY)
			.addTo(tooltip);

			StressImpact impactId = StressImpact.HIGH;
			LangBuilder builder = CreateLang.builder()
					.add(CreateLang.text(TooltipHelper.makeProgressBar(3, impactId.ordinal() + 1))
							.style(impactId.getAbsoluteColor()));

			if (hasGoggles) {
				builder.add(CreateLang.text("1-?"))
				.text("x ")
				.add(rpmUnit)
				.add(Component.literal(" ").append(Component.translatable("tooltip.coe.variableImpact")))
				.addTo(tooltip);
			} else
				builder.translate("tooltip.stressImpact." + Lang.asId(impactId.name()))
				.addTo(tooltip);
		}
	}
}
