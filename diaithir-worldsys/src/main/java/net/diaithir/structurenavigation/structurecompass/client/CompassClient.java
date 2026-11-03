// Ported from Structure Compass (MIT) https://github.com/Mrbysco/StructureCompass
// Modifications: Merged ClientHandler and KeyHandler; uses ClientTickEvent and ItemTooltipEvent instead of input/player-tick/item hover hooks.
package net.diaithir.structurenavigation.structurecompass.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.diaithir.structurenavigation.structurecompass.CompassIds;
import net.diaithir.structurenavigation.structurecompass.client.property.StructureCompassAngle;
import net.diaithir.structurenavigation.structurecompass.client.screen.CompassScreen;
import net.diaithir.structurenavigation.structurecompass.component.StructureInfo;
import net.diaithir.structurenavigation.structurecompass.registry.CompassComponents;
import net.diaithir.structurenavigation.structurecompass.registry.CompassItems;
import net.diaithir.structurenavigation.structurecompass.util.StructureUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;

public final class CompassClient {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(CompassIds.modLoc("structure_compass"));
    public static final KeyMapping KEY_TOGGLE = new KeyMapping(
            "key." + CompassIds.MOD_ID + ".structure_compass_hide",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY);

    private static boolean hidden = false;

    private CompassClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterRangeSelectItemModelPropertyEvent.class,
                e -> e.register(CompassIds.modLoc("structure_compass_angle"), StructureCompassAngle.MAP_CODEC));
        modBus.addListener(RegisterKeyMappingsEvent.class, e -> {
            e.registerCategory(CATEGORY);
            e.register(KEY_TOGGLE);
        });
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, CompassClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, CompassClient::onTooltip);
    }

    public static void openStructureScreen(InteractionHand hand, ItemStack stack, List<Identifier> allStructures, List<Identifier> allTags) {
        Minecraft.getInstance().setScreen(new CompassScreen(hand, stack, allStructures, allTags));
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        while (KEY_TOGGLE.consumeClick()) {
            if (player != null) {
                hidden = !hidden;
                player.sendSystemMessage(Component.translatable("structurecompass.locate.toggled", hidden ? "off" : "on"));
            }
        }

        if (player == null || hidden || player.tickCount % 10 != 0) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(CompassItems.STRUCTURE_COMPASS.get())) {
            stack = player.getOffhandItem();
            if (!stack.is(CompassItems.STRUCTURE_COMPASS.get())) {
                return;
            }
        }
        StructureInfo info = stack.get(CompassComponents.STRUCTURE_INFO.get());
        if (info == null) {
            return;
        }
        GlobalPos globalPos = info.globalPos();
        if (player.level().dimension().identifier().equals(globalPos.dimension().identifier())) {
            int distance = player.blockPosition().distManhattan(globalPos.pos());
            player.sendOverlayMessage(Component.translatable("structurecompass.locate.distance", distance).withStyle(ChatFormatting.YELLOW));
        }
    }

    private static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(CompassItems.STRUCTURE_COMPASS.get())) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        Identifier structureLocation = stack.get(CompassComponents.STRUCTURE.get());
        if (structureLocation == null) {
            tooltip.add(Component.translatable("structurecompass.structure.unset.tooltip").withStyle(ChatFormatting.GOLD));
            return;
        }
        boolean isTag = stack.getOrDefault(CompassComponents.IS_TAG.get(), false);
        Component structureName = StructureUtil.getStructureName(structureLocation, isTag);
        StructureInfo info = stack.get(CompassComponents.STRUCTURE_INFO.get());
        if (info != null) {
            Player player = event.getEntity();
            if (player != null && player.level().dimension().identifier().equals(info.globalPos().dimension().identifier())) {
                tooltip.add(Component.translatable("structurecompass.structure.found.tooltip", structureName).withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.translatable("structurecompass.structure.wrong_dimension.tooltip", structureName).withStyle(ChatFormatting.RED));
            }
        } else {
            tooltip.add(Component.translatable("structurecompass.structure.failed.tooltip", structureName).withStyle(ChatFormatting.RED));
        }
    }
}
