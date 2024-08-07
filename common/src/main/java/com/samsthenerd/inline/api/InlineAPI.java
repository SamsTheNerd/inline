package com.samsthenerd.inline.api;

import com.google.gson.JsonObject;
import com.samsthenerd.inline.api.InlineData.InlineDataType;
import com.samsthenerd.inline.impl.InlineImpl;
import net.minecraft.text.Style;

/**
 * The common Inline API, mostly just for working with InlineData.
 */
public interface InlineAPI {

    /**
     * Usable API Instance.
     */
    InlineAPI INSTANCE = new InlineImpl();


    /**
     * Register an inline data type
     * @param type
     */
    void addDataType(InlineDataType<?> type);

    /**
     * Parse data from json
     * @param json serialized data
     * @return data
     */
    <D extends InlineData<D>> D deserializeData(JsonObject json);

    /**
     * Serialize data to json
     * @param data data to serialize
     * @return serialized data
     */
    <D extends InlineData<D>> JsonObject serializeData(D data);

    /**
     * Attach a size modifier to this style. Generally this should be used for some data-holding style to tell
     * the renderer to render it with a different size.
     * @param style style to attach it to.
     * @param modifier size to scale it by. Ideally should be less than 2.
     * @return a new style object with the given size modifier.
     */
    Style withSizeModifier(Style style, double modifier);
}
