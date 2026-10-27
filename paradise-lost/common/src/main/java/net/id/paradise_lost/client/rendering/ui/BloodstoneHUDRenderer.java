package net.id.paradise_lost.client.rendering.ui;

import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.api.MoaAPI;
import net.id.paradise_lost.entity.passive.moa.MoaAttributes;
import net.id.paradise_lost.item.tool.bloodstone.*;
import net.id.paradise_lost.item.ParadiseLostDataComponentTypes;
import net.id.paradise_lost.registry.MoaRaceRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class BloodstoneHUDRenderer {
    public static void render(GuiGraphicsExtractor context, DeltaTracker deltaTracker) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof BloodstoneItem) {
            var hasBloodstoneData = stack.getComponents().stream().anyMatch((c) -> c.type() == ParadiseLostDataComponentTypes.MOA_GENES || c.type() == ParadiseLostDataComponentTypes.BLOODSTONE);
            if (hasBloodstoneData) {
                Minecraft client = Minecraft.getInstance();
                BloodstoneCapturedData capturedData = BloodstoneCapturedData.fromComponents(stack);
                if (client.screen == null && isLookingAtMatchingEntity(client, capturedData) || doUUIDMatch(player, capturedData)) {
                    var matrixStack = context.pose();
                    matrixStack.pushMatrix();
                    matrixStack.translate(context.guiWidth() / 2f, context.guiHeight() / 2f);
                    if (stack.getItem() instanceof CherineBloodstoneItem) {
                        renderCherine(context, client, capturedData);
                    } else if (stack.getItem() instanceof OlviteBloodstoneItem) {
                        renderOlvite(context, client, capturedData);
                    } else if (stack.getItem() instanceof SurtrumBloodstoneItem) {
                        renderSurtrum(context, client, capturedData);
                    }
                    matrixStack.popMatrix();
                }
            }
        }
    }

    private static boolean isLookingAtMatchingEntity(Minecraft client, BloodstoneCapturedData capturedData) {
        if (client.hitResult == null
                || client.hitResult.getType() != HitResult.Type.ENTITY
                || !(((EntityHitResult) client.hitResult).getEntity() instanceof LivingEntity)) {
            return false;
        }

        if (((EntityHitResult) client.hitResult).getEntity() instanceof LivingEntity entity) {
            return doUUIDMatch(entity, capturedData);
        }

        return false;
    }

    private static boolean doUUIDMatch(LivingEntity entity, BloodstoneCapturedData capturedData) {
        return capturedData.bloodstoneComponent.uuid().equals(entity.getUUID());
    }

    private static void renderCherine(GuiGraphicsExtractor context, Minecraft client, BloodstoneCapturedData bloodstoneCapturedData) {
                renderRing(context, 0, 0);
        var component = bloodstoneCapturedData.bloodstoneComponent;
        renderText(context, client, component.name(), 0, -80);
        renderIconWText(context, client, effectIcon(MobEffects.REGENERATION), Component.literal(component.health()), 0, 80);
        renderIconWText(context, client, effectIcon(MobEffects.RESISTANCE), Component.literal(component.defense()), -80, 0);
        renderIconWText(context, client, effectIcon(MobEffects.ABSORPTION), Component.literal(component.toughness()), 80, 0);
    }

    private static void renderOlvite(GuiGraphicsExtractor context, Minecraft client, BloodstoneCapturedData bloodstoneCapturedData) {
                TextureAtlasSprite affinitySprite = context.getSprite(new SpriteId(TextureAtlas.LOCATION_BLOCKS, ModConstants.locate("item/icons/affinity")));
        TextureAtlasSprite raceSprite = context.getSprite(new SpriteId(TextureAtlas.LOCATION_BLOCKS, ModConstants.locate("item/icons/race")));

        renderRing(context, 0, 0);
        renderText(context, client, bloodstoneCapturedData.bloodstoneComponent.name(), 0, -80);

        if (bloodstoneCapturedData.moaGeneComponent != null) {
            renderIconWText(context, client, affinitySprite, Component.translatable(bloodstoneCapturedData.moaGeneComponent.affinity()), 76, -25);
            renderIconWText(context, client, effectIcon(MobEffects.INVISIBILITY), Component.literal(bloodstoneCapturedData.bloodstoneComponent.owner()), 47, 65);
            renderIconWText(context, client, effectIcon(MobEffects.HUNGER), Component.literal(String.format("%.1f", bloodstoneCapturedData.moaGeneComponent.hunger()) + "/" + 100.0), -47, 65);
            var raceId = bloodstoneCapturedData.moaGeneComponent.race();
            var race = client.level != null
                    ? MoaAPI.getRace(client.level, raceId)
                    : MoaRaceRegistry.FALLBACK_VALUE;
            renderIconWText(context, client, raceSprite, Component.translatable(race.translationKey(raceId)), -76, -25);
        }
    }

    private static void renderSurtrum(GuiGraphicsExtractor context, Minecraft client, BloodstoneCapturedData bloodstoneCapturedData) {
        renderRing(context, 0, 0);
        renderText(context, client, bloodstoneCapturedData.bloodstoneComponent.name(), 0, -80);
        if (bloodstoneCapturedData.moaGeneComponent != null) {
            renderText(context, client, Component.translatable("moa.attribute.ground_speed").append(": ").append(bloodstoneCapturedData.getRatingWithColor(MoaAttributes.GROUND_SPEED.getRatingTierTranslationKey(bloodstoneCapturedData.moaGeneComponent.attributes().groundSpeed()))), 63, -50);
            renderText(context, client, Component.translatable("moa.attribute.gliding_speed").append(": ").append(bloodstoneCapturedData.getRatingWithColor(MoaAttributes.GLIDING_SPEED.getRatingTierTranslationKey(bloodstoneCapturedData.moaGeneComponent.attributes().glidingSpeed()))), 80, 0);
            renderText(context, client, Component.translatable("moa.attribute.gliding_decay").append(": ").append(bloodstoneCapturedData.getRatingWithColor(MoaAttributes.GLIDING_DECAY.getRatingTierTranslationKey(bloodstoneCapturedData.moaGeneComponent.attributes().glidingDecay()))), 63, 50);
            renderText(context, client, Component.translatable("moa.attribute.jumping_strength").append(": ").append(bloodstoneCapturedData.getRatingWithColor(MoaAttributes.JUMPING_STRENGTH.getRatingTierTranslationKey(bloodstoneCapturedData.moaGeneComponent.attributes().jumpStrength()))), -63, -50);
            renderText(context, client, Component.translatable("moa.attribute.drop_multiplier").append(": ").append(bloodstoneCapturedData.getRatingWithColor(MoaAttributes.DROP_MULTIPLIER.getRatingTierTranslationKey(bloodstoneCapturedData.moaGeneComponent.attributes().dropMultiplier()))), -80, 0);
            renderText(context, client, Component.translatable("moa.attribute.max_health").append(": ").append(bloodstoneCapturedData.getRatingWithColor(MoaAttributes.MAX_HEALTH.getRatingTierTranslationKey(bloodstoneCapturedData.moaGeneComponent.attributes().maxHealth()))), -63, 50);
        }
    }

    private static void renderRing(GuiGraphicsExtractor context, int offsetX, int offsetY) {
        context.blit(RenderPipelines.GUI_TEXTURED, ModConstants.locate("textures/hud/bloodstone/bloodstone_ring.png"), offsetX - 75, offsetY - 75, 0, 0, 150, 150, 150, 150);
    }

    private static Identifier effectIcon(Holder<MobEffect> effect) {
        return Gui.getMobEffectSprite(effect);
    }

    private static void renderIconWText(GuiGraphicsExtractor context, Minecraft client, Identifier guiSprite, Component text, int offsetX, int offsetY) {
        renderIconWText(context, client, 18, (x, y) -> context.blitSprite(RenderPipelines.GUI_TEXTURED, guiSprite, x, y, 18, 18), text, offsetX, offsetY);
    }

    private static void renderIconWText(GuiGraphicsExtractor context, Minecraft client, TextureAtlasSprite sprite, Component text, int offsetX, int offsetY) {
        renderIconWText(context, client, sprite.contents().width(), (x, y) -> context.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 18, 18), text, offsetX, offsetY);
    }

    private static void renderIconWText(GuiGraphicsExtractor context, Minecraft client, int iconWidth, java.util.function.BiConsumer<Integer, Integer> icon, Component text, int offsetX, int offsetY) {
        int totalWidth = iconWidth + 2 + client.font.width(text);
        int totalHeight = client.font.lineHeight / 2;

        int startX = offsetX;
        if (offsetX == 0) {
            startX = offsetX - (totalWidth / 2);
        } else if (offsetX < 0) {
            startX = (offsetX - totalWidth);
        }

        icon.accept(startX, offsetY - 9);
        context.text(client.font, text, startX + iconWidth + 2, offsetY - totalHeight, 0xFFE0E0E0);
    }

    private static void renderText(GuiGraphicsExtractor context, Minecraft client, Component text, int offsetX, int offsetY) {
        int totalWidth = client.font.width(text);
        int totalHeight = client.font.lineHeight / 2;

        int startX = offsetX;
        if (offsetX == 0) {
            startX = offsetX - (totalWidth / 2);
        } else if (offsetX < 0) {
            startX = (offsetX - totalWidth);
        }

        context.text(client.font, text, startX, offsetY - totalHeight, 0xFFE0E0E0);
    }

    private static Tuple<Integer, Integer> getCircularPosition(int radius, int itemNum, int totalItems) {
        if (totalItems < 5) {
            return switch (itemNum) {
                case 0 -> new Tuple<>(0, -80);
                case 1 -> new Tuple<>(80, 0);
                case 2 -> new Tuple<>(-80, 0);
                case 3 -> new Tuple<>(0, 80);
                default -> new Tuple<>(0, 0);
            };
        }
        double angle = ((2 * Math.PI) / totalItems) * itemNum;
        int y = (int) Math.round(Math.cos(angle) * radius);
        int x = (int) Math.round(Math.sin(angle) * radius);
        return new Tuple<>(x, -y);
    }
}
