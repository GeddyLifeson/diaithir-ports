package net.diaithir.furnishings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Holds up to two sign arms. Each arm remembers the sign item it was made from, a heading in eighths of a turn, and two lines of text. */
public final class SignpostEntity extends BlockEntity {
    public static final int ARMS = 2;
    public static final int DIRECTIONS = 8;
    public static final int MAX_LINE = 16;

    /** dir 0 points south and turns clockwise seen from above (west is 2). */
    public record Arm(int slot, String sign, int dir, String line1, String line2) {
        public static final Codec<Arm> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("slot").forGetter(Arm::slot),
            Codec.STRING.fieldOf("sign").forGetter(Arm::sign),
            Codec.INT.fieldOf("dir").forGetter(Arm::dir),
            Codec.STRING.optionalFieldOf("line1", "").forGetter(Arm::line1),
            Codec.STRING.optionalFieldOf("line2", "").forGetter(Arm::line2)).apply(i, Arm::new));

        public Arm withDir(int newDir) {
            return new Arm(slot, sign, Math.floorMod(newDir, DIRECTIONS), line1, line2);
        }

        public Arm withText(String a, String b) {
            return new Arm(slot, sign, dir, clean(a), clean(b));
        }

        /** The wood name for the arm's board, taken from the sign item's id ("oak_sign" is "oak"). */
        public String wood() {
            String path = Identifier.parse(sign).getPath();
            return path.endsWith("_sign") ? path.substring(0, path.length() - 5) : path;
        }

        public ItemStack signStack() {
            Item item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(sign)).orElse(net.minecraft.world.item.Items.OAK_SIGN);
            return new ItemStack(item);
        }
    }

    private final Arm[] arms = new Arm[ARMS];

    public SignpostEntity(BlockPos pos, BlockState state) {
        super(DiaithirFurnishings.SIGNPOST_ENTITY.get(), pos, state);
    }

    public static String clean(String text) {
        String t = text == null ? "" : text.replaceAll("\\p{Cntrl}", "");
        return t.length() > MAX_LINE ? t.substring(0, MAX_LINE) : t;
    }

    public Arm arm(int slot) {
        return slot >= 0 && slot < ARMS ? arms[slot] : null;
    }

    public int freeSlot() {
        for (int i = 0; i < ARMS; i++) {
            if (arms[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public void setArm(int slot, Arm arm) {
        arms[slot] = arm;
        changed();
    }

    public boolean isEmpty() {
        return arms[0] == null && arms[1] == null;
    }

    private void changed() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        List<Arm> list = new ArrayList<>();
        for (Arm a : arms) {
            if (a != null) {
                list.add(a);
            }
        }
        output.store("Arms", Arm.CODEC.listOf(), list);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        arms[0] = null;
        arms[1] = null;
        Optional<List<Arm>> list = input.read("Arms", Arm.CODEC.listOf());
        list.ifPresent(l -> {
            for (Arm a : l) {
                if (a.slot() >= 0 && a.slot() < ARMS) {
                    arms[a.slot()] = a.withDir(a.dir()).withText(a.line1(), a.line2());
                }
            }
        });
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    /** Breaking the post gives the signs back. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null) {
            for (int i = 0; i < ARMS; i++) {
                if (arms[i] != null) {
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, arms[i].signStack());
                    arms[i] = null;
                }
            }
        }
    }
}
