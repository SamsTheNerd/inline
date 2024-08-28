package com.samsthenerd.inline.mixin.core;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.JsonOps;
import com.samsthenerd.inline.impl.InlineStyle;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

@Mixin(Style.Serializer.class)
public class MixinInlineStyleSerializer {
    @Unique
    private static final String COMP_KEY = "inlinecomps";

    @ModifyReturnValue(method = "deserialize", at = @At("RETURN"))
    private Style InlineStyDeserialize(Style initialStyle, JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        if (!jsonElement.isJsonObject() || initialStyle == null) {
            return initialStyle;
        }
        JsonObject json = jsonElement.getAsJsonObject();
        if (!json.has(COMP_KEY)) {
            return initialStyle;
        }
        Style copiedStyle = InlineStyle.makeCopy(initialStyle);
        for(Map.Entry<String, JsonElement> compEntry : json.get(COMP_KEY).getAsJsonObject().entrySet()){
            InlineStyle.ISComponent comp = InlineStyle.ISComponent.ALL_COMPS.get(compEntry.getKey());
            if(comp == null) continue;
            Optional<?> compVal = comp.codec().parse(JsonOps.INSTANCE, compEntry.getValue()).result();
            compVal.ifPresent(val -> copiedStyle.setComponent(comp, val));
        }
        return copiedStyle;
    }

    @SuppressWarnings("unchecked")
    @ModifyReturnValue(method = "serialize", at = @At("RETURN"))
    private JsonElement HexPatStySerialize(JsonElement jsonElement, Style style, Type type, JsonSerializationContext jsonSerializationContext) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            return jsonElement;
        }
        JsonObject json = jsonElement.getAsJsonObject();
        JsonObject compsJson = new JsonObject();
        // save all comps
        for(InlineStyle.ISComponent comp : style.getComponents()){
            Optional<JsonElement> dataElem = comp.codec().encodeStart(JsonOps.INSTANCE, style.getComponent(comp)).result();
            dataElem.ifPresent(element -> compsJson.add(comp.id(), element));
        }
        if(compsJson.size() > 0) json.add(COMP_KEY, compsJson);
        return json;
    }
}
