package net.diaithir.carts;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.function.Supplier;

/** A train schedule: the station rails a cart stops at, in order, looping. Lives as an attachment on a cart or on a timetable item. */
public record Schedule(List<BlockPos> stops, int next) {
    public static final int MAX_STOPS = 32;
    public static final MapCodec<Schedule> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BlockPos.CODEC.listOf().fieldOf("stops").forGetter(Schedule::stops),
            com.mojang.serialization.Codec.INT.fieldOf("next").forGetter(Schedule::next)).apply(i, Schedule::new));

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DiaithirCarts.ID);
    public static final Supplier<AttachmentType<Schedule>> TYPE = ATTACHMENTS.register("schedule", () -> AttachmentType.builder(() -> new Schedule(List.of(), 0)).serialize(CODEC).build());

    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, DiaithirCarts.ID);
    public static final Supplier<net.minecraft.core.component.DataComponentType<Schedule>> ITEM = COMPONENTS.registerComponentType("schedule", b -> b.persistent(CODEC.codec()));

    public static Schedule get(IAttachmentHolder h) {
        if (!h.hasData(TYPE)) return null;
        Schedule s = h.getData(TYPE);
        return s.stops.isEmpty() ? null : s;
    }

    public Schedule add(BlockPos p) {
        if (stops.size() >= MAX_STOPS || (!stops.isEmpty() && stops.get(stops.size() - 1).equals(p))) return this;
        List<BlockPos> l = new java.util.ArrayList<>(stops);
        l.add(p.immutable());
        return new Schedule(List.copyOf(l), next);
    }

    public BlockPos target() {
        return stops.get(next % stops.size());
    }

    public Schedule advance() {
        return new Schedule(stops, (next + 1) % stops.size());
    }

    public static void clear(IAttachmentHolder h) {
        h.removeData(TYPE);
    }

    public static void set(Entity cart, Schedule s) {
        cart.setData(TYPE, s);
    }
}
