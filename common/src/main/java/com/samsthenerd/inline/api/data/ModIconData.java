package com.samsthenerd.inline.api.data;

import java.nio.file.Path;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.api.InlineData;
import com.samsthenerd.inline.utils.Spritelike;
import com.samsthenerd.inline.utils.TextureSprite;
import com.samsthenerd.inline.utils.URLSprite;
import com.samsthenerd.inline.xplat.IModMeta;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

// mostly just extending so we can still use the renderer
public class ModIconData extends SpriteInlineData{
    public Identifier getDataType(){
        return new Identifier(Inline.MOD_ID, "modicon");
    }

    public Identifier getRendererId(){
        return new Identifier(Inline.MOD_ID, "spritelike");
    }

    public static final Spritelike MISSING_ICON = new TextureSprite(new Identifier(Inline.MOD_ID, "textures/missingicon.png"));

    public String modid;

    public ModIconData(String modid){
        this(modid, true);
    }

    public ModIconData(String modid, boolean usePlaceholder){
        super(spriteFromModid(modid, usePlaceholder));
        this.modid = modid;
    }

    // abstracted so it can be used in the super constructor
    @Nullable
    public static Spritelike spriteFromModid(String modid, boolean usePlaceholder){
        Optional<IModMeta> maybeMod = IModMeta.getMod(modid);
        if(maybeMod.isEmpty()){
            return usePlaceholder ? MISSING_ICON : null;
        }
        IModMeta mod = maybeMod.get();
        try {
            Optional<String> logoFile = mod.getLogoFile(128);
            if(logoFile.isEmpty()) return usePlaceholder ? MISSING_ICON : null;
            Optional<Path> logoPath = mod.findResource(logoFile.get());
            if(logoPath.isEmpty()) return usePlaceholder ? MISSING_ICON : null;
            return new URLSprite(logoPath.get().toUri().toURL().toString(), new Identifier("inlinemodicon", mod.getModId()));
        } catch (Exception e){
            return usePlaceholder ? MISSING_ICON : null;
        }
    }

    public static Style getTooltipStyle(String modid){
        Optional<IModMeta> maybeMod = IModMeta.getMod(modid);
        if(maybeMod.isEmpty()){
            return Style.EMPTY;
        }
        IModMeta mod = maybeMod.get();
        
        // kinda a shame to have it be just the name and not the description and everything but that'd require adding a new tooltip type i think?
        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(mod.getName())); 
        Style styled = Style.EMPTY.withHoverEvent(he);
        Optional<String> homepageMaybe = mod.getHomepage();
        if(homepageMaybe.isPresent()){
            ClickEvent ce = new ClickEvent(ClickEvent.Action.OPEN_URL, homepageMaybe.get().toString());
            styled = styled.withClickEvent(ce);
        }
        return styled;
    }

    public IDSerializer<SpriteInlineData> getSerializer(){
        return Serializer.INSTANCE;
    }

    public static class Serializer implements InlineData.IDSerializer<SpriteInlineData> {
        public static Serializer INSTANCE = new Serializer();

        public SpriteInlineData deserialize(JsonObject json){
            return new ModIconData(json.get("modid").getAsString());
        }

        public JsonElement serializeData(SpriteInlineData data){
            JsonObject obj = new JsonObject();
            obj.addProperty("modid", ((ModIconData)data).modid);
            return obj;
        }
    }
}
