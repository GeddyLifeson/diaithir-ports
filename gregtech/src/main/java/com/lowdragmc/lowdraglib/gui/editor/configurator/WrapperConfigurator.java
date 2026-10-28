package com.lowdragmc.lowdraglib.gui.editor.configurator;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2022/12/3
 * @implNote WrapperConfigurator
 */
@Accessors(chain = true)
public class WrapperConfigurator extends Configurator {
    public final Widget inner;
    @Setter
    public boolean removeTitleBar;

    public WrapperConfigurator(Widget widget) {
        this("", widget, true);
    }

    public WrapperConfigurator(Function<WrapperConfigurator, Widget> widgetSupplier) {
        this("", widgetSupplier, true);
    }

    public WrapperConfigurator(String name, Widget widget) {
        this(name, widget, false);
    }

    public WrapperConfigurator(String name, Function<WrapperConfigurator, Widget> widgetSupplier) {
        this(name, widgetSupplier, false);
    }

    public WrapperConfigurator(String name, Widget widget, boolean removeTitleBar) {
        this(name, configurator -> widget, removeTitleBar);
    }

    public WrapperConfigurator(String name, Function<WrapperConfigurator, Widget> widgetSupplier, boolean removeTitleBar) {
        super(name);
        this.inner = widgetSupplier.apply(this);
        this.removeTitleBar = removeTitleBar;
    }

    @Override
    public void computeHeight() {  /* ported stub */ }

    @Override
    public void init(int width) {  /* ported stub */ }
}
