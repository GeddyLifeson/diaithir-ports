package me.desht.pneumaticcraft.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Client-side helpers which common code needs to call. Methods here must only be called client-side.
 * (26.1 port: reduced to the subset needed by common code; GUI-dependent helpers are stubbed.)
 */
public class ClientUtils {
    public static void emitParticles(Level world, BlockPos pos, ParticleOptions particle, double yOffset) {
        float xOff = world.getRandom().nextFloat() * 0.6F + 0.2F;
        float zOff = world.getRandom().nextFloat() * 0.6F + 0.2F;
        getClientLevel().addParticle(particle, pos.getX() + xOff, pos.getY() + yOffset, pos.getZ() + zOff, 0, 0, 0);
    }

    public static void emitParticles(Level world, BlockPos pos, ParticleOptions particle) {
        emitParticles(world, pos, particle, 1.2);
    }

    public static ItemStack getWornArmor(EquipmentSlot slot) {
        return getClientPlayer().getItemBySlot(slot);
    }

    public static Level getClientLevel() {
        return Objects.requireNonNull(Minecraft.getInstance().level);
    }

    public static Optional<Level> getOptionalClientLevel() {
        return Optional.ofNullable(Minecraft.getInstance().level);
    }

    public static Player getClientPlayer() {
        return Objects.requireNonNull(Minecraft.getInstance().player);
    }

    public static Optional<Player> getOptionalClientPlayer() {
        return Optional.ofNullable(Minecraft.getInstance().player);
    }

    public static boolean isLocalPlayer(Entity e) {
        return e instanceof LocalPlayer;
    }

    public static boolean hasShiftDown() {
        return Minecraft.getInstance().hasShiftDown();
    }

    public static BlockEntity getBlockEntity(BlockPos pos) {
        return getClientLevel().getBlockEntity(pos);
    }

    public static int getLightAt(BlockPos pos) {
        return net.minecraft.client.renderer.LevelRenderer.getLightCoords(getClientLevel(), pos);
    }

    public static int getStringWidth(String line) {
        return Minecraft.getInstance().font.width(line);
    }

    public static boolean isGuiOpen(BlockEntity te) {
        return false; // TODO client port
    }

    public static float[] getTextureUV(BlockState state, Direction face) {
        return null; // TODO client port
    }

    public static void spawnEntityClientside(Entity e) {
        ((ClientLevel) getClientLevel()).addEntity(e);
    }

    public static String translateDirection(Direction d) {
        return net.minecraft.client.resources.language.I18n.get("pneumaticcraft.gui.tooltip.direction." + d);
    }

    public static Component translateDirectionComponent(Direction d) {
        return Component.translatable("pneumaticcraft.gui.tooltip.direction." + d);
    }

    public static void addGuiContextSensitiveTooltip(ItemStack stack, List<Component> tooltip) {
        // TODO client port: shows hints for open GUIs
    }

    public static int getRenderDistanceThresholdSq() {
        int d = Minecraft.getInstance().options.renderDistance().get() * 16;
        return d * d;
    }

    public static boolean isFirstPersonCamera() {
        return Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    public static void syncViaOpenContainerScreen(int syncId, Object value) {
        if (Minecraft.getInstance().screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen
                && screen.getMenu() instanceof me.desht.pneumaticcraft.common.inventory.AbstractPneumaticCraftMenu<?> pncMenu) {
            pncMenu.updateField(syncId, value);
        }
    }

    public static void setArmorUpgradeEnabled(EquipmentSlot slot, byte idx, boolean state) {
        // TODO client port
    }
}
