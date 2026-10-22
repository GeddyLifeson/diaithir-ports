package dev.tocraft.remorphed.neoforge;

import dev.tocraft.craftedcore.permission.neoforge.PermissionCheckerImpl;
import dev.tocraft.remorphed.Remorphed;
import dev.tocraft.remorphed.permission.PermissionManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Mod(Remorphed.MODID)
public class RemorphedNeoForge {
    public RemorphedNeoForge() {
        NeoForge.EVENT_BUS.addListener(PermissionGatherEvent.Nodes.class, RemorphedNeoForge::registerNodesEvent);
        new Remorphed().initialize();
    }

    public static void registerNodesEvent(PermissionGatherEvent.@NotNull Nodes event) {
        List<PermissionNode<@NotNull Boolean>> nodes = new ArrayList<>();

        // Core permissions
        nodes.add(node("morph"));
        nodes.add(node("bypass.lock"));

        // Command permissions (basic)
        nodes.add(node("command.addShape"));
        nodes.add(node("command.removeShape"));
        nodes.add(node("command.clearShapes"));
        nodes.add(node("command.hasShape"));
        nodes.add(node("command.addSkin"));
        nodes.add(node("command.removeSkin"));
        nodes.add(node("command.clearSkins"));
        nodes.add(node("command.hasSkin"));

        // Command permissions (.self variants)
        nodes.add(node("command.addShape.self"));
        nodes.add(node("command.removeShape.self"));
        nodes.add(node("command.clearShapes.self"));
        nodes.add(node("command.hasShape.self"));
        nodes.add(node("command.addSkin.self"));
        nodes.add(node("command.removeSkin.self"));
        nodes.add(node("command.clearSkins.self"));
        nodes.add(node("command.hasSkin.self"));

        // Command permissions (.others variants)
        nodes.add(node("command.addShape.others"));
        nodes.add(node("command.removeShape.others"));
        nodes.add(node("command.clearShapes.others"));
        nodes.add(node("command.hasShape.others"));
        nodes.add(node("command.addSkin.others"));
        nodes.add(node("command.removeSkin.others"));
        nodes.add(node("command.clearSkins.others"));
        nodes.add(node("command.hasSkin.others"));

        // Entity type permissions for all registered entities
        BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
            Identifier key = EntityType.getKey(entityType);
            nodes.add(node("type." + key));
        });

        // register all
        event.addNodes(nodes.toArray(PermissionNode[]::new));
    }

    private static PermissionNode<@NotNull Boolean> node(String permission) {
        return PermissionCheckerImpl.createNode(Remorphed.MODID, PermissionManager.node(permission));
    }
}
