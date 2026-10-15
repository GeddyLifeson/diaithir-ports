package com.buuz135.industrial.item.infinity;

import com.buuz135.industrial.gui.component.ItemStackTankScreenAddon;
import com.buuz135.industrial.utils.IFAttachments;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.api.client.IScreenAddonProvider;
import com.hrznstudio.titanium.component.fluid.FluidTankComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * The tanks of an infinity item, kept on the item as one tag per tank. Every change is an exchange of the accessed item
 * for an updated copy, so the access commits or rolls it back with the transaction.
 */
public class InfinityTankStorage implements ResourceHandler<FluidResource>, IScreenAddonProvider {
    private final ItemAccess access;
    private final List<TankDefinition> tanks;

    public InfinityTankStorage(ItemAccess access, TankDefinition... tanks) {
        this.access = access;
        this.tanks = List.of(tanks);
    }

    public InfinityTankStorage(ItemStack stack, TankDefinition... tanks) {
        this(ItemAccess.forStack(stack), tanks);
    }

    /** The same tanks on another access, for the item capability. */
    public InfinityTankStorage on(ItemAccess access) {
        return new InfinityTankStorage(access, tanks.toArray(TankDefinition[]::new));
    }

    private CompoundTag stored(ItemResource item) {
        return item.getOrDefault(IFAttachments.INFINITY_TANKS.get(), new CompoundTag());
    }

    private FluidStack fluid(ItemResource item, int index) {
        CompoundTag tank = stored(item).getCompoundOrEmpty(tanks.get(index).name());
        return FluidStack.OPTIONAL_CODEC.parse(IFAttachments.registryAccess().createSerializationContext(NbtOps.INSTANCE), tank.getCompoundOrEmpty("Fluid"))
                .result().orElse(FluidStack.EMPTY);
    }

    private int set(ItemResource item, int index, FluidStack fluid, TransactionContext transaction) {
        CompoundTag all = stored(item).copy();
        CompoundTag tank = new CompoundTag();
        if (!fluid.isEmpty()) {
            tank.put("Fluid", FluidStack.CODEC.encodeStart(IFAttachments.registryAccess().createSerializationContext(NbtOps.INSTANCE), fluid).getOrThrow());
        }
        all.put(tanks.get(index).name(), tank);
        return access.exchange(item.with(IFAttachments.INFINITY_TANKS, all), 1, transaction);
    }

    @Override
    public int size() {
        return tanks.size();
    }

    @Override
    public FluidResource getResource(int index) {
        return FluidResource.of(fluid(access.getResource(), index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return fluid(access.getResource(), index).getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return tanks.get(index).capacity();
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return tanks.get(index).filter().test(resource.toStack(1));
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || !tanks.get(index).insert() || !isValid(index, resource)) return 0;
        ItemResource item = access.getResource();
        FluidStack held = fluid(item, index);
        if (!held.isEmpty() && !resource.matches(held)) return 0;
        int added = Math.min(amount, tanks.get(index).capacity() - held.getAmount());
        if (added <= 0) return 0;
        return set(item, index, resource.toStack(held.getAmount() + added), transaction) == 1 ? added : 0;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || !tanks.get(index).extract()) return 0;
        ItemResource item = access.getResource();
        FluidStack held = fluid(item, index);
        if (held.isEmpty() || !resource.matches(held)) return 0;
        int taken = Math.min(amount, held.getAmount());
        return set(item, index, held.copyWithAmount(held.getAmount() - taken), transaction) == 1 ? taken : 0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public @NotNull List<IFactory<?>> getScreenAddons() {
        List<IFactory<?>> list = new ArrayList<>();
        IFluidHandler view = IFluidHandler.of(this);
        for (int i = 0; i < this.tanks.size(); i++) {
            var tankDefinition = this.tanks.get(i);
            int finalI = i;
            list.add(() -> new ItemStackTankScreenAddon(tankDefinition.x, tankDefinition.y, view, finalI, tankDefinition.type, tankDefinition.definedFluidStack));
        }
        return list;
    }

    public record TankDefinition(String name, int capacity, int x, int y, Predicate<FluidStack> filter, boolean extract,
                                 boolean insert, FluidTankComponent.Type type, FluidStack definedFluidStack) {
    }
}
