package rpggods.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import rpggods.RPGGods;
import rpggods.favor.FavorLevel;
import rpggods.favor.IFavor;
import rpggods.menu.FavorContainerMenu;

import java.util.Optional;

public class ScrollItem extends Item {

    public ScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // prevent use when there are no registered deities
        if (RPGGods.DEITY_HELPER.isEmpty()) {
            return InteractionResult.PASS;
        }
        // begin using item (to enable texture change) and open GUI
        player.startUsingItem(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            Optional<IFavor> favor = RPGGods.getFavor(player);
            if (favor.isPresent()) {
                IFavor ifavor = favor.get();
                // prevent use when favor is disabled
                if (!ifavor.isEnabled()) {
                    return InteractionResult.PASS;
                }
                // prevent use when there are no unlocked deities
                boolean unlocked = false;
                for (FavorLevel l : ifavor.getAllFavor().values()) {
                    if (l.isEnabled()) {
                        unlocked = true;
                        break;
                    }
                }
                if (!unlocked) {
                    return InteractionResult.PASS;
                }
                // open Favor GUI
                serverPlayer.openMenu(
                        new SimpleMenuProvider((id, inventory, p) ->
                                new FavorContainerMenu(id, inventory, ifavor, null),
                                Component.empty()),
                        buf -> {
                            buf.writeNbt(ifavor.serializeNBT());
                            buf.writeBoolean(false);
                        });
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack item) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack item, LivingEntity user) {
        return 72000;
    }
}
