package net.diaithir.entityanimation.notenoughanimations.client;

final class PoseState {
    float rightArmX, leftArmX, rightArmY, leftArmY, rightArmZ, leftArmZ;
    float bodyX, headY, legSplay, breath, sneak, chew;
    float prevBodyYaw, lagYaw;
    float speed;
    boolean init;
}
