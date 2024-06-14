package com.samsthenerd.inline.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.samsthenerd.inline.Inline;
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
    public <D extends InlineData<D>> D deserializeData(JsonObject json){
        String type = json.get("type").getAsString();
        if(!DATA_TYPES.containsKey(new Identifier(type))){
            return null;
        }
        InlineDataType<D> dType = (InlineDataType<D>)DATA_TYPES.get(new Identifier(type));
        return dType.getCodec().parse(JsonOps.INSTANCE, json.get("data")).getOrThrow(false, Inline.LOGGER::error);
    }

    @Override
    public <D extends InlineData<D>> JsonObject serializeData(D data){
        InlineDataType<D> dType = data.getType();
        JsonObject json = new JsonObject();
        Optional<JsonElement> dataElem = dType.getCodec().encodeStart(JsonOps.INSTANCE, data).result();
        json.addProperty("type", data.getType().toString());
        json.add("data", dataElem.orElse(new JsonObject()));
        return json;
    }
}
