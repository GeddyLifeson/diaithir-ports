package opekope2.optigui.internal.selector

import net.neoforged.fml.ModList
import org.apache.maven.artifact.versioning.ArtifactVersion
import org.apache.maven.artifact.versioning.DefaultArtifactVersion
import opekope2.optigui.internal.util.*
import kotlin.jvm.optionals.getOrNull

internal class ConditionalLoadSelector : (String) -> Boolean {
    override fun invoke(selectorValue: String) = selectorValue.toBooleanStrict()
}

internal class ModsLoadSelector : (String) -> Boolean {
    override fun invoke(selectorValue: String): Boolean {
        val modCheckResults = selectorValue.splitIgnoreEmpty(*delimiters)
            ?.assertNotEmpty()
            ?.mapNotNull(::parseVersion) {
                throw RuntimeException("Invalid mod predicates: ${joinNotFound(it)}")
            }
            ?.assertNotEmpty()
            ?.map { (modId, versionPredicate) ->
                ModList.get().getModContainerById(modId).getOrNull()?.modInfo?.version?.let(versionPredicate)
                    ?: false
            }
            ?: return false

        return modCheckResults.all { it }
    }

    private fun parseVersion(modWithVersion: String): Pair<String, (ArtifactVersion) -> Boolean>? {
        return try {
            val versionIndex = modWithVersion.indexOfAny(charArrayOf('>', '<', '=', '^', '~'))
            if (versionIndex < 0) {
                return modWithVersion to { true }
            }

            val modId = modWithVersion.substring(0 until versionIndex)
            val modVersion = modWithVersion.substring(versionIndex).ifEmpty { return modId to { true } }
            val operator = modVersion.takeWhile { it in "<>=^~" }
            val required = DefaultArtifactVersion(modVersion.substring(operator.length).trim())

            modId to { actual: ArtifactVersion ->
                val cmp = actual.compareTo(required)
                when (operator) {
                    ">=" -> cmp >= 0
                    "<=" -> cmp <= 0
                    ">" -> cmp > 0
                    "<" -> cmp < 0
                    "=", "==" -> cmp == 0
                    // Caret and tilde: same major (caret) or same major and minor (tilde), at least the given version
                    "^" -> cmp >= 0 && actual.majorVersion == required.majorVersion
                    "~" -> cmp >= 0 && actual.majorVersion == required.majorVersion &&
                            actual.minorVersion == required.minorVersion
                    else -> throw IllegalArgumentException("Unknown version operator `$operator`")
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
