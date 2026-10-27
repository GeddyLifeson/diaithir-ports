package net.diaithir.butchery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

/** The carcass on a hook and how far it is worked: 0 whole, 1 skinned, 2 gutted; cutting empties the hook. */
public class MeatHookEntity extends BlockEntity {
    private ItemStack carcass = ItemStack.EMPTY;
    private int stage;

    public MeatHookEntity(BlockPos pos, BlockState state) {
        super(Butchery.HOOK_ENTITY.get(), pos, state);
    }

    boolean isEmpty() {
        return carcass.isEmpty();
    }

    int stage() {
        return stage;
    }

    void hang(ItemStack one) {
        carcass = one;
        stage = 0;
        setChanged();
    }

    ItemStack takeDown() {
        ItemStack out = carcass;
        carcass = ItemStack.EMPTY;
        setChanged();
        return out;
    }

    /** One knife stroke: yields for the current stage, skipping stages the body class has nothing for. Returns whether work was done. */
    boolean work(ServerLevel level, BlockPos pos, Player player) {
        String body = carcass.getOrDefault(Butchery.BODY.get(), "");
        if (!ButcheryData.known(body)) {
            player.sendOverlayMessage(Component.translatable("message.diaithirbutchery.unknown"));
            return false;
        }
        while (stage < 3 && ButcheryData.yield(body, stage).isEmpty()) stage++;
        if (stage >= 3) {
            carcass = ItemStack.EMPTY;
            setChanged();
            return false;
        }
        String species = carcass.getOrDefault(Butchery.SPECIES.get(), "");
        long killed = carcass.getOrDefault(Butchery.KILLED.get(), level.getGameTime());
        RandomSource rng = level.getRandom();
        for (ButcheryData.Yield y : ButcheryData.yield(body, stage)) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(y.item()));
            int n = y.min() + (y.max() > y.min() ? rng.nextInt(y.max() - y.min() + 1) : 0);
            if (n <= 0 || item == null || item == net.minecraft.world.item.Items.AIR) continue;
            ItemStack out = new ItemStack(item, n);
            if (!species.isEmpty()) {
                out.set(Butchery.SPECIES.get(), species);
                out.set(Butchery.KILLED.get(), killed);
            }
            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() - 0.3, pos.getZ() + 0.5, out);
            level.addFreshEntity(drop);
        }
        level.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1.0F, 0.9F);
        stage++;
        while (stage < 3 && ButcheryData.yield(body, stage).isEmpty()) stage++;
        if (stage >= 3) carcass = ItemStack.EMPTY;
        setChanged();
        return true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("carcass", ItemStack.OPTIONAL_CODEC, carcass);
        output.putInt("stage", stage);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        carcass = input.read("carcass", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        stage = input.getIntOr("stage", 0);
    }

    List<ItemStack> contents() {
        return carcass.isEmpty() ? List.of() : List.of(carcass);
    }
}
