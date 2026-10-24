Lightman's Currency 26.1-1.0.0.1-diaithir.1. Original by Lightman314, https://github.com/Lightman314/LightmansCurrency
Licence: Apache License 2.0, as declared in the jar's META-INF/neoforge.mods.toml (license="Apache License 2.0").
The jar is the official CurseForge file lightmanscurrency-26.1-1.0.0.1.jar (sha256 eec7d7994888f22c2f4788797b85ab6bd78a60de550d204e0a6947015c0d95a0)
with one post-build change, made by PatchLC.java (ASM) in this folder. Mod id (lightmanscurrency) and version string (26.1-1.0.0.1) are unchanged.

Why: three classes in the mod's client package carry a bare @EventBusSubscriber. Its default dist is both sides, so on a dedicated server
NeoForge registered them, reflected over their client-typed event handlers and failed with
NoClassDefFoundError: net/minecraft/client/renderer/entity/RenderLayerParent (WalletClientEvents also touches ScreenEvent, EntityRenderersEvent,
KeyMapping; LCClientRecipeCache and ClientAdminMode use net.neoforged.neoforge.client.event types).

Change (the only one): @EventBusSubscriber becomes @EventBusSubscriber(value = Dist.CLIENT) on
  io/github/lightman314/lightmanscurrency/client/features/wallet/WalletClientEvents
  io/github/lightman314/lightmanscurrency/client/LCClientRecipeCache
  io/github/lightman314/lightmanscurrency/client/features/admin/ClientAdminMode
All other 5110 entries of the jar are byte-identical. Changed files per Apache-2.0 section 4(b): those three .class files.

Rebuild: javac -cp asm-9.9.jar:asm-tree-9.9.jar PatchLC.java
         java -cp asm-9.9.jar:asm-tree-9.9.jar:. PatchLC lightmanscurrency-26.1-1.0.0.1.jar lightmanscurrency-26.1-1.0.0.1-diaithir.1.jar
The upstream source is public at the link above; this patch needs nothing else.
