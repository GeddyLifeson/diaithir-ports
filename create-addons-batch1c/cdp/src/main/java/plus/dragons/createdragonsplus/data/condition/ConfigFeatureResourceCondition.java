/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.data.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.config.FeaturesConfig;

public record ConfigFeatureResourceCondition(Identifier feature) implements ICondition {
    public static final MapCodec<ConfigFeatureResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Identifier.CODEC.fieldOf("feature").forGetter(ConfigFeatureResourceCondition::feature))
            .apply(instance, ConfigFeatureResourceCondition::new));
    private static boolean registered;

    public static void register() {
        if (!registered) {
            registered = true;
            Registry.register(NeoForgeRegistries.CONDITION_SERIALIZERS, CreateDragonsPlus.id("config_feature"), CODEC);
        }
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(ICondition.IContext context) {
        return FeaturesConfig.isFeatureEnabled(feature);
    }
}
