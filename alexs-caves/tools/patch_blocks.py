#!/usr/bin/env python3
import re, pathlib
B=pathlib.Path('src/main/java/com/github/alexmodguy/alexscaves/server/block')
def edit(rel, pairs, regex=False):
    p=B/rel; s=p.read_text()
    for a,b in pairs:
        if regex: s=re.sub(a,b,s)
        else:
            if a not in s: print('MISSING',rel,a[:50])
            s=s.replace(a,b)
    p.write_text(s)
edit('ACBlockRegistry.java',[(r'\.dropsLike\((\w+)\.get\(\)\)', r'.overrideLootTable(\1.get().getLootTable())'),
  (r'new LeavesBlock\(', 'new net.minecraft.world.level.block.UntintedParticleLeavesBlock(0.01F, net.minecraft.core.particles.ParticleTypes.OAK_LEAVES, ')],regex=True)
# sign valid blocks via event
edit('blockentity/ACBlockEntityRegistry.java',[(r'public static void expandVanillaDefinitions\(\) \{.*\n\}\s*$','''public static void expandVanillaDefinitions(net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.SIGN, ACBlockRegistry.PEWEN_SIGN.get(), ACBlockRegistry.PEWEN_WALL_SIGN.get(), ACBlockRegistry.THORNWOOD_SIGN.get(), ACBlockRegistry.THORNWOOD_WALL_SIGN.get());
        event.modify(BlockEntityType.HANGING_SIGN, ACBlockRegistry.PEWEN_HANGING_SIGN.get(), ACBlockRegistry.PEWEN_WALL_HANGING_SIGN.get(), ACBlockRegistry.THORNWOOD_HANGING_SIGN.get(), ACBlockRegistry.THORNWOOD_WALL_HANGING_SIGN.get());
    }
}
''')],regex=True) if False else None
p=B/'blockentity/ACBlockEntityRegistry.java'; s=p.read_text()
i=s.index('    public static void expandVanillaDefinitions()')
s=s[:i]+'''    public static void expandVanillaDefinitions(net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.SIGN, ACBlockRegistry.PEWEN_SIGN.get(), ACBlockRegistry.PEWEN_WALL_SIGN.get(), ACBlockRegistry.THORNWOOD_SIGN.get(), ACBlockRegistry.THORNWOOD_WALL_SIGN.get());
        event.modify(BlockEntityType.HANGING_SIGN, ACBlockRegistry.PEWEN_HANGING_SIGN.get(), ACBlockRegistry.PEWEN_WALL_HANGING_SIGN.get(), ACBlockRegistry.THORNWOOD_HANGING_SIGN.get(), ACBlockRegistry.THORNWOOD_WALL_HANGING_SIGN.get());
    }
}
'''
p.write_text(s)
edit('SirenLightBlock.java',[('dyeItem.getDyeColor().getTextColor()','heldItem.get(net.minecraft.core.component.DataComponents.DYE).getTextColor()')])
edit('AmbersolBlock.java',[('fillWithLights(BlockPos current, LevelReader level)','fillWithLights(BlockPos current, net.minecraft.world.level.LevelAccessor level)'),
  ('        fillWithLights(blockPos, levelAccessor);\n        return super.updateShape','        ticks.scheduleTick(blockPos, this, 1);\n        return super.updateShape')])
edit('NuclearBombBlock.java',[('public void onCaughtFire(','public boolean onCaughtFire('),
  ('level.gameEvent(igniter, GameEvent.PRIME_FUSE, blockPos);\n        }\n    }','level.gameEvent(igniter, GameEvent.PRIME_FUSE, blockPos);\n        }\n        return true;\n    }')])
edit('IceCreamBlock.java',[('getOcclusionShape(BlockState state, BlockGetter getter, BlockPos blockPos)','getOcclusionShape(BlockState state)')])
edit('CopperValveBlock.java',[('public void onRemove(BlockState state, Level level, BlockPos blockPos, BlockState blockState, boolean b) {\n        if (!b && !state.is(blockState.getBlock())) {','protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos blockPos, boolean b) {\n        if (!b) {'),
  ('            super.onRemove(state, level, blockPos, blockState, b);\n','            super.affectNeighborsAfterRemoval(state, level, blockPos, b);\n')])
edit('AbyssalAltarBlock.java',[('public void onRemove(BlockState state, Level level, BlockPos pos, BlockState state1, boolean b) {\n        if (!state.is(state1.getBlock())) {','protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, boolean b) {\n        {'),
  ('            super.onRemove(state, level, pos, state1, b);','            super.affectNeighborsAfterRemoval(state, level, pos, b);')])
edit('VolcanicCoreBlock.java',[('levelAccessor.scheduleTick(blockPos, this, 60 + levelAccessor.getRandom().nextInt(40));','ticks.scheduleTick(blockPos, this, 60 + randomSource.nextInt(40));')])
for f in ['fluid/PurpleSodaFluidType.java','fluid/AcidFluidType.java']:
    edit(f,[('level.dimensionType().ultraWarm()','level.dimension() == Level.NETHER'),('@Nullable Player player, Level level, BlockPos pos, FluidStack stack)','@Nullable net.minecraft.world.entity.LivingEntity player, Level level, BlockPos pos, FluidStack stack)')])
edit('blockentity/AbyssalAltarBlockEntity.java',[('startOpen(Player player)','startOpen(net.minecraft.world.entity.ContainerUser player)'),('stopOpen(Player player)','stopOpen(net.minecraft.world.entity.ContainerUser player)')])
edit('FrostedChocolateBlock.java',[('import net.minecraft.util.FastColor;\n','')])
edit('blockentity/ConfectionOvenBlockEntity.java',[('gingerbreadMan.hasImpulse = true;','gingerbreadMan.needsSync = true;')])
