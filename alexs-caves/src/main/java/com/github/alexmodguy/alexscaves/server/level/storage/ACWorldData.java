package com.github.alexmodguy.alexscaves.server.level.storage;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.entity.living.LuxtructosaurusEntity;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import net.neoforged.neoforge.common.world.chunk.TicketSet;

import javax.annotation.Nullable;
import java.util.*;

public class ACWorldData extends SavedData {

    private static final String IDENTIFIER = "alexscaves_world_data";
    public static final com.mojang.serialization.Codec<ACWorldData> CODEC = CompoundTag.CODEC.xmap(ACWorldData::load, ACWorldData::save);
    public static final net.minecraft.world.level.saveddata.SavedDataType<ACWorldData> TYPE = new net.minecraft.world.level.saveddata.SavedDataType<>(
            net.minecraft.resources.Identifier.fromNamespaceAndPath(AlexsCaves.MODID, IDENTIFIER), ACWorldData::new, CODEC);
    private Map<UUID, Integer> deepOneReputations = new HashMap<>();
    private boolean primordialBossDefeatedOnce = false;
    private long firstPrimordialBossDefeatTimestamp = -1;
    private Set<Integer> trackedLuxtructosaurusIds = new ObjectArraySet();


    private ACWorldData() {
        super();
    }

    public static ACWorldData get(Level world) {
        if (world instanceof ServerLevel) {
            ServerLevel overworld = ((ServerLevel) world).getServer().getLevel(Level.OVERWORLD);
            ACWorldData data = overworld.getDataStorage().computeIfAbsent(TYPE);
            if (data != null) {
                data.setDirty();
            }
            return data;
        }
        return null;
    }

    public static ACWorldData load(CompoundTag nbt) {
        ACWorldData data = new ACWorldData();
        if (nbt.getList("DeepOneReputations").isPresent()) {
            ListTag listtag = nbt.getListOrEmpty("DeepOneReputations");
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag innerTag = listtag.getCompoundOrEmpty(i);
                data.deepOneReputations.put(innerTag.read("UUID", net.minecraft.core.UUIDUtil.CODEC).orElse(null), innerTag.getIntOr("Reputation", 0));
            }
        }
        data.primordialBossDefeatedOnce = nbt.getBooleanOr("PrimordialBossDefeatedOnce", false);
        data.firstPrimordialBossDefeatTimestamp = nbt.getLongOr("FirstPrimordialBossDefeatTimestamp", 0L);
        // Note: trackedLuxtructosaurusIds is not persisted since entity IDs are runtime values
        return data;
    }

    public static CompoundTag save(ACWorldData self) {
        return self.saveTag(new CompoundTag());
    }

    private CompoundTag saveTag(CompoundTag compound) {
        if (!this.deepOneReputations.isEmpty()) {
            ListTag listTag = new ListTag();
            for (Map.Entry<UUID, Integer> reputations : deepOneReputations.entrySet()) {
                CompoundTag tag = new CompoundTag();
                tag.store("UUID", net.minecraft.core.UUIDUtil.CODEC, reputations.getKey());
                tag.putInt("Reputation", reputations.getValue());
                listTag.add(tag);
            }
            compound.put("DeepOneReputations", listTag);
        }
        compound.putBoolean("PrimordialBossDefeatedOnce", primordialBossDefeatedOnce);
        compound.putLong("FirstPrimordialBossDefeatTimestamp", firstPrimordialBossDefeatTimestamp);
        // Note: trackedLuxtructosaurusIds is not persisted since entity IDs are runtime values
        return compound;
    }

    public int getDeepOneReputation(@Nullable UUID uuid) {
        return uuid == null ? 0 : deepOneReputations.getOrDefault(uuid, 0);
    }

    public void setDeepOneReputation(UUID uuid, int reputation) {
        deepOneReputations.put(uuid, Mth.clamp(reputation, -100, 100));
    }

    public boolean isPrimordialBossActive(Level level){
        for(int i : trackedLuxtructosaurusIds){
            if(level.getEntity(i) instanceof LuxtructosaurusEntity lux && lux.isAlive() && lux.isLoadedInWorld()){
                return true;
            }
        }
        return false;
    }

    public void trackPrimordialBoss(int id, boolean add){
        if(add){
            trackedLuxtructosaurusIds.add(id);
        }else{
            trackedLuxtructosaurusIds.remove(id);
        }
    }


    public boolean isPrimordialBossDefeatedOnce(){
        return primordialBossDefeatedOnce;
    }

    public void setPrimordialBossDefeatedOnce(boolean defeatedOnce){
        this.primordialBossDefeatedOnce = defeatedOnce;
    }

    public long getFirstPrimordialBossDefeatTimestamp(){
        return firstPrimordialBossDefeatTimestamp;
    }

    public void setFirstPrimordialBossDefeatTimestamp(long time){
        this.firstPrimordialBossDefeatTimestamp = time;
    }

    public void fillOutCaveMap(UUID uuid, ItemStack map, ServerLevel serverLevel, BlockPos center, Player player){
        com.github.alexmodguy.alexscaves.server.level.map.CaveMapWorkers.add(new com.github.alexmodguy.alexscaves.server.level.map.CaveBiomeMapWorldWorker(map, serverLevel, center, player, uuid));
    }

    public boolean isCaveMapTicking(){
        return com.github.alexmodguy.alexscaves.server.level.map.CaveMapWorkers.isActive();
    }

    public static void clearLoadedChunksCallback(ServerLevel serverLevel, TicketHelper ticketHelper) {
        //remove all forced chunks on server relog
        int i = 0;
        for(Map.Entry<UUID, TicketSet> entry : ticketHelper.getEntityTickets().entrySet()){
            ticketHelper.removeAllTickets(entry.getKey());
            i++;
        }
        for(Map.Entry<BlockPos, TicketSet> entry : ticketHelper.getBlockTickets().entrySet()){
            ticketHelper.removeAllTickets(entry.getKey());
            i++;
        }
        if(i > 0){
            AlexsCaves.LOGGER.debug("unloaded {} forced chunks", i);
        }
    }
}
