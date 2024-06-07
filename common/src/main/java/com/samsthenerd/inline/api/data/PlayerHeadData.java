package com.samsthenerd.inline.api.data;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.tooltips.CustomTooltipManager;
import com.samsthenerd.inline.tooltips.providers.EntityTTProvider;
import com.samsthenerd.inline.utils.cradles.PlayerCradle;

import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
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

    public HoverEvent getEntityDisplayHoverEvent(){
        return new HoverEvent(
            HoverEvent.Action.SHOW_ITEM, 
            new HoverEvent.ItemStackContent(CustomTooltipManager.getForTooltip(EntityTTProvider.INSTANCE, new PlayerCradle(profile)))
        ); 
    }

    public Style getDataStyle(boolean withAdditional){
        Style superStyle = InlineData.super.getDataStyle(withAdditional);
        if(!withAdditional) return superStyle;
        return superStyle.withParent(Style.EMPTY.withHoverEvent(getEntityDisplayHoverEvent()));
    }

    public static class Serializer implements InlineData.IDSerializer<PlayerHeadData> {
        public static Serializer INSTANCE = new Serializer();

        private static Gson GSON = new GsonBuilder().create();

        public PlayerHeadData deserialize(JsonElement json){
            return new PlayerHeadData(GSON.fromJson(json, GameProfile.class));
        }

        public JsonElement serializeData(PlayerHeadData data){
            GameProfile profile = ((PlayerHeadData)data).profile;
            return GSON.toJsonTree(profile);
        }
    }
}
