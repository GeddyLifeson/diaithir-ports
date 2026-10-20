package untamedwilds.block.blockentity;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import untamedwilds.UntamedWilds;
import untamedwilds.config.ConfigGamerules;
import untamedwilds.init.ModBlock;
import untamedwilds.init.ModTags;
import untamedwilds.util.EntityUtils;

import javax.annotation.Nullable;
import java.util.Objects;

public class CageBlockEntity extends BlockEntity {

    private CompoundTag data;
    private boolean locked;

    public CageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlock.TILE_ENTITY_CAGE.get(), pos, state);
    }

    public static boolean isBlacklisted(Entity entity) {
        return entity.typeHolder().is(ModTags.EntityTags.CAGE_BLACKLIST);
    }

    public boolean cageEntity(Mob entity) {
        if (!this.isLocked()) {
            if (!isBlacklisted(entity) && (ConfigGamerules.easyMobCapturing.get() || entity.getTarget() == null)) {
                this.setTagCompound(EntityUtils.writeEntityToNBT(entity));
                this.setLocked(true);
                entity.discard();
                setChanged();
                return true;
            }
        }
        return false;
    }

    public boolean spawnCagedCreature(ServerLevel worldIn, BlockPos pos, boolean offsetHitbox) {
        if (!worldIn.isClientSide() && this.isLocked()) {
            EntityType<?> entity = EntityUtils.getEntityTypeFromTag(this.getTagCompound(), null);
            if (entity != null) {
                if (worldIn.noCollision(untamedwilds.util.EntityUtils.getEntityTypeAABB(entity, pos.getX() + 0.5F, pos.getY() - (offsetHitbox ? entity.getHeight() + 1.2F : 0), pos.getZ() + 0.5F))) {
                    if (this.data.getCompoundOrEmpty("EntityTag").read("UUID", net.minecraft.core.UUIDUtil.CODEC).map(u -> worldIn.getEntity(u) != null).orElse(false)) {
                        UntamedWilds.LOGGER.info("UUID is already present in the Level; Randomizing UUID for the new mob");
                        this.data.getCompoundOrEmpty("EntityTag").store("UUID", net.minecraft.core.UUIDUtil.CODEC, Mth.createInsecureUUID(worldIn.getRandom()));
                    }
                    final Entity[] created = new Entity[1];
                    created[0] = entity.create(worldIn, (e) -> {
                        if (this.data != null && this.data.contains("EntityTag")) {
                            e.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, worldIn.registryAccess(), this.data.getCompoundOrEmpty("EntityTag")));
                        }
                    }, pos, EntitySpawnReason.DISPENSER, true, !Objects.equals(pos, this.getBlockPos()));
                    Entity caged_entity = created[0];
                    if (caged_entity != null) {
                        caged_entity.snapTo(pos.getX() + 0.5F, pos.getY() - (offsetHitbox ? caged_entity.getBbHeight() + 1.2 : 0.8), pos.getZ() + 0.5F, Mth.wrapDegrees(worldIn.getRandom().nextFloat() * 360.0F), 0.0F);
                        if (!worldIn.tryAddFreshEntityWithPassengers(caged_entity)) {
                            caged_entity.setUUID(Mth.createInsecureUUID(worldIn.getRandom()));
                            worldIn.addFreshEntityWithPassengers(caged_entity);
                        }
                        this.setTagCompound(null);
                        this.setLocked(true);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Nullable
    public CompoundTag getTagCompound() { return this.data; }

    private void setTagCompound(@Nullable CompoundTag nbt) { this.data = nbt; }

    public boolean hasTagCompound() { return this.data != null; }

    public boolean isLocked() { return this.locked; }

    private void setLocked(boolean locked) { this.locked = locked; }

    /**
     * Read a tag coming from an ItemStack (used when placing the block with saved entity data).
     * This avoids calling protected/private block entity APIs from external classes.
     */
    public void readFromItemTag(@Nullable CompoundTag tag) {
        if (tag == null) {
            this.data = null;
            this.locked = false;
        } else {
            this.data = tag.copy();
            this.locked = tag.getBooleanOr("closed", false);
        }
        setChanged();
    }

    @Override
    protected void loadAdditional(ValueInput compound) {
        super.loadAdditional(compound);
        this.setTagCompound(compound.read("EntityTag", CompoundTag.CODEC).map(t -> { CompoundTag w = new CompoundTag(); w.put("EntityTag", t); return w; }).orElse(null));
        this.setLocked(compound.getBooleanOr("closed", false));
    }

    @Override
    protected void saveAdditional(ValueOutput compound) {
        super.saveAdditional(compound);
        compound.putBoolean("closed", this.isLocked());
        if (this.getTagCompound() != null) {
            compound.store("EntityTag", CompoundTag.CODEC, this.getTagCompound().getCompoundOrEmpty("EntityTag"));
        }
    }
}