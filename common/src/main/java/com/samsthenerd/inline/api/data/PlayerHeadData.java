package com.samsthenerd.inline.api.data;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;

import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.util.Identifier;

public class PlayerHeadData implements InlineData{

    public static final Map<UUID, GameProfile> UUID_PROFILE_CACHE = new HashMap<>();
    public static final Map<String, GameProfile> NAME_PROFILE_CACHE = new HashMap<>();

    @Nullable
    public static GameProfile getBetterProfile(GameProfile weakProf){
        // try to find the better profile in our caches
        if(weakProf.getId() != null){
            GameProfile prof = UUID_PROFILE_CACHE.get(weakProf.getId());
            if(prof != null){
                return prof;
            }
        }
        if(weakProf.getName() != null && !weakProf.getName().equals("")){
            GameProfile prof = NAME_PROFILE_CACHE.get(weakProf.getName());
            if(prof != null){
                return prof;
            }
        }
        // can't find, try to fetch it
        SkullBlockEntity.loadProperties(weakProf, betterProf -> {
            if(betterProf.getId() != null){
                UUID_PROFILE_CACHE.put(betterProf.getId(), betterProf);
            }
            if(betterProf.getName() != null && !betterProf.getName().equals("")){
                NAME_PROFILE_CACHE.put(betterProf.getName(), betterProf);
            }
        });
        return null;
    }

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

    // // abstracted so it can be used in the super constructor
    // @Nullable // nullable for now i guess? do a 
    // public static Spritelike spriteFromModid(String modid){
    //     try {
    //         Mod mod = Platform.getMod(modid);
    //         Optional<String> logoFile = mod.getLogoFile(128);
    //         if(logoFile.isEmpty()) return null; // TODO: make this give some placeholder maybe?
    //         Optional<Path> logoPath = mod.findResource(logoFile.get());
    //         if(logoPath.isEmpty()) return null;
    //         return new URLSprite(logoPath.get().toUri().toURL().toString(), new Identifier("inlinemodicon", mod.getModId()));
    //     } catch (Exception e){
    //         return null;
    //     }
    // }

    // public static Style getTooltipStyle(String modid){
    //     try {
    //         Mod mod = Platform.getMod(modid);
    //         // kinda a shame to have it be just the name and not the description and everything but that'd require adding a new tooltip type i think?
    //         HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(mod.getName())); 
    //         Style styled = Style.EMPTY.withHoverEvent(he);
    //         Optional<String> homepageMaybe = mod.getHomepage();
    //         if(homepageMaybe.isPresent()){
    //             ClickEvent ce = new ClickEvent(ClickEvent.Action.OPEN_URL, homepageMaybe.get().toString());
    //             styled = styled.withClickEvent(ce);
    //         }
    //         return styled;
    //     } catch (Exception e){
    //         return Style.EMPTY;
    //     }
    // }

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
