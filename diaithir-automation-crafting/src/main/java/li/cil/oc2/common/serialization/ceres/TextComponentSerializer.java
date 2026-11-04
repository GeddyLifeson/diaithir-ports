/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.serialization.ceres;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import li.cil.ceres.api.DeserializationVisitor;
import li.cil.ceres.api.SerializationException;
import li.cil.ceres.api.SerializationVisitor;
import li.cil.ceres.api.Serializer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryOps;

import javax.annotation.Nullable;

public final class TextComponentSerializer implements Serializer<Component> {
    @Override
    public void serialize(final SerializationVisitor visitor, final Class<Component> type, final Object value) throws SerializationException {
        final RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.EMPTY);
        final JsonElement element = ComponentSerialization.CODEC.encodeStart(ops, (Component) value).getOrThrow(IllegalStateException::new);
        final String json = element.toString();
        visitor.putObject("value", String.class, json);
    }

    @Nullable
    @Override
    public Component deserialize(final DeserializationVisitor visitor, final Class<Component> type, @Nullable final Object value) throws SerializationException {
        if (!visitor.exists("value")) {
            return (Component) value;
        }

        final String json = (String) visitor.getObject("value", String.class, null);
        if (json == null) {
            return (Component) value;
        }

        final RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.EMPTY);
        return ComponentSerialization.CODEC.parse(ops, JsonParser.parseString(json)).getOrThrow(IllegalStateException::new);
    }
}
