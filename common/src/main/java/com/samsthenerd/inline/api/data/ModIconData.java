package com.samsthenerd.inline.api.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.samsthenerd.inline.Inline;
import com.samsthenerd.inline.impl.InlineStyle;
import com.samsthenerd.inline.tooltips.CustomTooltipManager;
import com.samsthenerd.inline.tooltips.providers.ModDataTTProvider;
import com.samsthenerd.inline.utils.Spritelike;
import com.samsthenerd.inline.utils.TextureSprite;
import com.samsthenerd.inline.utils.URLSprite;
import com.samsthenerd.inline.xplat.IModMeta;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Optional;

// mostly just extending so we can still use the renderer
public class ModIconData extends SpriteInlineData{
    public ModIconDataType getType(){
        return ModIconDataType.INSTANCE;
    }

    public Identifier getRendererId(){
        return Inline.id( "spritelike");
    }

    public static final Spritelike MISSING_ICON = new TextureSprite(Inline.id( "textures/missingicon.png"));

    public String modid;

    public ModIconData(String modid){
        this(modid, true);
    }

    public ModIconData(String modid, boolean usePlaceholder){
        super(spriteFromModid(modid, usePlaceholder));
        this.modid = modid;
    }

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
            return new URLSprite(logoPath.get().toUri().toURL().toString(), Identifier.of("inlinemodicon", mod.getModId()));
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

        HoverEvent he = new HoverEvent(
            HoverEvent.Action.SHOW_ITEM, 
            new HoverEvent.ItemStackContent(CustomTooltipManager.getForTooltip(ModDataTTProvider.INSTANCE, mod))
        ); 
        Style styled = Style.EMPTY.withHoverEvent(he);
        Optional<String> homepageMaybe = mod.getHomepage();
        if(homepageMaybe.isPresent()){
            ClickEvent ce = new ClickEvent(ClickEvent.Action.OPEN_URL, homepageMaybe.get().toString());
            styled = styled.withClickEvent(ce);
        }
        return styled;
    }

    public static Text makeModIcon(IModMeta mod){
        Style dataStyle = InlineStyle.fromInlineData(new ModIconData(mod.getModId()));
        return Text.literal(".").setStyle(dataStyle.withParent(getTooltipStyle(mod.getModId())));
    }

    public static class ModIconDataType extends SpriteDataType {
        public static ModIconDataType INSTANCE = new ModIconDataType();

        @Override
        public Identifier getId(){
            return Inline.id( "modicon");
        }

        @Override
        public Codec<SpriteInlineData> getCodec(){
            return Codec.STRING.flatComapMap(
                ModIconData::new,
                (SpriteInlineData data) -> {
                    if(!(data instanceof ModIconData mData)) {
                        return DataResult.error(() -> "");
                    }
                    return DataResult.success(mData.modid);
                }
            );
        }
    }
}
