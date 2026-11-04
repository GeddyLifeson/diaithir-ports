package net.diaithir.druidry.ars.common.block.tile;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.diaithir.druidry.ars.api.perk.IPerk;
import net.diaithir.druidry.ars.api.perk.PerkSlot;
import net.diaithir.druidry.ars.api.util.PerkUtil;
import net.diaithir.druidry.ars.common.block.AlterationTable;
import net.diaithir.druidry.ars.common.block.ITickable;
import net.diaithir.druidry.ars.common.block.ThreePartBlock;
import net.diaithir.druidry.ars.common.items.PerkItem;
import net.diaithir.druidry.ars.common.items.data.ArmorPerkHolder;
import net.diaithir.druidry.ars.common.util.PortUtil;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;

public class AlterationTile extends ModdedTile implements ITickable {

    public ItemStack armorStack = ItemStack.EMPTY;
    public ItemEntity renderEntity;
    public List<ItemStack> perkList = new ArrayList<>();

    public int newPerkTimer = 0;

    public AlterationTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    public AlterationTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.ARMOR_TILE, pos, state);
    }





    public @Nullable AlterationTile getLogicTile() {
        AlterationTile tile = this;
        if (!isMasterTile()) {
            BlockEntity tileEntity = level.getBlockEntity(getBlockPos().relative(AlterationTable.getConnectedDirection(getBlockState())));
            tile = tileEntity instanceof AlterationTile alterationTile ? alterationTile : null;
        }
        return tile;
    }

    public boolean isMasterTile() {
        return getBlockState().getValue(AlterationTable.PART) == ThreePartBlock.HEAD;
    }

    public void setArmorStack(ItemStack stack, Player player){
        ArmorPerkHolder holder = stack.getOrDefault(DataComponentRegistry.ARMOR_PERKS, new ArmorPerkHolder(null, List.of(), 0, new HashMap<>()));
        this.perkList = new ArrayList<>(PerkUtil.getPerksAsItems(stack).stream().map(Item::getDefaultInstance).toList());
        stack.set(DataComponentRegistry.ARMOR_PERKS, holder.setPerks(new ArrayList<>()));
        this.armorStack = stack.copy();
        stack.shrink(1);
        this.newPerkTimer = 0;
        updateBlock();

    }

    public void removePerk(Player player) {
        if(!perkList.isEmpty()){
            ItemStack stack = perkList.get(0);
            if(!player.addItem(stack.copy())){
                level.addFreshEntity(new ItemEntity(level, player.position().x(), player.position().y(), player.position().z(), stack.copy()));
            }
            perkList.remove(0);
        }
        updateBlock();
    }

    public void removeArmorStack(Player player){
        ArmorPerkHolder perkHolder = PerkUtil.getPerkHolder(armorStack);
        Map<IPerk, CompoundTag> perkTags = new HashMap<>();
        var newHolder = perkHolder.setPerks(new ArrayList<>(perkList.stream().map(i ->{
            if(i.getItem() instanceof PerkItem perkItem){
                var perk = perkItem.perk;
                CompoundTag initTag = perk.getInitTag();
                if(initTag != null && perkHolder.getTagForPerk(perk) == null){
                    perkTags.put(perk, initTag);
                }
                return perk;
            }
            return null;
        }).filter(Objects::nonNull).toList()));
        var copyStack = armorStack.copy();
        copyStack.set(DataComponentRegistry.ARMOR_PERKS, newHolder.setPerkTags(perkTags));
        if(!player.addItem(copyStack)){
            level.addFreshEntity(new ItemEntity(level, player.position().x(), player.position().y(), player.position().z(), copyStack));
        }
        this.armorStack = ItemStack.EMPTY;
        this.perkList = new ArrayList<>();
        updateBlock();
    }

    public void addPerkStack(ItemStack stack, Player player){
        ArmorPerkHolder perkHolder = PerkUtil.getPerkHolder(armorStack);
        if (perkHolder == null) {
            PortUtil.sendMessage(player, Component.translatable("ars_nouveau.perk.set_armor"));
            return;
        }
        if(this.perkList.size() >= 3 || this.perkList.size() >= perkHolder.getSlotsForTier(armorStack).size()){
            PortUtil.sendMessage(player, Component.translatable("ars_nouveau.perk.max_perks"));
            return;
        }
        PerkSlot foundSlot = getAvailableSlot(perkHolder);
        if(stack.getItem() instanceof PerkItem perkItem) {
            IPerk perk = perkItem.perk;
            if (foundSlot != null && perk.validForSlot(foundSlot, armorStack, player)) {
                this.perkList.add(stack.split(1));
                if (newPerkTimer <= 0) {
                    newPerkTimer = 40;
                }
                updateBlock();
            }
        }
    }

    private PerkSlot getAvailableSlot(ArmorPerkHolder perkHolder){
        if(this.perkList.size() >= perkHolder.getSlotsForTier(armorStack).size()){
            return null;
        }else{
            return perkHolder.getSlotsForTier(armorStack).get(this.perkList.size());
        }
    }

    public void dropItems(){
        if(!armorStack.isEmpty()){
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), armorStack.copy()));
        }
        for(ItemStack stack : perkList){
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack.copy()));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        if(!armorStack.isEmpty()) {
            tag.store("armorStack", ItemStack.OPTIONAL_CODEC, armorStack);
        }
        tag.putInt("numPerks", perkList.size());
        int count = 0;
        for(ItemStack i : perkList){
            tag.store("perk" + count, ItemStack.OPTIONAL_CODEC, i);
            count++;
        }
        tag.putInt("newPerkTimer", newPerkTimer);
    }

    @Override
    protected void loadAdditional(ValueInput compound) {
        super.loadAdditional(compound);
        this.armorStack = compound.read("armorStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        int count = compound.getIntOr("numPerks", 0);
        perkList = new ArrayList<>();
        for(int i = 0; i < count; i++){
            ItemStack perk = compound.read("perk" + i, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
            perkList.add(perk);
        }
        this.newPerkTimer = compound.getIntOr("newPerkTimer", 0);
    }

    @Override
    public void tick(Level level, BlockState state, BlockPos pos) {
        if (level.isClientSide()) {
            if (newPerkTimer >= 0) {
                newPerkTimer--;
            }
        }
    }
}
