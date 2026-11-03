package net.diaithir.druidry.ars;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.world.chunk.TicketController;

import java.util.ArrayList;
import java.util.List;

/** Port stub of the Ars Nouveau main class: constants and a minimal side proxy. */
public class ArsNouveau {
    public static final String MODID = "ars_nouveau";

    public static final TicketController ticketController = new TicketController(prefix("chunk_ticket"));

    public static final List<String> postLoadWarnings = new ArrayList<>();

    /** Replaced by the client setup on the physical client. */
    public static IProxy proxy = new IProxy() {
    };

    public static Identifier prefix(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public interface IProxy {
        default boolean isClientSide() {
            return false;
        }

        default Player getPlayer() {
            return null;
        }

        default Level getClientWorld() {
            return null;
        }

        default Minecraft getMinecraft() {
            return null;
        }
    }
}
