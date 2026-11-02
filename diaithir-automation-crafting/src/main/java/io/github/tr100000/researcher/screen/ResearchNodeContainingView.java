package io.github.tr100000.researcher.screen;

public abstract class ResearchNodeContainingView extends AbstractResearchView {
    private boolean isHovered = false;

    protected ResearchNodeContainingView(ResearchScreen parent, int x, int y, int width, int height) {
        super(parent, x, y, width, height);
    }

    public void onNodeShiftClicked(ResearchNodeWidget widget) {}

    public void setIsHovered(boolean isHovered) {
        this.isHovered = isHovered;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return isHovered && super.isMouseOver(mouseX, mouseY);
    }
}
