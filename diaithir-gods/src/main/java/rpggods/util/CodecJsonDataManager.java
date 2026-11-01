/*

The MIT License (MIT)

Copyright (c) 2020 Joseph Bettendorff a.k.a. "Commoble"

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

 */

package rpggods.util;

import com.mojang.serialization.Codec;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Codec-based data manager for loading data.
 * Files that fail to decode are logged and skipped by the vanilla base class.
 * Reworked from the original by Commoble for NeoForge 26.1, where the base class
 * decodes the JSON itself. Syncing to clients is done in {@link rpggods.RGData}.
 * @param <T> The type of the objects that the codec is parsing jsons as
 */
public class CodecJsonDataManager<T> extends SimpleJsonResourceReloadListener<T>
{
    private static final Logger LOGGER = LoggerFactory.getLogger("diaithir_gods");

    private final String folderName;

    /** Called on the server after each reload with the freshly loaded data **/
    private final Consumer<Map<Identifier, T>> onLoaded;

    /** The raw data that we parsed from json last time resources were reloaded **/
    protected Map<Identifier, T> data = new HashMap<>();

    /**
     * @param folderName The name of the data folder that we will load from, under data/namespace/
     * @param codec A codec to deserialize the json into your T
     * @param onLoaded called on the server thread with the loaded data after every reload
     */
    public CodecJsonDataManager(String folderName, Codec<T> codec, Consumer<Map<Identifier, T>> onLoaded)
    {
        super(codec, FileToIdConverter.json(folderName));
        this.folderName = folderName;
        this.onLoaded = onLoaded;
    }

    /**
     * @return The data entries
     */
    public Map<Identifier, T> getData()
    {
        return this.data;
    }

    @Override
    protected void apply(Map<Identifier, T> jsons, ResourceManager resourceManager, ProfilerFiller profiler)
    {
        this.data = new HashMap<>(jsons);
        LOGGER.info("Data loader for {} loaded {} jsons", this.folderName, this.data.size());
        this.onLoaded.accept(this.data);
    }
}
