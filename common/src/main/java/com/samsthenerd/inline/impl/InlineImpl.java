package com.samsthenerd.inline.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineAPI;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.api.InlineData.InlineDataType;

import com.samsthenerd.inline.utils.EntityCradle;
import net.minecraft.util.Identifier;

public class InlineImpl implements InlineAPI {

    private static final Map<Identifier, InlineDataType<?>> DATA_TYPES = new HashMap<>();

    @Override
    public void addDataType(InlineDataType<?> type){
        DATA_TYPES.put(type.getId(), type);
    }

    private static final Codec<InlineDataType<?>> INLINE_DATA_TYPE_CODEC = Identifier.CODEC.comapFlatMap(
            id -> DATA_TYPES.containsKey(id)
                    ? DataResult.success(DATA_TYPES.get(id))
                    : DataResult.error(() -> "No inline data type: " + id.toString()),
            InlineDataType::getId);

    public static final Codec<InlineData<?>> INLINE_DATA_CODEC = INLINE_DATA_TYPE_CODEC.dispatch("type",
            InlineData::getType, InlineDataType::getCodec);

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
