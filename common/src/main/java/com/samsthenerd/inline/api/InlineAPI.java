package com.samsthenerd.inline.api;

import com.google.gson.JsonObject;
import com.samsthenerd.inline.api.InlineData.InlineDataType;
import com.samsthenerd.inline.impl.InlineImpl;

/**
 * The common Inline API, mostly just for working with InlineData.
 */
public interface InlineAPI {

    /**
     * Usable API Instance.
     */
    public static final InlineAPI INSTANCE = new InlineImpl();


    /**
     * Register an inline data type
     * @param type
     */
    public void addDataType(InlineDataType<?> type);

    /**
     * Parse data from json
     * @param json serialized data
     * @return data
     */
    public InlineData<?> deserializeData(JsonObject json);

    /**
     * Serialize data to json
     * @param data data to serialize
     * @return serialized data
     */
    public <D extends InlineData<D>> JsonObject serializeData(D data);
}
