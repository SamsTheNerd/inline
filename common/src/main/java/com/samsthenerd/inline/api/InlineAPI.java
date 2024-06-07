package com.samsthenerd.inline.api;

import com.google.gson.JsonObject;
import com.samsthenerd.inline.impl.InlineImpl;

import net.minecraft.util.Identifier;

public interface InlineAPI {

    public static final InlineAPI INSTANCE = new InlineImpl();

    public void addDataType(Identifier id, InlineData.IDSerializer<?> serializer);

    public InlineData deserializeData(JsonObject json);

    public JsonObject serializeData(InlineData data);
}
