package me.desht.pneumaticcraft.common.capabilities;

import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IActiveEntityHacks;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableEntity;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorRegistry;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.util.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ActiveEntityHacks implements IActiveEntityHacks, net.neoforged.neoforge.common.util.ValueIOSerializable {
    private final Map<Identifier, IHackableEntity<? extends Entity>> hacks = new HashMap<>();

    @Override
    public void tick(Entity entity) {
        hacks.values().removeIf(hackable -> !hackable._afterHackTick(entity));
    }

    @Override
    public void addHackable(IHackableEntity<?> hackable) {
        hacks.put(hackable.getHackableId(), hackable);
    }

    @Override
    public Collection<IHackableEntity<?>> getCurrentHacks() {
        return hacks.values();
    }

    @Override
    public void clear() {
        hacks.clear();
    }

    @Override
    public void serialize(net.minecraft.world.level.storage.ValueOutput output) {
        if (getCurrentHacks().isEmpty()) return;
        var list = output.childrenList("hackables");
        for (IHackableEntity<?> hackableEntity : getCurrentHacks()) {
            list.addChild().putString("id", hackableEntity.getHackableId().toString());
        }
    }

    @Override
    public void deserialize(net.minecraft.world.level.storage.ValueInput input) {
        clear();
        input.childrenListOrEmpty("hackables").stream().forEach(child -> {
            String id = child.getStringOr("id", "");
            Identifier.read(id).result().ifPresentOrElse(hackableId -> {
                CommonArmorRegistry.getInstance().getHackableEntityForId(hackableId).ifPresentOrElse(
                        this::addHackable,
                        () -> Log.error("entity-hackable '{}' not found when deserializing IHacking capability?", hackableId)
                );
            }, () -> Log.error("invalid hackable id '{}': not a resource location", id));
        });
    }
}
