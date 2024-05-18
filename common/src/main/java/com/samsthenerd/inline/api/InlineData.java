package com.samsthenerd.inline.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.Identifier;

// gets attached to style 
public interface InlineData {
    // public static final InlineData EMPTY = new InlineData();

    public Identifier getDataType();

    public Identifier getRendererId();

    public IDSerializer<? extends InlineData> getSerializer();

    public static interface IDSerializer<D extends InlineData> {

        public D deserialize(JsonObject json);

        public JsonElement serializeData(D data);

        public default JsonObject serialize(D data){
            JsonObject json = new JsonObject();
            json.addProperty("type", data.getDataType().toString());
            json.add("data", serializeData(data));
            return json;
        }
    }
}
