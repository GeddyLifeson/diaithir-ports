// Ported from Structure Compass (MIT) https://github.com/Mrbysco/StructureCompass
// Modifications: Renamed from Reference, mod id switched to diaithirstructurenavigation.
package net.diaithir.structurenavigation.structurecompass;

import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import org.slf4j.Logger;

public final class CompassIds {
    public static final String MOD_ID = "diaithirstructurenavigation";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static Identifier modLoc(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static final StreamCodec<ByteBuf, InteractionHand> INTERACTION_HAND = ByteBufCodecs.BOOL.map(
            bool -> bool ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
            hand -> hand == InteractionHand.MAIN_HAND
    );

    private CompassIds() {}
}
