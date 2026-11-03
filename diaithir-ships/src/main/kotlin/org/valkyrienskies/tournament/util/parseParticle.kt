package org.valkyrienskies.tournament.util

import com.mojang.brigadier.StringReader
import net.minecraft.commands.arguments.ParticleArgument
import net.minecraft.core.RegistryAccess
import net.minecraft.core.particles.ParticleOptions

fun parseParticle(access: RegistryAccess, part: String): ParticleOptions =
    ParticleArgument.readParticle(StringReader(part), access)
