package com.samsthenerd.inline.api.data;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.tooltips.CustomTooltipManager;
import com.samsthenerd.inline.tooltips.providers.EntityTTProvider;
import com.samsthenerd.inline.utils.cradles.GameProfileish;
import com.samsthenerd.inline.utils.cradles.PlayerCradle;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;

public class PlayerHeadData implements InlineData<PlayerHeadData>{

    @Override
    public PlayerHeadDataType getType(){
        return PlayerHeadDataType.INSTANCE;
    }

    @Override
    public Identifier getRendererId(){
        return Inline.id( "playerhead");
    }

    public final GameProfileish profile;

    public PlayerHeadData(GameProfileish profile){ this.profile = profile; }

    public HoverEvent getEntityDisplayHoverEvent(){
        return new HoverEvent(
            HoverEvent.Action.SHOW_ITEM, 
            new HoverEvent.ItemStackContent(CustomTooltipManager.getForTooltip(EntityTTProvider.INSTANCE, new PlayerCradle(profile)))
        ); 
    }

    public Style getDataStyle(boolean withAdditional){
        Style superStyle = InlineData.super.asStyle(withAdditional);
        if(!withAdditional) return superStyle;
        return superStyle.withParent(Style.EMPTY.withHoverEvent(getEntityDisplayHoverEvent()));
    }

    public static class PlayerHeadDataType implements InlineDataType<PlayerHeadData> {
        public static PlayerHeadDataType INSTANCE = new PlayerHeadDataType();

        @Override
        public Identifier getId(){
            return Inline.id( "playerhead");
        }

        private static Gson GSON = new GsonBuilder().create();

        @Override
        public Codec<PlayerHeadData> getCodec(){
            return GameProfileish.GAME_PROFILEISH_CODEC.xmap(PlayerHeadData::new, data -> data.profile);
        }

        public PlayerHeadData deserialize(JsonElement json){
            return new PlayerHeadData(GSON.fromJson(json, GameProfileish.class));
        }

        public JsonElement serializeData(PlayerHeadData data){
            GameProfileish profile = ((PlayerHeadData)data).profile;
            return GSON.toJsonTree(profile);
        }
    }
}
