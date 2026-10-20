package untamedwilds.util;

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

import com.mojang.serialization.Codec;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import untamedwilds.UntamedWilds;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class JSONLoader<T> extends SimpleJsonResourceReloadListener<T> {

    private final String folderName;

    protected Map<Identifier, T> data = new HashMap<>();

    /**
     * Creates a data manager that parses every json in a data folder with a codec
     * @param folderName The name of the data folder that we will load from, vanilla folderNames are "recipes", "loot_tables", etc</br>
     * Jsons will be read from data/all_modids/folderName/all_jsons</br>
     * folderName can include subfolders, e.g. "some_mod_that_adds_lots_of_data_loaders/cheeses"
     * @param codec A codec to deserialize the json into your T, see javadocs above class
     */
    public JSONLoader(String folderName, Codec<T> codec) {
        super(codec, FileToIdConverter.json(folderName));
        this.folderName = folderName;
    }

    /**
     * Get the data object for the given key
     * @param id An identifier for a json; e.g. a json at data/some_modid/folderName/some_json.json has id "some_modid:some_json"
     * @return The java object that was deserializd from the json with the given ID, or null if no such object is associated with that ID
     */
    @Nullable
    public T getData(Identifier id) {
        return this.data.get(id);
    }

    @Override
    protected void apply(Map<Identifier, T> parsed, ResourceManager resourceManager, ProfilerFiller profiler) {
        UntamedWilds.LOGGER.info("Beginning loading of data for data loader: {}", this.folderName);
        this.data = new HashMap<>(parsed);
        UntamedWilds.LOGGER.info("Data loader for {} loaded {} jsons", this.folderName, this.data.size());
    }
}
