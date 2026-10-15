package mcjty.rftoolsbase.keys;

import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;


public class KeyBindings {

    public static KeyMapping porterNextDestination;
    public static KeyMapping porterPrevDestination;

    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("rftoolsbase", "rftools"));

    public static void init(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        porterNextDestination = new KeyMapping("key.nextDestination", KeyConflictContext.IN_GAME, InputConstants.getKey("key.keyboard.right.bracket"), CATEGORY);
        event.register(porterNextDestination);
        porterPrevDestination = new KeyMapping("key.prevDestination", KeyConflictContext.IN_GAME, InputConstants.getKey("key.keyboard.left.bracket"), CATEGORY);
        event.register(porterPrevDestination);
//        debugDumpNBTItem = new KeyBinding("key.debugDumpNBTItem", KeyConflictContext.IN_GAME, Keyboard.KEY_NONE, CATEGORY);
//        ClientRegistry.registerKeyBinding(debugDumpNBTItem);
//        debugDumpNBTBlock = new KeyBinding("key.debugDumpNBTBlock", KeyConflictContext.IN_GAME, Keyboard.KEY_NONE, CATEGORY);
//        ClientRegistry.registerKeyBinding(debugDumpNBTBlock);
    }
}
