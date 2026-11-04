package net.diaithir.computing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The files on a disk: named text in the disk item's custom data. Limits: 16 files, 8000 characters each, 32000 in all. */
final class Disks {
    static final int MAX_FILES = 16, MAX_FILE = 8000, MAX_TOTAL = 32_000;

    private Disks() {}

    private static Varargs fail(String why) { return LuaValue.varargsOf(LuaValue.FALSE, LuaValue.valueOf(why)); }

    static CompoundTag files(ItemStack disk) {
        CustomData d = disk.get(DataComponents.CUSTOM_DATA);
        return d == null ? new CompoundTag() : d.copyTag().getCompoundOrEmpty("Files");
    }

    static void setFiles(ItemStack disk, CompoundTag files) {
        CompoundTag t = new CompoundTag();
        t.put("Files", files);
        disk.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
    }

    static int total(CompoundTag files) {
        int n = 0;
        for (String k : files.keySet()) n += files.getStringOr(k, "").length();
        return n;
    }

    /** y = disk, operation, side index, name, text. */
    static Varargs op(Level level, BlockPos from, Varargs y) {
        String op = y.arg(2).tojstring();
        Direction d = Direction.from3DDataValue(y.arg(3).checkint());
        if (!(level.getBlockEntity(from.relative(d)) instanceof DriveEntity drive)) return op.equals("present") ? LuaValue.FALSE : fail("no disk drive on that side");
        ItemStack disk = drive.disk;
        if (op.equals("present")) return LuaValue.valueOf(!disk.isEmpty());
        if (disk.isEmpty()) return fail("no disk in the drive");
        CompoundTag files = files(disk);
        String name = y.arg(4).isnil() ? "" : y.arg(4).tojstring();
        switch (op) {
            case "list": {
                List<String> names = new ArrayList<>(files.keySet());
                Collections.sort(names);
                LuaTable t = new LuaTable();
                for (int i = 0; i < names.size(); i++) t.set(i + 1, names.get(i));
                return t;
            }
            case "free": return LuaValue.valueOf(MAX_TOTAL - total(files));
            case "read": return files.contains(name) ? LuaValue.valueOf(files.getStringOr(name, "")) : LuaValue.NIL;
            case "delete": {
                if (!files.contains(name)) return fail("no such file");
                files.remove(name);
                setFiles(disk, files);
                drive.setChanged();
                return LuaValue.TRUE;
            }
            case "write": {
                if (!name.matches("[A-Za-z0-9_.-]{1,32}")) return fail("file names are 1 to 32 letters, digits, _ . -");
                String text = y.arg(5).isnil() ? "" : y.arg(5).tojstring();
                if (text.length() > MAX_FILE) return fail("a file holds at most " + MAX_FILE + " characters");
                if (!files.contains(name) && files.keySet().size() >= MAX_FILES) return fail("the disk holds at most " + MAX_FILES + " files");
                int old = files.getStringOr(name, "").length();
                if (total(files) - old + text.length() > MAX_TOTAL) return fail("the disk is full");
                files.putString(name, text);
                setFiles(disk, files);
                drive.setChanged();
                return LuaValue.TRUE;
            }
            default: return fail("unknown disk operation " + op);
        }
    }
}
