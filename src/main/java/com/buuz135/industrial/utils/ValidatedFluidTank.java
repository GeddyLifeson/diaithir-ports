package com.buuz135.industrial.utils;

import com.hrznstudio.titanium.component.IComponentHarness;
import com.hrznstudio.titanium.component.fluid.SidedFluidTankComponent;
import com.hrznstudio.titanium.component.sideness.IFacingComponent;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

import java.util.function.Predicate;

/**
 * Titanium 26.1 dropped tank validators, so a tank would accept any fluid. The builder methods are overridden only to
 * keep this type through IF's construction chains, which end in setValidator.
 */
public class ValidatedFluidTank<T extends IComponentHarness> extends SidedFluidTankComponent<T> {
    private Predicate<FluidStack> validator = stack -> true;

    public ValidatedFluidTank(String name, int amount, int posX, int posY, int pos) {
        super(name, amount, posX, posY, pos);
    }

    public ValidatedFluidTank<T> setValidator(Predicate<FluidStack> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return super.isValid(index, resource) && validator.test(resource.toStack(1));
    }

    @Override
    public ValidatedFluidTank<T> setColor(DyeColor color) {
        super.setColor(color);
        return this;
    }

    @Override
    public ValidatedFluidTank<T> setColor(int color) {
        super.setColor(color);
        return this;
    }

    @Override
    public ValidatedFluidTank<T> disableFacingAddon() {
        super.disableFacingAddon();
        return this;
    }

    @Override
    public ValidatedFluidTank<T> setFacingHandlerPos(int x, int y) {
        super.setFacingHandlerPos(x, y);
        return this;
    }

    @Override
    public ValidatedFluidTank<T> setValidFaceModes(IFacingComponent.FaceMode... modes) {
        super.setValidFaceModes(modes);
        return this;
    }

    @Override
    public ValidatedFluidTank<T> setComponentHarness(T harness) {
        super.setComponentHarness(harness);
        return this;
    }

    @Override
    public ValidatedFluidTank<T> setTankType(Type type) {
        super.setTankType(type);
        return this;
    }

    @Override
    public ValidatedFluidTank<T> setTankAction(Action action) {
        super.setTankAction(action);
        return this;
    }

    @Override
    public ValidatedFluidTank<T> setOnContentChange(Runnable onContentChange) {
        super.setOnContentChange(onContentChange);
        return this;
    }
}
