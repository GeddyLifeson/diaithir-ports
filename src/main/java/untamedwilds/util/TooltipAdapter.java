package untamedwilds.util;

import net.minecraft.network.chat.Component;

import java.util.AbstractList;
import java.util.function.Consumer;

/**
 * Adapts the 26.1 tooltip Consumer to the List-based tooltip code used throughout the original mod.
 */
public class TooltipAdapter extends AbstractList<Component> {
    private final Consumer<Component> adder;

    public TooltipAdapter(Consumer<Component> adder) {
        this.adder = adder;
    }

    @Override
    public boolean add(Component component) {
        this.adder.accept(component);
        return true;
    }

    @Override
    public void add(int index, Component component) {
        this.adder.accept(component);
    }

    @Override
    public Component get(int index) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int size() {
        return 0;
    }
}
