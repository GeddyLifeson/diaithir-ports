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
    private long hungAt;

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
        hungAt = level != null ? level.getGameTime() : 0;
        setChanged();
    }

    ItemStack takeDown() {
        ItemStack out = carcass;
        carcass = ItemStack.EMPTY;
        setChanged();
        return out;
    }

    /** One knife stroke: yields for the current stage, skipping stages the body class has nothing for. Returns whether work was done. */
    boolean work(ServerLevel level, BlockPos pos, Player player, ItemStack tool) {
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
        dropStage(level, pos, carcass, body, stage, tool, level.getGameTime() - hungAt);
        level.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1.0F, 0.9F);
        stage++;
        while (stage < 3 && ButcheryData.yield(body, stage).isEmpty()) stage++;
        if (stage >= 3) carcass = ItemStack.EMPTY;
        setChanged();
        return true;
    }


    /** Drops one stage of a carcass at the block: yields from the body class, a bleeding scale on blood, tool bonus and the cleaver's cutting bonus. */
    static void dropStage(ServerLevel level, BlockPos pos, ItemStack carcass, String body, int stage, ItemStack tool, long hungFor) {
        String species = carcass.getOrDefault(Butchery.SPECIES.get(), "");
        long killed = carcass.getOrDefault(Butchery.KILLED.get(), level.getGameTime());
        RandomSource rng = level.getRandom();
        int bonus = ButcheryData.bonusOf(BuiltInRegistries.ITEM.getKey(tool.getItem()).toString());
        for (ButcheryData.Yield y : ButcheryData.yield(body, stage)) {
            Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(y.item()));
            int n = y.min() + (y.max() > y.min() ? rng.nextInt(y.max() - y.min() + 1) : 0);
            // a carcass bleeds out while it hangs: a full flask takes a minute, a fresh one gives a fraction
            if (y.item().endsWith(":blood_flask")) n = Math.max(n > 0 ? 1 : 0, (int) (n * Math.min(1.0, hungFor / 1200.0)));
            // a better tool takes more off the carcass; the fraction is a chance, so a +15% knife adds one in about seven pieces
            n += n * bonus / 100 + (rng.nextInt(100) < n * bonus % 100 ? 1 : 0);
            // a cleaver takes a quarter more off the bone
            if (stage == 2 && tool.is(Butchery.CLEAVER.get())) n += n / 4;
            if (n <= 0 || item == null || item == net.minecraft.world.item.Items.AIR) continue;
            ItemStack out = new ItemStack(item, n);
            if (!species.isEmpty()) {
                out.set(Butchery.SPECIES.get(), species);
                out.set(Butchery.KILLED.get(), killed);
            }
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, out));
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("carcass", ItemStack.OPTIONAL_CODEC, carcass);
        output.putInt("stage", stage);
        output.putLong("hungAt", hungAt);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        carcass = input.read("carcass", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        stage = input.getIntOr("stage", 0);
        hungAt = input.getLongOr("hungAt", 0L);
    }

    List<ItemStack> contents() {
        return carcass.isEmpty() ? List.of() : List.of(carcass);
    }
}
