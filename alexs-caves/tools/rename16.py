import re,glob,os
exec(open('tools/rename11.py').read().split('def main')[0])
SL='net.minecraft.server.level.ServerLevel'
files={os.path.basename(f)[:-5]:f for f in glob.glob('src/main/java/**/*.java',recursive=True)}
def rw(n,pairs,regex=False):
    p=files[n]; s=open(p).read()
    for a,b in pairs:
        if regex: s2=re.sub(a,b,s,flags=re.S)
        else: s2=s.replace(a,b)
        if s2==s: print('MISS',n,a[:60])
        s=s2
    open(p,'w').write(s)
rw('IDancesToJukebox',[('import com.github.alexthe666.citadel.server.message.DanceJukeboxMessage;\n',''),('        PacketDistributor.sendToServer(new DanceJukeboxMessage(entityId, dancing, pos));\n','')])
os.remove(files['VillagerUndergroundCabinMapTrade'])
for f in glob.glob(ROOT+'**/*.java',recursive=True):
    s=open(f).read();o=s
    s=re.sub(r'this\.convertTo\(([\w.]+\.get\(\)), true\)',r'this.convertTo(\1, net.minecraft.world.entity.ConversionParams.single(this, true, true), net.minecraft.world.entity.EntitySpawnReason.CONVERSION, __c -> {})',s)
    if s!=o: open(f,'w').write(s)
rw('GingerbreadManEntity',[('this.kill((ServerLevel) this.level());','this.kill((net.minecraft.server.level.ServerLevel) this.level());')])
rw('GingerbreadManStealGoal',[('IItemHandler handler = entity.getCapability(Capabilities.ItemHandler.ENTITY);\n        if (handler != null) {\n            for (int i = 0; i < handler.getSlots(); i++) {\n                if (handler.getStackInSlot(i).is','net.minecraft.world.Container handler = entity instanceof Player __p ? __p.getInventory() : entity instanceof net.minecraft.world.Container __c ? __c : null;\n        if (handler != null) {\n            for (int i = 0; i < handler.getContainerSize(); i++) {\n                if (handler.getItem(i).is'),
('IItemHandler handler = entity.getCapability(Capabilities.ItemHandler.ENTITY);\n        if (handler != null) {\n            List<Integer> validSlots = new ArrayList<>();\n            for (int i = 0; i < handler.getSlots(); i++) {\n                if (handler.getStackInSlot(i).is','net.minecraft.world.Container handler = entity instanceof Player __p ? __p.getInventory() : entity instanceof net.minecraft.world.Container __c ? __c : null;\n        if (handler != null) {\n            List<Integer> validSlots = new ArrayList<>();\n            for (int i = 0; i < handler.getContainerSize(); i++) {\n                if (handler.getItem(i).is'),
('ItemStack stack = handler.getStackInSlot(slotId);','ItemStack stack = handler.getItem(slotId);')])
rw('FrostmintExplosion',[('living.getArmorSlots()','java.util.List.of(living.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD), living.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST), living.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS), living.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET))')])
rw('MagneticEntityData',[('public static final Codec<MagneticEntityData> CODEC = RecordCodecBuilder.create(','public static final com.mojang.serialization.MapCodec<MagneticEntityData> MAP_CODEC = RecordCodecBuilder.mapCodec('),('        ).apply(instance, MagneticEntityData::new)\n    );','        ).apply(instance, MagneticEntityData::new)\n    );\n    public static final Codec<MagneticEntityData> CODEC = MAP_CODEC.codec();')])
rw('ACAttachmentRegistry',[('.serialize(MagneticEntityData.CODEC)','.serialize(MagneticEntityData.MAP_CODEC)')])
rw('GammaroachEntity',[('pickupMonster.startRiding(GammaroachEntity.this, true)','pickupMonster.startRiding(GammaroachEntity.this, true, true)')])
rw('CaramelCubeEntity',[('10, true, false, (y) -> {\n            return Math.abs(y.getY() - this.getY()) <= 4.0D;\n        })','10, true, false, (y, __l) -> {\n            return Math.abs(y.getY() - this.getY()) <= 4.0D;\n        })')])
rw('SodaBottleRocketEntity',[('.entityInside(this.level(), blockpos, this)','.entityInside(this.level(), blockpos, this, net.minecraft.world.entity.InsideBlockEffectApplier.NOOP, true)')])
rw('FallingGuanoEntity',[('tag.put("BlockState", NbtUtils.writeBlockState(this.guanoState));','tag.store("BlockState", net.minecraft.world.level.block.state.BlockState.CODEC, this.guanoState);'),('this.guanoState = NbtUtils.readBlockState(this.level().holderLookup(Registries.BLOCK), tag.getCompoundOrEmpty("BlockState"));','this.guanoState = tag.read("BlockState", net.minecraft.world.level.block.state.BlockState.CODEC).orElse(this.guanoState);')])
rw('AbstractMovingBlockEntity',[('EntityDataSerializers.COMPOUND_TAG','com.github.alexmodguy.alexscaves.server.misc.ACDataSerializers.COMPOUND_TAG'),('dataBlock.blockData.getAllKeys()','dataBlock.blockData.keySet()'),('blockentity.loadWithComponents(compoundtag, level().registryAccess());','blockentity.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level().registryAccess(), compoundtag));'),
('''        if (compound.contains("BlockDataContainer", 10)) {
            this.setAllBlockData(compound.getCompoundOrEmpty("BlockDataContainer"));
        }''','''        compound.read("BlockDataContainer", CompoundTag.CODEC).ifPresent(this::setAllBlockData);'''),('compound.put("BlockDataContainer", this.getAllBlockData());','compound.store("BlockDataContainer", CompoundTag.CODEC, this.getAllBlockData());'),
('    @Override\n    protected AABB makeBoundingBox() {\n        List<AABB> aabbs = getShape().toAabbs();\n        AABB minMax = new AABB(this.getX() - 0.5F, this.getY() - 0.5F, this.getZ() - 0.5F, this.getX() + 0.5F, this.getY() + 0.5F, this.getZ() + 0.5F);','    @Override\n    protected AABB makeBoundingBox(Vec3 position) {\n        List<AABB> aabbs = getShape().toAabbs();\n        AABB minMax = new AABB(position.x - 0.5F, position.y - 0.5F, position.z - 0.5F, position.x + 0.5F, position.y + 0.5F, position.z + 0.5F);')])
rw('MagneticWeaponEntity',[('player.getInventory().armor.get(3)','player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD)'),('living.setLastHurtByPlayer(player);','living.setLastHurtByPlayer(player, 100);'),('this.setItemStack(ItemStack.parseOptional(this.registryAccess(), tag.getCompoundOrEmpty("WeaponStack")));','this.setItemStack(tag.read("WeaponStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));'),('tag.put("WeaponStack", this.getItemStack().save(this.registryAccess()));','tag.store("WeaponStack", ItemStack.OPTIONAL_CODEC, this.getItemStack());')])
rw('NuclearExplosionEntity',[('state.onBlockExploded(level(), carve, dummyExplosion);','level().setBlock(carve, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);')])
print('ok')
