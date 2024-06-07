package com.samsthenerd.inline.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.samsthenerd.inline.api.InlineAPI;
import com.samsthenerd.inline.api.InlineData;

import net.minecraft.util.Identifier;

public class InlineImpl implements InlineAPI {

    private final Map<Identifier, InlineData.IDSerializer<?>> SERIALIZERS = new HashMap<>();

    public void addDataType(Identifier id, InlineData.IDSerializer<?> serializer){
        SERIALIZERS.put(id, serializer);
    }

    @Nullable
    public InlineData deserializeData(JsonObject json){
        String type = json.get("type").getAsString();
        if(!SERIALIZERS.containsKey(new Identifier(type))){
            return null;
        }
        return SERIALIZERS.get(new Identifier(type)).deserialize(json.get("data"));
    }

    @SuppressWarnings("unchecked")
    public JsonObject serializeData(InlineData data){
        InlineData.IDSerializer serializer = data.getSerializer();
        JsonObject json = new JsonObject();
        json.addProperty("type", data.getDataType().toString());
        json.add("data", serializer.serializeData(data));
        return json;
    }
}
