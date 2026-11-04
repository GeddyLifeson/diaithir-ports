package io.github.tr100000.researcher.screen;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import io.github.tr100000.researcher.Researcher;
import io.github.tr100000.researcher.ResearcherRegistries;
import io.github.tr100000.researcher.api.ResearchReward;
import io.github.tr100000.researcher.api.reward.ResearchClientReward;
import io.github.tr100000.researcher.api.reward.ResearchClientRewardRegistry;
import io.github.tr100000.trutils.api.gui.GuiHelper;
import io.github.tr100000.trutils.api.gui.Icon;
import io.github.tr100000.trutils.api.gui.IconRenderers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ResearchRewardWidget extends AbstractWidget {
    private static final Minecraft client = Minecraft.getInstance();

    public final ResearchReward reward;
    public final ResearchClientReward clientReward;
    public final Icon icon;
    public final List<ClientTooltipComponent> tooltip;
    public final boolean isError;

    public static ResearchRewardWidget create(int x, int y, ResearchReward reward) {
        ResearchClientReward clientReward = ResearchClientRewardRegistry.get(reward);
        if (clientReward != null) {
            return new ResearchRewardWidget(x, y, reward, clientReward, false);
        }
        else {
            Researcher.LOGGER.warn("Couldn't display reward of type {}", ResearcherRegistries.RESEARCH_REWARD_TYPE.getId(reward.getType()));
            return new ResearchRewardWidget(x, y, reward, ResearchClientReward.ERROR, true);
        }
    }

    private ResearchRewardWidget(int x, int y, ResearchReward reward, ResearchClientReward clientReward, boolean isError) {
        super(x, y, 16, 16, Component.empty());
        this.reward = reward;
        this.clientReward = clientReward;
        this.icon = clientReward.icon();
        this.tooltip = clientReward.tooltip();
        this.isError = isError;
    }

    @Override
    protected void extractWidgetRenderState(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        IconRenderers.draw(icon, graphics, getX(), getY());
        if (!tooltip.isEmpty() && isHovered() && graphics.containsPointInScissor(mouseX, mouseY)) {
            GuiHelper.tooltip(graphics, client.font, tooltip,  mouseX, mouseY, DefaultTooltipPositioner.INSTANCE);
            if (isError) graphics.requestCursor(CursorTypes.NOT_ALLOWED);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) { /* do nothing */ }

    @Override
    public void playDownSound(SoundManager soundManager) { /* do nothing */ }
}
