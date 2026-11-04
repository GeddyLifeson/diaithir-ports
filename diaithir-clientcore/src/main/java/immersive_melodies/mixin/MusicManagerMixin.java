package immersive_melodies.mixin;

import immersive_melodies.client.sound.MusicSuppression;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public class MusicManagerMixin {
    @Inject(method = "startPlaying", at = @At("HEAD"), cancellable = true)
    private void immersive_melodies$preventStartWhilePlaying(Music music, CallbackInfo ci) {
        if (MusicSuppression.isActive()) {
            ci.cancel();
        }
    }
}
