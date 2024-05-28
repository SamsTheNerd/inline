package com.samsthenerd.inline.api.data;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;

import net.minecraft.util.Identifier;

public class PlayerHeadData implements InlineData{

    @Override
    public Identifier getDataType(){
        return new Identifier(Inline.MOD_ID, "playerhead");
    }

    @Override
    public Identifier getRendererId(){
        return new Identifier(Inline.MOD_ID, "playerhead");
    }

    public GameProfile profile;

    public PlayerHeadData(GameProfile profile){
        this.profile = profile;
    }

    public IDSerializer<PlayerHeadData> getSerializer(){
        return Serializer.INSTANCE;
    }

    public static class Serializer implements InlineData.IDSerializer<PlayerHeadData> {
        public static Serializer INSTANCE = new Serializer();

        private static Gson GSON = new GsonBuilder().create();

        public PlayerHeadData deserialize(JsonObject json){
            return new PlayerHeadData(GSON.fromJson(json.get("profile"), GameProfile.class));
        }

        public JsonElement serializeData(PlayerHeadData data){
            JsonObject obj = new JsonObject();
            GameProfile profile = ((PlayerHeadData)data).profile;
            obj.addProperty("profile", GSON.toJson(profile));
            return obj;
        }
    }
}
