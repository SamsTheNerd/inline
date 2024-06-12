package com.samsthenerd.inline.api.data;

import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.utils.Spritelike;

import net.minecraft.util.Identifier;

public class SpriteInlineData implements InlineData<SpriteInlineData>{
    public SpriteDataType getType(){
        return SpriteDataType.INSTANCE;
    }

    @Override
    public Identifier getRendererId(){
        return new Identifier(Inline.MOD_ID, "spritelike");
    }

    public final Spritelike sprite;

    public SpriteInlineData(Spritelike sprite){
        this.sprite = sprite;
    }

    public static class SpriteDataType implements InlineDataType<SpriteInlineData> {
        public static SpriteDataType INSTANCE = new SpriteDataType();

        @Override
        public Identifier getId(){
            return new Identifier(Inline.MOD_ID, "spritelike");
        }

        public SpriteInlineData deserialize(JsonElement json){
            return new SpriteInlineData(Spritelike.fromJson(json));
        }

        public JsonElement serializeData(SpriteInlineData data){
            Optional<JsonElement> res = Spritelike.CODEC.encodeStart(JsonOps.INSTANCE, data.sprite).result();
            if(res.isPresent()){
                return res.get();
            }
            return new JsonObject();
        }
    }
}
