package folk.sisby.surveyor.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Collection;

public class NbtUtil {
	public static void removeRecursive(CompoundTag nbt, Collection<String> keys) {
		keys.forEach(nbt::remove);
		for (String key : nbt.keySet()) {
			if (nbt.getCompound(key).isPresent()) {
				removeRecursive(nbt.getCompound(key).get(), keys);
			} else if (nbt.getList(key).isPresent()) {
				for (Tag listNbt : nbt.getList(key).get()) {
					removeRecursive((CompoundTag) listNbt, keys);
				}
			}
		}
	}
}
