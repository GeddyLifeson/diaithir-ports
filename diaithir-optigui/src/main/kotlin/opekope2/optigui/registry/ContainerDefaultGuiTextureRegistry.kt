@file: JvmName("ContainerTexturePath")

package opekope2.optigui.registry

import net.neoforged.fml.ModList
import net.neoforged.neoforgespi.language.IModInfo
import net.minecraft.resources.Identifier

/**
 * Texture paths for Minecraft containers.
 *
 * To register your mod's containers' default GUI textures, add entries to your `[modproperties.<modid>]` table in
 * `neoforge.mods.toml`. Each key is `optigui:container_default_gui_textures:` followed by the container ID, and the
 * value is the default GUI texture, for example
 * `"optigui:container_default_gui_textures:mymod:crate" = "mymod:textures/gui/crate.png"`.
 */
object ContainerDefaultGuiTextureRegistry {
    private const val CONTAINER_DEFAULT_TEXTURES_KEY = "optigui:container_default_gui_textures"

    private lateinit var containerTextures: MutableMap<Identifier, Identifier>

    private var containerTexturesInitialized = false

    internal fun onInitializeClient() {
        containerTextures = mutableMapOf()
        containerTexturesInitialized = true
        for (mod in ModList.get().mods) {
            try {
                loadModTextures(mod)
            } catch (e: Exception) {
                throw RuntimeException("Failed to load container default GUI textures from mod `${mod.modId}`", e)
            }
        }
    }

    private fun loadModTextures(mod: IModInfo) {
        val prefix = "$CONTAINER_DEFAULT_TEXTURES_KEY:"

        for ((property, value) in mod.modProperties) {
            if (!property.startsWith(prefix)) continue
            val key = property.substring(prefix.length)

            val keyId = Identifier.parse(key)
            val valueId = Identifier.parse(value.toString())

            if (keyId in containerTextures) {
                throw RuntimeException("A default GUI texture for container `$key` has already been registered")
            }

            containerTextures[keyId] = valueId
        }
    }

    /**
     * Checks if the default GUI texture of a container is known.
     *
     * @param container The id of the entity or block entity
     */
    @JvmStatic
    operator fun contains(container: Identifier) = containerTexturesInitialized && container in containerTextures

    /**
     * Gets the default GUI texture of the given container, or `null`, if it's unknown.
     *
     * @param container The id of the entity or block entity to get its GUI texture
     */
    @JvmStatic
    operator fun get(container: Identifier) = if (containerTexturesInitialized) containerTextures[container] else null
}
