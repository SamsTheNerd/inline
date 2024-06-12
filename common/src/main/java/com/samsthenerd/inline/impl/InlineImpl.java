package com.samsthenerd.inline.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.samsthenerd.inline.api.InlineAPI;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.InlineData.InlineDataType;

import net.minecraft.util.Identifier;

public class InlineImpl implements InlineAPI {

    private final Map<Identifier, InlineDataType<?>> DATA_TYPES = new HashMap<>();

    @Override
    public void addDataType(InlineDataType<?> type){
        DATA_TYPES.put(type.getId(), type);
    }

    @Override
    @Nullable
    public InlineData<?> deserializeData(JsonObject json){
        String type = json.get("type").getAsString();
        if(!DATA_TYPES.containsKey(new Identifier(type))){
            return null;
        }
        return DATA_TYPES.get(new Identifier(type)).deserialize(json.get("data"));
    }

    @Override
    public <D extends InlineData<D>> JsonObject serializeData(D data){
        InlineDataType<D> serializer = data.getType();
        JsonObject json = new JsonObject();
        json.addProperty("type", data.getType().toString());
        json.add("data", serializer.serializeData(data));
        return json;
    }
}
