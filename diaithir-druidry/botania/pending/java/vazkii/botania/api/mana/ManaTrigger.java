/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.mana;

import net.minecraft.resources.Identifier;

import vazkii.botania.api.capability.BlockApiNoContext;
import vazkii.botania.api.internal.ManaBurst;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

/**
 * A Block or Block Entity with this capability will receive a callback when a burst
 * collides with it.
 */
public interface ManaTrigger {

	Identifier ID = botaniaRL("mana_trigger");
	BlockApiNoContext<ManaTrigger> LOOKUP = new BlockApiNoContext<>(ID, ManaTrigger.class);

	void onBurstCollision(ManaBurst burst);

}
