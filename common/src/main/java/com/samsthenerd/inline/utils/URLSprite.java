package com.samsthenerd.inline.utils;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class URLSprite extends Spritelike{
    
    private String url;
    private Identifier id;
    private Identifier textureID;
    private float minU;
    private float minV;
    private float maxU;
    private float maxV;

    public URLSprite(String url, Identifier id, float minU, float minV, float maxU, float maxV){
        this.url = url;
        this.id = id;
        this.minU = minU;
        this.minV = minV;
        this.maxU = maxU;
        this.maxV = maxV;
    }

    public URLSprite(String url, Identifier id){
        this(url, id, 0, 0, 1, 1);
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

    // should these take some timing input to allow for animated sprites ?
    public float getMinU(){
        return this.minU;
    }
    public float getMinV(){
        return this.minV;
    }
    public float getMaxU(){
        return this.maxU;
    }
    public float getMaxV(){
        return this.maxV;
    }

    public int getTextureWidth(){
        getTextureId(); // force it to load real quick
        Pair<Integer, Integer> dims = URLTextureUtils.getTextureDimensions(id);
        if(dims == null){
            return 0;
        }
        return dims.getLeft();
    }

    public int getTextureHeight(){
        getTextureId(); // force it to load real quick
        Pair<Integer, Integer> dims = URLTextureUtils.getTextureDimensions(id);
        if(dims == null){
            return 0;
        }
        return dims.getRight();
    }

    public static class UrlSpriteType implements SpritelikeType{
        public static final UrlSpriteType INSTANCE = new UrlSpriteType();
        private static final Codec<URLSprite> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("url").forGetter(URLSprite::getUrl),
            Identifier.CODEC.fieldOf("id").forGetter(URLSprite::getId),
            Codec.FLOAT.optionalFieldOf("minU", 0f).forGetter(URLSprite::getMinU),
            Codec.FLOAT.optionalFieldOf("minV", 0f).forGetter(URLSprite::getMinV),
            Codec.FLOAT.optionalFieldOf("maxU", 1f).forGetter(URLSprite::getMaxU),
            Codec.FLOAT.optionalFieldOf("maxV", 1f).forGetter(URLSprite::getMaxV)
        ).apply(instance, URLSprite::new));

        public Codec<URLSprite> getCodec(){
            return CODEC;
        }

        public String getId(){
            return "url";
        }
    }
}
