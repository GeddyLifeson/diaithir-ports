#!/usr/bin/env python3
"""Per-file manual fixes applied after particle_port.py (idempotent when run after a fresh port)."""
import pathlib,re
D=pathlib.Path('src/main/java/com/github/alexmodguy/alexscaves/client/particle')
def edit(name, *pairs, regex=False):
    p=D/name; s=p.read_text()
    for a,b in pairs:
        if regex: s2=re.sub(a,b,s,flags=re.S)
        else:
            if a not in s: print('!! missing in',name,':',a[:60]); continue
            s2=s.replace(a,b)
        s=s2
    p.write_text(s)
edit('RainbowParticle.java',
 ('import net.minecraft.core.particles.ParticleGroup;','import net.minecraft.core.particles.ParticleLimit;'),
 ('public static final ParticleGroup PARTICLE_GROUP = new ParticleGroup(100);','public static final ParticleLimit PARTICLE_LIMIT = new ParticleLimit(100);'),
 ('public Optional<ParticleGroup> getParticleGroup() {\n        return Optional.of(PARTICLE_GROUP);','public Optional<ParticleLimit> getParticleLimit() {\n        return Optional.of(PARTICLE_LIMIT);'))
edit('PlayerRainbowParticle.java',('import net.minecraft.core.particles.ParticleGroup;\n',''))
edit('WatcherAppearanceParticle.java',
 ('        float fogBefore = RenderSystem.getShaderFogEnd();\n        RenderSystem.setShaderFogEnd(40);\n',''),
 ('        RenderSystem.setShaderFogEnd(fogBefore);\n',''))
edit('BigSplashEffectParticle.java',
 ('extends WaterDropParticle','extends ACSpriteParticle'),
 ('import net.minecraft.client.particle.WaterDropParticle;\n',''),
 ('        super(level, x, y, z);\n','        super(level, x, y, z);\n        this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F;\n        this.friction = 0.98F;\n'),
 ('    public static class Factory','''    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
            return;
        }
        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        if (this.onGround) {
            if (Math.random() < 0.5D) this.remove();
            this.xd *= 0.7F;
            this.zd *= 0.7F;
        }
    }

    public static class Factory'''))
edit('JellyBeanEatParticle.java',
 ('extends BreakingItemParticle','extends BreakingItemParticle'),
 ('ItemStack stack) {\n        super(clientLevel, x, y, z, stack);','ItemStack stack, net.minecraft.client.renderer.texture.TextureAtlasSprite sprite) {\n        super(clientLevel, x, y, z, sprite);'),
 ('        int colorizer = Minecraft.getInstance().getItemColors().getColor(stack, 0);\n        if(stack.getItem() == ACItemRegistry.BIOME_TREAT.get()){','        int colorizer = -1;\n        if(stack.getItem() == ACItemRegistry.JELLY_BEAN.get()){\n            colorizer = com.github.alexmodguy.alexscaves.server.item.JellyBeanItem.getBeanColor(stack);\n        }\n        if(stack.getItem() == ACItemRegistry.BIOME_TREAT.get()){'),
 ('public static class Factory implements ParticleProvider<ItemParticleOption> {','public static class Factory extends BreakingItemParticle.ItemParticleProvider<ItemParticleOption> {'),
 ('return new JellyBeanEatParticle(clientLevel, x, y, z, xd, yd, zd, itemParticleOption.getItem());','return new JellyBeanEatParticle(clientLevel, x, y, z, xd, yd, zd, itemParticleOption.getItem().create(), this.getSprite(itemParticleOption.getItem(), clientLevel, randomSource));'))
edit('WaterTremorParticle.java',
 ('extends SimpleAnimatedParticle','extends LegacyParticle'),
 ('    protected WaterTremorParticle(ClientLevel level, double x, double y, double z, SpriteSet set) {\n        super(level, x, y, z, set, 0);','    private final SpriteSet sprites;\n\n    protected WaterTremorParticle(ClientLevel level, double x, double y, double z, SpriteSet set) {\n        super(level, x, y, z);\n        this.sprites = set;'),
 ('        super.tick();\n        BlockPos slightlyAbove','        super.tick();\n        this.setSpriteFromAge(sprites);\n        BlockPos slightlyAbove'))
edit('VoidBeingCloudParticle.java',
 ('package com.github.alexmodguy.alexscaves.client.particle;','package com.github.alexmodguy.alexscaves.client.particle;\n\nimport com.github.alexmodguy.alexscaves.AlexsCaves;'),
 ('new DynamicTexture(textureSize, textureSize, true)','new DynamicTexture("alexscaves void cloud", textureSize, textureSize, true)'),
 ('Minecraft.getInstance().getTextureManager().register("alexscavesvoid_particle/void_cloud_" + id, dynamicTexture)','Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "void_particle/void_cloud_" + id)'),
 ('        currentlyUsedTextures++;','        Minecraft.getInstance().getTextureManager().register(resourcelocation, dynamicTexture);\n        currentlyUsedTextures++;'),
 ('getPixels().setPixelRGBA(','getPixels().setPixel('))
