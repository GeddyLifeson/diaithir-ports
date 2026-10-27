package cn.leolezury.eternalstarlight.common.client.book.component;

import net.minecraft.resources.Identifier;

import java.util.HashSet;

public interface BookComponentConfig {
	HashSet<HashSet<Identifier>> unlockConditions();

	Identifier id();
}
