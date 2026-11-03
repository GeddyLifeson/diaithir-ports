package io.github.tr100000.researcher.api.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NullMarked;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

@NullMarked
public class IndentedTextHolder implements Consumer<Component>, Iterable<MutableComponent> {
    private static final int DEFAULT_INDENT = 2;

    private final List<MutableComponent> text = new ObjectArrayList<>();
    private int indent = 0;

    public int push(int amount) {
        indent += amount;
        return indent;
    }

    public int pop(int amount) {
        indent -= amount;
        return indent;
    }

    public int push() {
        return push(DEFAULT_INDENT);
    }

    public int pop() {
        return pop(DEFAULT_INDENT);
    }

    public void accept(Component text) {
        this.text.add(getIndentText().append(text));
    }

    public void accept(List<MutableComponent> text) {
        text.forEach(this);
    }

    public void accept(IndentedTextHolder textHolder) {
        textHolder.forEach(this);
    }

    private MutableComponent getIndentText() {
        return Component.literal(" ".repeat(indent));
    }

    public List<MutableComponent> getText() {
        return text;
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }

    public int count() {
        return text.size();
    }

    @Override
    public Iterator<MutableComponent> iterator() {
        return text.iterator();
    }
}
