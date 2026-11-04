package net.diaithir.druidry.ars.common.world.saved_data;

import net.diaithir.druidry.ars.ArsNouveau;
import net.minecraft.core.BlockPos;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = ArsNouveau.MODID)
public class RedstoneSavedData extends SavedData {
    public final Map<BlockPos, Entry> SIGNAL_MAP = new HashMap<>();

    public RedstoneSavedData(){

    }

    public static final Codec<RedstoneSavedData> CODEC = Entry.CODEC.listOf().xmap(list -> {
        RedstoneSavedData data = new RedstoneSavedData();
        for (Entry e : list) data.SIGNAL_MAP.put(e.pos, e);
        return data;
    }, data -> new ArrayList<>(data.SIGNAL_MAP.values()));

    public static final SavedDataType<RedstoneSavedData> TYPE = new SavedDataType<>(ArsNouveau.prefix("an_redstone_signals"), RedstoneSavedData::new, CODEC);

    public void tick(ServerLevel serverLevel){
        List<BlockPos> toRemove = new ArrayList<>();
        for(var signal : SIGNAL_MAP.values()){
            if(!serverLevel.isLoaded(signal.pos))
                continue;
            if(signal.ticks-- <= 0){
                toRemove.add(signal.pos);
            }
        }
        for(var pos : toRemove){
            SIGNAL_MAP.remove(pos);
            serverLevel.getBlockState(pos).onNeighborChange(serverLevel, pos, pos);
            serverLevel.updateNeighborsAt(pos, serverLevel.getBlockState(pos).getBlock());
        }
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    public static RedstoneSavedData from(ServerLevel level){
        return level.getDataStorage()
                .computeIfAbsent(TYPE);
    }

    public static class Entry{
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(e -> e.pos),
                Codec.INT.fieldOf("power").forGetter(e -> e.power),
                Codec.INT.fieldOf("ticks").forGetter(e -> e.ticks)
        ).apply(i, Entry::new));

        public BlockPos pos;
        public int power;
        public int ticks;

        public Entry(BlockPos pos, int power, int ticks){
            this.pos = pos;
            this.power = power;
            this.ticks = ticks;
        }

        public Entry(CompoundTag tag){
            this(new BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0)), tag.getIntOr("power", 0), tag.getIntOr("ticks", 0));
        }

        public CompoundTag save(CompoundTag tag){
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            tag.putInt("power", power);
            tag.putInt("ticks", ticks);
            return tag;
        }
    }

    @SubscribeEvent
    public static void serverTick(LevelTickEvent.Post e) {

        if (e.getLevel().isClientSide() )
            return;

        RedstoneSavedData data = RedstoneSavedData.from((ServerLevel) e.getLevel());
        data.tick((ServerLevel) e.getLevel());
    }
}
