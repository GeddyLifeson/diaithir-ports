package me.desht.pneumaticcraft.compat;

import net.minecraft.network.chat.Component;

import java.util.AbstractList;
import java.util.function.Consumer;

/**
 * Write-only List view over the 26.1 tooltip consumer, so 1.21-style code which just calls add() keeps working.
 */
public class TooltipList extends AbstractList<Component> {
    private final Consumer<Component> consumer;

    private TooltipList(Consumer<Component> consumer) {
        this.consumer = consumer;
    }

    public static java.util.List<Component> of(Consumer<Component> consumer) {
        return new TooltipList(consumer);
    }

    @Override
    public boolean add(Component c) {
        consumer.accept(c);
        return true;
    }

    @Override
    public void add(int index, Component c) {
        consumer.accept(c);
    }

    @Override
    public Component get(int index) {
        throw new UnsupportedOperationException("write-only");
    }

    @Override
    public int size() {
        return 0;
    }
}
