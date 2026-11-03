package net.diaithir.lookat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

/** The lines shown for a target. Pure string work so it can be tested without a client. */
public final class Info {
    private Info() {}

    public static String modName(String namespace) {
        return namespace.equals("minecraft") ? "Minecraft" : namespace;
    }

    public static String[] forBlock(BlockState state, String blockName, String namespace) {
        return new String[] {blockName, modName(namespace)};
    }

    public static String[] forEntity(Entity e, String namespace) {
        String name = e.getName().getString();
        if (e instanceof LivingEntity l) {
            String hp = String.format("%.1f / %.1f", l.getHealth(), l.getMaxHealth());
            return new String[] {name, hp, modName(namespace)};
        }
        return new String[] {name, modName(namespace)};
    }
}
