package org.diaithir.archimedes;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** The blocks of one ship, in local coordinates (0..size-1). Replaces the original MobileChunk. */
public final class ShipChunk {
    public static final int CRATE_SLOTS = 27;

    public final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    public final Map<BlockPos, NonNullList<ItemStack>> crates = new LinkedHashMap<>();
    public int sizeX, sizeY, sizeZ;
    /** Local position of the helm (ship marker) and its facing as a horizontal index (0 N, 1 E, 2 S, 3 W). */
    public BlockPos helm = BlockPos.ZERO;
    public int front;

    public float mass;
    public int balloons, floaters, engines;
    /** Blocks per vertical layer, used for the displacement calculation. */
    public int[] layerCount = new int[0];

    public void put(BlockPos local, BlockState state) {
        blocks.put(local.immutable(), state);
    }

    /** Recomputes size and the block statistics. Call after the block map is final. */
    public void finish() {
        int mx = 0, my = 0, mz = 0;
        mass = 0;
        balloons = floaters = engines = 0;
        for (var e : blocks.entrySet()) {
            BlockPos p = e.getKey();
            mx = Math.max(mx, p.getX() + 1);
            my = Math.max(my, p.getY() + 1);
            mz = Math.max(mz, p.getZ() + 1);
        }
        sizeX = mx;
        sizeY = my;
        sizeZ = mz;
        layerCount = new int[my];
        for (var e : blocks.entrySet()) {
            Block b = e.getValue().getBlock();
            mass += ShipDensity.of(e.getValue());
            layerCount[e.getKey().getY()]++;
            if (b instanceof org.diaithir.archimedes.block.BalloonBlock) balloons++;
            else if (b == ArchimedesMod.FLOATER.get()) floaters++;
            else if (b == ArchimedesMod.ENGINE.get()) engines++;
        }
        if (mass < 0.1f) mass = 0.1f;
    }

    public double centerX() { return sizeX / 2.0; }
    public double centerZ() { return sizeZ / 2.0; }

    public CompoundTag save(net.minecraft.core.HolderLookup.Provider registries, boolean withInventories) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (var e : blocks.entrySet()) {
            CompoundTag c = new CompoundTag();
            BlockPos p = e.getKey();
            c.putInt("x", p.getX());
            c.putInt("y", p.getY());
            c.putInt("z", p.getZ());
            c.put("s", NbtUtils.writeBlockState(e.getValue()));
            list.add(c);
        }
        tag.put("blocks", list);
        tag.putInt("hx", helm.getX());
        tag.putInt("hy", helm.getY());
        tag.putInt("hz", helm.getZ());
        tag.putInt("front", front);
        if (withInventories) {
            ListTag cl = new ListTag();
            for (var e : crates.entrySet()) {
                CompoundTag c = new CompoundTag();
                BlockPos p = e.getKey();
                c.putInt("x", p.getX());
                c.putInt("y", p.getY());
                c.putInt("z", p.getZ());
                c.put("items", CrateStore.write(e.getValue(), registries));
                cl.add(c);
            }
            tag.put("crates", cl);
        }
        return tag;
    }

    public static ShipChunk load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        ShipChunk chunk = new ShipChunk();
        HolderGetter<Block> getter = registries.lookupOrThrow(Registries.BLOCK);
        for (var t : tag.getListOrEmpty("blocks")) {
            if (!(t instanceof CompoundTag c)) continue;
            BlockPos p = new BlockPos(c.getIntOr("x", 0), c.getIntOr("y", 0), c.getIntOr("z", 0));
            chunk.put(p, NbtUtils.readBlockState(getter, c.getCompoundOrEmpty("s")));
        }
        chunk.helm = new BlockPos(tag.getIntOr("hx", 0), tag.getIntOr("hy", 0), tag.getIntOr("hz", 0));
        chunk.front = tag.getIntOr("front", 0);
        for (var t : tag.getListOrEmpty("crates")) {
            if (!(t instanceof CompoundTag c)) continue;
            BlockPos p = new BlockPos(c.getIntOr("x", 0), c.getIntOr("y", 0), c.getIntOr("z", 0));
            chunk.crates.put(p, CrateStore.read(c.getCompoundOrEmpty("items"), registries));
        }
        chunk.finish();
        return chunk;
    }
}
