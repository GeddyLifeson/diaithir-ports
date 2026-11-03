package net.diaithir.druidry.ars.common.camera;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.common.block.ScryerCrystal;
import net.diaithir.druidry.ars.common.entity.ScryerCamera;
import net.diaithir.druidry.ars.common.network.Networking;
import net.diaithir.druidry.ars.common.network.PacketDismountCamera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = ArsNouveau.MODID, value = Dist.CLIENT)
public class CameraController {
    public static CameraType previousCameraType;
    public static boolean resetOverlaysAfterDismount = false;
    private static float lastSentYRot;
    private static float lastSentXRot;
    private static boolean wasUpPressed;
    private static boolean wasDownPressed;
    private static boolean wasLeftPressed;
    private static boolean wasRightPressed;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();

        if (cameraEntity instanceof ScryerCamera cam) {
            Options options = Minecraft.getInstance().options;

            //up/down/left/right handling is split to prevent players who are viewing a camera from moving around in a boat or on a horse
            if (event instanceof ClientTickEvent.Pre) {
                if (wasUpPressed = options.keyUp.isDown())
                    options.keyUp.setDown(false);

                if (wasDownPressed = options.keyDown.isDown())
                    options.keyDown.setDown(false);

                if (wasLeftPressed = options.keyLeft.isDown())
                    options.keyLeft.setDown(false);

                if (wasRightPressed = options.keyRight.isDown())
                    options.keyRight.setDown(false);

                if (options.keyShift.isDown()) {
                    dismount();
                    options.keyShift.setDown(false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();

        if (cameraEntity instanceof ScryerCamera cam) {
            Options options = Minecraft.getInstance().options;

            //up/down/left/right handling is split to prevent players who are viewing a camera from moving around in a boat or on a horse
            if (event instanceof ClientTickEvent.Post) {
                if (wasUpPressed) {
                    moveViewUp(cam);
                    options.keyUp.setDown(true);
                }

                if (wasDownPressed) {
                    moveViewDown(cam);
                    options.keyDown.setDown(true);
                }

                if (wasLeftPressed) {
                    moveViewHorizontally(cam, cam.getYRot(), cam.getYRot() - (float) cam.cameraSpeed * cam.zoomAmount);
                    options.keyLeft.setDown(true);
                }

                if (wasRightPressed) {
                    moveViewHorizontally(cam, cam.getYRot(), cam.getYRot() + (float) cam.cameraSpeed * cam.zoomAmount);
                    options.keyRight.setDown(true);
                }

                //update other players with the head rotation
                LocalPlayer player = Minecraft.getInstance().player;
                double yRotChange = player.getYRot() - lastSentYRot;
                double xRotChange = player.getXRot() - lastSentXRot;

                if (yRotChange != 0.0D || xRotChange != 0.0D) {
                    lastSentYRot = player.getYRot();
                    lastSentXRot = player.getXRot();
                    player.connection.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround(), player.horizontalCollision));
                }
            }
        }
    }

    private static void dismount() {
        Networking.sendToServer(new PacketDismountCamera());
    }

    public static void moveViewUp(ScryerCamera cam) {
        float next = cam.getXRot() - (float) cam.cameraSpeed * cam.zoomAmount;

        if (cam.isCameraDown()) {
            if (next > 40F)
                cam.setRotation(cam.getYRot(), next);
        } else if (next > -25F)
            cam.setRotation(cam.getYRot(), next);
    }

    public static void moveViewDown(ScryerCamera cam) {
        float next = cam.getXRot() + (float) cam.cameraSpeed * cam.zoomAmount;

        if (cam.isCameraDown()) {
            if (next < 90F)
                cam.setRotation(cam.getYRot(), next);
        } else if (next < 60F)
            cam.setRotation(cam.getYRot(), next);
    }

    public static void moveViewHorizontally(ScryerCamera cam, float yRot, float next) {
        BlockState state = cam.level().getBlockState(cam.blockPosition());

        if (state.hasProperty(ScryerCrystal.FACING)) {
            float checkNext = next;

            if (checkNext < 0)
                checkNext += 360;

            boolean shouldSetRotation = switch (state.getValue(ScryerCrystal.FACING)) {
                case NORTH -> checkNext > 90F && checkNext < 270F;
                case SOUTH -> checkNext > 270F || checkNext < 90F;
                case EAST -> checkNext > 180F && checkNext < 360F;
                case WEST -> checkNext > 0F && checkNext < 180F;
                case DOWN -> true;
                default -> false;
            };

            if (shouldSetRotation)
                cam.setYRot(next);
        }
    }

    /**
     * The client chunk-storage swap used to render chunks around the camera relied on private ClientChunkCache internals
     * (and a mixin) that do not exist in 26.1, so this is a no-op in the port.
     */
    public static void setRenderPosition(Entity entity) {
    }
}
