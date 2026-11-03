package net.diaithir.gear.workshop;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** State of one bench: the blueprint chosen, the material in each slot and the stacks that were spent (handed back on a reset). */
public final class StationEntity extends BlockEntity {
    String blueprint = "";
    final Map<String, String> parts = new LinkedHashMap<>();
    final List<ItemStack> spent = new ArrayList<>();

    public StationEntity(BlockPos pos, BlockState state) { super(Workshop.STATION_BE.get(), pos, state); }

    String station() { return ((StationBlock) getBlockState().getBlock()).station; }

    Craft.Blueprint bp() { return Catalog.blueprints().get(blueprint); }

    // ---- hand actions ----------------------------------------------------------------------------------------------------------

    /** Sneak with an empty hand: hand the parts back, or (bench empty) switch to the next blueprint this player knows. */
    void sneakUse(Player p) {
        if (!spent.isEmpty()) { refund(p); StationBlock.say(p, "workshop.diaithirgear.cleared"); return; }
        List<Craft.Blueprint> all = Catalog.forStation(station()).stream().filter(b -> Workshop.knows(p, b)).toList();
        if (all.isEmpty()) { StationBlock.say(p, "workshop.diaithirgear.none"); return; }
        int at = -1;
        for (int i = 0; i < all.size(); i++) if (all.get(i).id().equals(blueprint)) at = i;
        blueprint = all.get((at + 1) % all.size()).id();
        sync();
        p.sendOverlayMessage(describe());
    }

    /** Empty hand: take the finished item, or show what is still missing. */
    void take(Player p) {
        Craft.Blueprint b = bp();
        if (b == null) { StationBlock.say(p, "workshop.diaithirgear.pick"); return; }
        if (!Craft.missing(b, Workshop.materials(new Assembly(blueprint, parts, 0, List.of()))).isEmpty()) { p.sendOverlayMessage(describe()); return; }
        ItemStack out = Workshop.build(b, parts);
        if (out.isEmpty()) { StationBlock.say(p, "workshop.diaithirgear.unknown_schematic"); return; }
        parts.clear();
        spent.clear();
        sync();
        if (!p.getInventory().add(out)) p.drop(out, false);
        StationBlock.say(p, "workshop.diaithirgear.done", out.getHoverName());
    }

    /** A part item clicked onto the bench: fills the first open slot that accepts it. */
    void place(Player p, ItemStack held) {
        Craft.Blueprint b = bp();
        if (b == null) { StationBlock.say(p, "workshop.diaithirgear.pick"); return; }
        if (!Workshop.knows(p, b)) { StationBlock.say(p, "workshop.diaithirgear.no_schematic"); return; }
        if (Workshop.ageOf(p) < b.age()) { StationBlock.say(p, "workshop.diaithirgear.too_early", b.age()); return; }
        for (Craft.Slot s : b.slots()) {
            if (parts.containsKey(s.name())) continue;
            Craft.Material m = Catalog.materialFor(held, s.role());
            if (m == null) continue;
            if (Workshop.ageOf(p) < m.age()) { StationBlock.say(p, "workshop.diaithirgear.too_early", m.age()); return; }
            if (held.getCount() < s.count()) { StationBlock.say(p, "workshop.diaithirgear.need_more", s.count()); return; }
            spent.add(held.copyWithCount(s.count()));
            if (!p.getAbilities().instabuild) held.shrink(s.count());
            parts.put(s.name(), m.id());
            sync();
            p.sendOverlayMessage(describe());
            return;
        }
        p.sendOverlayMessage(describe());
    }

    /** A finished item clicked on the bench: apply honing, and an improvement when a spare item is in the off hand. */
    void service(Player p, ItemStack held) {
        Assembly a = held.get(Workshop.ASSEMBLY.get());
        Craft.Blueprint b = Catalog.blueprints().get(a.blueprint());
        if (b == null || !b.station().equals(station())) { StationBlock.say(p, "workshop.diaithirgear.wrong_bench"); return; }
        ItemStack off = p.getOffhandItem();
        var imp = off.isEmpty() ? java.util.Optional.<Catalog.Improvement>empty() : Workshop.improvementFor(off);
        if (imp.isPresent()) {
            Catalog.Improvement im = imp.get();
            if (a.improvements().contains(im.id())) { StationBlock.say(p, "workshop.diaithirgear.has_improvement"); return; }
            if (a.improvements().size() >= b.improvementSlots()) { StationBlock.say(p, "workshop.diaithirgear.full"); return; }
            List<String> l = new ArrayList<>(a.improvements());
            l.add(im.id());
            held.set(Workshop.ASSEMBLY.get(), new Assembly(a.blueprint(), a.parts(), a.uses(), l));
            if (!p.getAbilities().instabuild) off.shrink(1);
        }
        Workshop.refresh(held);
        Assembly n = held.get(Workshop.ASSEMBLY.get());
        Craft.Stats s = Workshop.stats(b, n);
        p.sendOverlayMessage(Component.translatable("workshop.diaithirgear.serviced", held.getHoverName(), n.hone(), s.damage(), s.durability(), s.massKg()));
    }

    Component describe() {
        Craft.Blueprint b = bp();
        if (b == null) return Component.translatable("workshop.diaithirgear.pick");
        List<Component> need = new ArrayList<>();
        for (Craft.Slot s : b.slots()) {
            if (parts.containsKey(s.name())) continue;
            need.add(Component.translatable("workshop.diaithirgear.slot_need", s.count(), Component.translatable("slot.diaithirgear." + s.name())));
        }
        Component name = Workshop.name(b);
        if (need.isEmpty()) return Component.translatable("workshop.diaithirgear.complete", name);
        Component list = Component.empty();
        for (int i = 0; i < need.size(); i++) list = Component.empty().append(list).append(i == 0 ? Component.empty() : Component.literal(", ")).append(need.get(i));
        return Component.translatable("workshop.diaithirgear.missing", name, list);
    }

    private void refund(Player p) {
        for (ItemStack s : spent) if (!p.getInventory().add(s.copy())) p.drop(s.copy(), false);
        spent.clear();
        parts.clear();
        sync();
    }

    // ---- save and sync ---------------------------------------------------------------------------------------------------------

    private void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("blueprint", blueprint);
        out.store("parts", Codec.unboundedMap(Codec.STRING, Codec.STRING), new LinkedHashMap<>(parts));
        out.store("spent", ItemStack.CODEC.listOf(), List.copyOf(spent));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        blueprint = in.getStringOr("blueprint", "");
        parts.clear();
        parts.putAll(in.read("parts", Codec.unboundedMap(Codec.STRING, Codec.STRING)).orElse(Map.of()));
        spent.clear();
        spent.addAll(in.read("spent", ItemStack.CODEC.listOf()).orElse(List.of()));
    }
}
