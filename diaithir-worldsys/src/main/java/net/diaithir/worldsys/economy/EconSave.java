package net.diaithir.worldsys.economy;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** The saved economy: EconState as one JSON string in the overworld's data storage. */
public final class EconSave extends SavedData {
    private static final Codec<EconSave> CODEC = Codec.STRING.xmap(EconSave::new, s -> EconJson.toJson(s.state));
    public static final SavedDataType<EconSave> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath("diaithirworldsys", "economy"), EconSave::new, CODEC, null);

    public final EconState state;

    public EconSave() { this.state = new EconState(); }

    private EconSave(String json) {
        this.state = EconJson.fromJson(json);
    }

    public static EconSave get(ServerLevel overworld) { return overworld.getDataStorage().computeIfAbsent(TYPE); }

}
