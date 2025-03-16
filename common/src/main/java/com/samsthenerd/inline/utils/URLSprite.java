package com.samsthenerd.inline.utils;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

// this sprite can be used to fetch pngs or gifs from URLs, local or remote.
public class URLSprite extends Spritelike{
    
    private final String url;
    private final Identifier id;

    @NotNull
    private final UnaryOperator<SpriteUVLens> lensModifier;

    @Nullable
    private IntPair textDims;
    @Nullable
    private SpriteUVLens lens;
    @Nullable
    private Identifier textureID;

//    public static final SpritePosDataOld DEFAULT_UNLOADED_POS_DATA = new SpritePosDataOld(0,0,1,1,0,0);

    public URLSprite(String url, Identifier id){
        this(url, id, UnaryOperator.identity());
    }

    public URLSprite(String url, Identifier id, UnaryOperator<SpriteUVLens> lensModifier){
        this.url = url;
        this.id = id;
        this.lensModifier = lensModifier;
    }

    public SpritelikeType getType(){
        return UrlSpriteType.INSTANCE;
    }

    public Identifier getTextureId(){
        return URLTextureUtils.loadTextureFromURL(url, id);
    }

    public Identifier getId(){
        return id;
    }

    public String getUrl(){
        return url;
    }

    private IntPair getOrFetchDims(){
        if(this.textDims != null) return this.textDims;
        var newTextInfo = URLTextureUtils.getTextureInfo(id);
        if(newTextInfo != null){
            this.textDims = newTextInfo.getLeft();
        }
        return textDims;
    }

    private SpriteUVLens getOrFetchLens(){
        if(this.lens != null) return this.lens;
        var newTextInfo = URLTextureUtils.getTextureInfo(id);
        if(newTextInfo != null){
            this.lens = lensModifier.apply(newTextInfo.getRight());
        }
        return this.lens;
    }

    @Override
    public int getTextureWidth(){
        return Objects.requireNonNullElse(getOrFetchDims(), new IntPair(0, 0)).width();
    }

    @Override
    public int getTextureHeight(){
        return Objects.requireNonNullElse(getOrFetchDims(), new IntPair(0, 0)).height();
    }

    @Override
    public SpriteUVRegion getUVs(long time) {
        return Objects.requireNonNullElse(getOrFetchLens(), SpriteUVLens::identity).genUVs(time);
    }

    public static class UrlSpriteType implements SpritelikeType{
        public static final UrlSpriteType INSTANCE = new UrlSpriteType();
        private static final MapCodec<URLSprite> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("url").forGetter(URLSprite::getUrl),
            Identifier.CODEC.fieldOf("id").forGetter(URLSprite::getId)
        ).apply(instance, URLSprite::new));

        public MapCodec<URLSprite> getCodec(){
            return CODEC;
        }

        public String getId(){
            return "url";
        }
    }
}
