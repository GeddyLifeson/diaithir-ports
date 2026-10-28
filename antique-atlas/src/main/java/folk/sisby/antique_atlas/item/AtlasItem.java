package folk.sisby.antique_atlas.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * The atlas item. Using it opens the atlas screen on the client.
 * The client entry point sets {@link #OPENER}; on a dedicated server it stays a no-op.
 */
public class AtlasItem extends Item {
	public static Runnable OPENER = () -> {
	};

	public AtlasItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide()) OPENER.run();
		return InteractionResult.SUCCESS;
	}
}
