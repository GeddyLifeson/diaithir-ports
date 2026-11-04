# builds compute/jobs shards translating Nature's Spirit Java (Yarn, Fabric 1.21.1) to NeoForge 26.1.2 Mojang mappings
import json,pathlib,os
SRC=pathlib.Path('/tmp/ns/src/src/main/java/net/hibiscus/naturespirit')
SKIP=('datagen/','compat/','mixin/')
RULES="""You port Java source from the Fabric mod Nature's Spirit (Minecraft 1.21.1, Yarn mappings, MIT code) to NeoForge 26.1.2.116 for Minecraft 26.1.2 with OFFICIAL MOJANG mappings, as one package inside the Diaithir mod 'diaithir-overworld-biomes'.
Output ONLY the complete translated Java file, no prose, no markdown fences.
Package rule: net.hibiscus.naturespirit.X becomes com.diaithir.overworldbiomes.naturesspirit.X (update package and every import, including references to sibling classes). Registry namespace stays "natures_spirit" (MOD_ID = "natures_spirit").
Mapping rules: Yarn to Mojang names (net.minecraft.block.Block -> net.minecraft.world.level.block.Block, BlockState, Blocks, AbstractBlock.Settings -> BlockBehaviour.Properties, Identifier -> net.minecraft.resources.Identifier with Identifier.fromNamespaceAndPath(ns, path) (Identifier.of does not exist), RegistryKey -> ResourceKey, Registries -> BuiltInRegistries or Registries as in Mojmap, BlockPos, World -> Level, ServerWorld -> ServerLevel, PlayerEntity -> Player, ItemStack, ActionResult -> InteractionResult, Direction, VoxelShape/Shapes, Text -> Component, BlockSoundGroup -> SoundType, MapColor same, ItemGroup -> CreativeModeTab, FoliagePlacer/TrunkPlacer/TreeDecorator keep names under net.minecraft.world.level.levelgen.feature.*).
Registration: use NeoForge DeferredRegister / DeferredRegister.Blocks / DeferredRegister.Items / DeferredHolder with a static void init(IEventBus modBus) per registry class instead of Fabric Registry.register; replace Fabric API events with NeoForge events (NeoForge.EVENT_BUS or the mod bus); drop Fabric-only helpers (FabricBlockSettings, FabricItemSettings, FlammableBlockRegistry, CompostingChanceRegistry, StrippableBlockRegistry etc.) and instead expose public static maps/lists the module can wire from NeoForge events (keep the data, e.g. flammability and compost values, as static Map fields with a TODO-free helper method).
26.1 API notes: BlockBehaviour.Properties requires setId via registerBlock(...) of DeferredRegister.Blocks; items likewise via DeferredRegister.Items.registerItem; Block constructors take Properties; codecs (MapCodec) are required on Block subclasses via codec() overrides; use Fabric-free code; ResourceLocation is now Identifier. Where an API no longer exists, simplify the feature rather than guess, but keep every public class and member that other files in the mod reference. Keep behaviour, constants and names identical. Skip datagen, TerraBlender and Lithostitched code (not present in this port).
The source file follows. Its path relative to the mod root is given first.
"""
files=[p for p in SRC.rglob('*.java') if not any(s in str(p.relative_to(SRC))+'/' for s in SKIP) or p.parent==SRC]
files=[p for p in files if not str(p.relative_to(SRC)).startswith(SKIP)]
files.sort(key=lambda p:-p.stat().st_size)
N=4; shards=[[] for _ in range(N)]; sz=[0]*N
for p in files:
    i=sz.index(min(sz)); shards[i].append(p); sz[i]+=p.stat().st_size
root=pathlib.Path('/mnt/project-files/compute/jobs')
for i,s in enumerate(shards):
    d=root/f'naturesspirit_port-s{i:02d}'; d.mkdir(exist_ok=True)
    with open(d/'in.jsonl','w') as f:
        for p in s:
            rel=str(p.relative_to(SRC))
            f.write(json.dumps({"id":rel,"system":RULES,"prompt":f"PATH: {rel}\n\n"+p.read_text()})+"\n")
    (d/'README').write_text("Port Nature's Spirit Java (Yarn/Fabric 1.21.1) to NeoForge 26.1.2 Mojmap package com.diaithir.overworldbiomes.naturesspirit. model: sonnet. max_tokens 32000. Check rule: output is a complete Java file for that path (compile-fix loop validates). Owner: nature's-spirit port thread (worktree /home/user/wt/ns, branch claude/natures-spirit-port).\n")
    print(d.name,len(s),sz[i])
print(len(files))
