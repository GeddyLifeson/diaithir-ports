package net.diaithir.claims.graves;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** What a dead player left: their items, their experience, their name and the day they died. */
public class GraveEntity extends BlockEntity {
    UUID owner;
    String ownerName = "";
    int xp;
    long died;
    final List<ItemStack> items = new ArrayList<>();

    public GraveEntity(BlockPos pos, BlockState state) {
        super(Graves.GRAVE_ENTITY.get(), pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        if (owner != null) out.store("owner", UUIDUtil.CODEC, owner);
        out.putString("name", ownerName);
        out.putInt("xp", xp);
        out.putLong("died", died);
        ValueOutput.TypedOutputList<ItemStack> list = out.list("items", ItemStack.CODEC);
        for (ItemStack s : items) list.add(s);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        owner = in.read("owner", UUIDUtil.CODEC).orElse(null);
        ownerName = in.getStringOr("name", "");
        xp = in.getIntOr("xp", 0);
        died = in.getLongOr("died", 0L);
        items.clear();
        in.listOrEmpty("items", ItemStack.CODEC).forEach(items::add);
    }
}
